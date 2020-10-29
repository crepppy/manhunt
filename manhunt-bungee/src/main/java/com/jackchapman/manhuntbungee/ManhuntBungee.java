package com.jackchapman.manhuntbungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ManhuntBungee extends Plugin implements Listener {
	public static final String PLUGIN_CHANNEL = "manhunt:game";
	private Set<Game> running;
	private Set<ServerInfo> closed;
	private List<List<ProxiedPlayer>> queue;
	private HashMap<List<ProxiedPlayer>, Integer> queueMode;

	// Configuration files
	private Configuration config;

	@Override
	public void onEnable() {
		running = new HashSet<>();
		closed = new HashSet<>();
		queue = new ArrayList<>();
		queueMode = new HashMap<>();
		loadConfig();
		getProxy().registerChannel(PLUGIN_CHANNEL);
		getProxy().getPluginManager().registerCommand(this, new JoinCommand(this));
		getProxy().getPluginManager().registerCommand(this, new LeaveQueueCommand(this));
		getProxy().getPluginManager().registerListener(this, this);
		getProxy().getPluginManager().registerListener(this, new PlayerRoutingListener(this));

		// Remove servers that start offline
		getProxy().getServers().values()
				.stream()
				.filter(x -> x.getName().startsWith(config.getString("game-server-prefix")))
				.forEach(x -> x.ping((callback, throwable) -> {
					if (callback != null) closed.add(x);
				}));

		// Every 30 seconds, check the status of a previously offline server
		getProxy().getScheduler().schedule(this, () -> closed.forEach(x -> x.ping((ping, callback) -> {
			// Server responded, server is back online
			if (callback == null) {
				closed.remove(x);
				// Attempt to move players from the queue into the new mode
				queue.forEach(party -> sendNewServer(party, queueMode.get(party), x));
			}
		})), 30, 30, TimeUnit.SECONDS);
	}

	public synchronized void addGame(List<ProxiedPlayer> players, int mode) {
		Comparator<Game> comparator = Comparator.comparingInt(x -> x.getServer().getPlayers().size());
		// Attempt to find a running server that the party can join
		Optional<Game> server = running
				.stream()
				.filter(x -> x.getMode() == mode)
				.filter(x -> !x.isRunning())
				.filter(x -> mode >= x.getServer().getPlayers().size() + players.size())
				.max(comparator);

		if (server.isPresent()) {
			server.get().getServer().ping((ping, throwable) -> {
				// If the server has crashed / is offline
				if (throwable != null) {
					players.forEach(pl -> pl.sendMessage(new ComponentBuilder("Couldn't join server. Please try to queue again!").color(ChatColor.RED).create()));
					running.remove(server.get());
					return;
				}
				// Send players to server
				players.forEach(pl -> pl.connect(server.get().getServer()));
			});
		} else {
			// No currently running server
			// Create new server // handle no server available
			sendNewServer(players, mode);
		}
	}

	public Set<ServerInfo> getClosed() {
		return closed;
	}

	private void sendNewServer(List<ProxiedPlayer> players, int mode, ServerInfo... servers) {
		for (ServerInfo info : servers.length == 0 ? getProxy().getServers().values() : Arrays.asList(servers)) {
			// Guard cases for server that cannot run a game
			if (!info.getName().startsWith(config.getString("game-server-prefix"))) continue;
			if (closed.contains(info)) continue;
			if (running.stream().noneMatch(x -> x.getServer().equals(info))) {
				info.ping((ping, throwable) -> {
					if (throwable != null) {
						closed.add(info);
						sendNewServer(players, mode);
					} else {
						queue.remove(players);
						queueMode.remove(players);
						running.add(new Game(info, mode));
						players.forEach(pl -> pl.connect(info));
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("start");
						out.writeInt(mode);
						info.sendData(PLUGIN_CHANNEL, out.toByteArray());

						// Send the game configuration to the server
						// This is where custom modes will be implemented
						sendConfig(info);
					}
				});
				return;
			}
		}
		// Couldn't create a server
		if (!queue.contains(players)) {
			queue.add(players);
			queueMode.put(players, mode);
		}
		players.forEach(pl -> pl.sendMessage(new ComponentBuilder("You are position " + (queue.indexOf(players) + 1) + " in the queue").color(ChatColor.YELLOW).create()));
	}

	@EventHandler
	public void on(PluginMessageEvent e) {
		if (!e.getTag().equalsIgnoreCase(PLUGIN_CHANNEL) || e.getReceiver() instanceof Server) return;
		ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
		ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
		String msg = in.readUTF();
		if (msg.equals("end")) {
			// Server has shutdown -> kick players and reset server
			Optional<ServerInfo> lobbyServer = getLobbyServer();
			if (lobbyServer.isPresent())
				player.getServer().getInfo().getPlayers().forEach(pl -> pl.connect(lobbyServer.get()));
			else
				player.getServer().getInfo().getPlayers().forEach(pl -> pl.disconnect(new ComponentBuilder("The server is currently full").color(ChatColor.RED).create()));
			running.removeIf(game -> game.getServer().equals(player.getServer().getInfo()));
			closed.add(player.getServer().getInfo());
		} else if (msg.equals("start")) {
			// Set games status to running
			String players = in.readUTF();
			for (Game game : running) {
				if (game.getServer().equals(player.getServer().getInfo())) {
					game.setPlayers(Arrays.stream(players.split(" ")).map(UUID::fromString).collect(Collectors.toList()));
					game.setRunning(true);
					return;
				}
			}
		}
	}

	private Optional<ServerInfo> getLobbyServer() {
		return getProxy().getServers().values()
				.stream()
				.filter(x -> x.getName().toLowerCase().startsWith(config.getString("lobby-server-prefix")))
				.min(Comparator.comparingInt(x -> x.getPlayers().size()));
	}

	public Set<Game> getRunning() {
		return running;
	}

	public List<List<ProxiedPlayer>> getQueue() {
		return queue;
	}

	public HashMap<List<ProxiedPlayer>, Integer> getQueueMode() {
		return queueMode;
	}

	private void loadConfig() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		try {
			File configFile = new File(getDataFolder(), "config.yml");
			if (!configFile.exists()) {
				configFile.createNewFile();
				try (InputStream is = getResourceAsStream("config.yml");
					 OutputStream os = new FileOutputStream(configFile)) {
					// Copy default config
					ByteStreams.copy(is, os);
				}
			}
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
		} catch (IOException e) {
			getLogger().severe("Could not create config.yml");
		}
	}

	private void sendConfig(ServerInfo info) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("config");
		out.writeInt(config.getInt("game.hunted-headstart"));
		out.writeInt(config.getInt("game.hunted-chance"));
		out.writeUTF(config.getString("game.won-game"));
		out.writeUTF(config.getString("game.lost-game"));
		info.sendData(PLUGIN_CHANNEL, out.toByteArray());
	}

	public boolean removeQueue(UUID player) {
		List<ProxiedPlayer> party = null;
		for (List<ProxiedPlayer> proxiedPlayers : queue) {
			if (!proxiedPlayers.get(0).getUniqueId().equals(player)) {
				// Remove singular player from queue if not in party
				boolean l = proxiedPlayers.removeIf(p -> p.getUniqueId().equals(player));
				if (l) {
					proxiedPlayers.forEach(pl -> pl.sendMessage(new ComponentBuilder(getProxy().getPlayer(player).getName() + " left the queue").color(ChatColor.RED).create()));
					if (getProxy().getPlayer(player) != null)
						getProxy().getPlayer(player).sendMessage(new ComponentBuilder("You left the queue").color(ChatColor.RED).create());
					return true;
				}
			} else {
				party = proxiedPlayers;
			}
		}
		if (party != null) {
			queue.remove(party);
			queueMode.remove(party);
			party.forEach(pl -> pl.sendMessage(new ComponentBuilder("Party leader left the queue").color(ChatColor.RED).create()));
			return true;
		} else {
			return false;
		}
	}
}
