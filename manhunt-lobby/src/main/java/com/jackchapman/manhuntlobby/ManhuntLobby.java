package com.jackchapman.manhuntlobby;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ManhuntLobby extends Plugin implements Listener {
	public static final String PLUGIN_CHANNEL = "manhunt:game";
	private List<Game> running;

	@Override
	public void onEnable() {
		running = new ArrayList<>();
		getProxy().registerChannel(PLUGIN_CHANNEL);
		getProxy().getPluginManager().registerCommand(this, new JoinCommand(this));
		getProxy().getPluginManager().registerListener(this, this);
	}

	public synchronized void addGame(List<ProxiedPlayer> players, int mode) {
		int max = 3;
		Comparator<Game> comparator = Comparator.comparingInt(x -> x.getServer().getPlayers().size());
		Optional<Game> server = running
				.stream()
				.filter(x -> x.getMode() == mode)
				.filter(x -> !x.isRunning())
				.filter(x -> max >= x.getServer().getPlayers().size() + players.size())
				.max(comparator);

		if (server.isPresent()) {
			// Send players to server
			players.forEach(pl -> pl.connect(server.get().getServer()));
		} else {
			// Create new server // handle no server available
			for (ServerInfo info : getProxy().getServers().values()) {
				if (running.stream().noneMatch(x -> x.getServer().equals(info))) {
					running.add(new Game(info, mode));
					players.forEach(pl -> pl.connect(info));
					ByteArrayDataOutput out = ByteStreams.newDataOutput();
					out.writeUTF("start");
					out.writeInt(mode);
					players.get(0).getServer().getInfo().sendData(PLUGIN_CHANNEL, out.toByteArray());
					return;
				}
			}
			// Couldn't create a server
		}
	}

	@EventHandler
	public void on(PluginMessageEvent e) {
		if (!e.getTag().equalsIgnoreCase(PLUGIN_CHANNEL) || e.getReceiver() instanceof Server) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
		if(in.readUTF().equals("end")) {
			boolean hunterWin = in.readBoolean();
			ProxiedPlayer player  = (ProxiedPlayer) e.getReceiver();
			running.removeIf(game -> game.getServer().equals(player.getServer().getInfo()));
		}
	}
}
