package com.jackchapman.manhuntcore;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Game {
	private final List<UUID> waiting;
	private final int mode;
	private final HashMap<UUID, Integer> rods;
	private List<UUID> hunters;
	private UUID hunted;
	private BukkitTask preGameTask;
	private boolean running;
	private boolean ended;
	private PlayerType winners;
	private boolean countdown; // If the hunted player is able to move but the hunters are still frozen
	private Location endPortal;
	private Location netherPortalNether;
	private Location netherPortalOverworld;

	public Game(int mode) {
		this.rods = new HashMap<>();
		this.mode = mode;
		this.waiting = new ArrayList<>();
	}

	public Location getNetherPortalNether() {
		return netherPortalNether;
	}

	public void setNetherPortalNether(Location netherPortalNether) {
		this.netherPortalNether = netherPortalNether;
	}

	public Location getNetherPortalOverworld() {
		return netherPortalOverworld;
	}

	public void setNetherPortalOverworld(Location netherPortalOverworld) {
		this.netherPortalOverworld = netherPortalOverworld;
	}

	public Location getEndPortal() {
		return endPortal;
	}

	public void setEndPortal(Location endPortal) {
		this.endPortal = endPortal;
	}

	public HashMap<UUID, Integer> getRods() {
		return rods;
	}

	public BukkitTask getPreGameTask() {
		return preGameTask;
	}

	public void setPreGameTask(BukkitTask preGameTask) {
		this.preGameTask = preGameTask;
	}

	public int getMode() {
		return mode;
	}

	public boolean isEnded() {
		return ended;
	}

	public List<UUID> getWinners() {
		return winners == PlayerType.HUNTED ? Collections.singletonList(hunted) : hunters;
	}

	/**
	 * Returns the players waiting for game to start
	 * List is empty after game has started
	 *
	 * @return The players currently in the game
	 */
	public List<Player> getWaitingPlayers() {
		return waiting.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
	}

	public List<UUID> getWaiting() {
		return waiting;
	}

	public List<Player> getPlayers() {
		return new ArrayList<Player>() {{
			addAll(getHunterPlayers());
			add(getHuntedPlayer());
		}};
	}

	/**
	 * The countdown stage is the stage between the hunted player being released and the hunters being released
	 *
	 * @return <code>true</code> if the hunters are waiting to be released
	 */
	public boolean isCountdown() {
		return countdown;
	}

	public void setCountdown(boolean countdown) {
		this.countdown = countdown;
	}

	public List<UUID> getHunters() {
		return hunters;
	}

	public void setHunters(List<UUID> hunters) {
		this.hunters = hunters;
	}

	public UUID getHunted() {
		return hunted;
	}

	public void setHunted(UUID hunted) {
		this.hunted = hunted;
	}

	public Player getHuntedPlayer() {
		return Bukkit.getPlayer(hunted);
	}

	public List<Player> getHunterPlayers() {
		if (hunters.isEmpty()) return Collections.emptyList();
		return hunters.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
	}

	public List<Player> getWinnerPlayers() {
		return getWinners().stream().map(Bukkit::getPlayer).collect(Collectors.toList());
	}

	/**
	 * Whether the game is running (note if the hunted player hasn't been released yet the game is not running
	 *
	 * @return <code>true</code> if the game is running
	 */
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void start(ManhuntCore plugin) {
		Bukkit.getWorlds().get(0).setDifficulty(Difficulty.NORMAL);
		Bukkit.getWorlds().get(0).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		// Teleport all players back to spawn so they don't get an advantage by
		// running whilst players are joining
		World world = getWaitingPlayers().get(0).getWorld();
		getWaitingPlayers().forEach(p -> p.teleport(world.getSpawnLocation()));

		// Compute which player will be the hunter
		int defWeight = plugin.getBungeeConfig().getHuntedChance();
		TreeMap<Integer, Player> items = new TreeMap<>();
		int weight = 0;
		for (Player player : getWaitingPlayers()) {
			weight += Util.getPermissionVariable("core.hunted", player).max().orElse(defWeight);
			items.put(weight, player);
		}

		// Set hunter and keep reference
		Player hunted = items.ceilingEntry(new Random().nextInt(weight) + 1).getValue();
		this.hunted = hunted.getUniqueId();
		this.waiting.remove(hunted.getUniqueId());

		// Players is no longer needed as players are only accessed by their role (hunter / hunted)
		List<Player> hunters = getWaitingPlayers();
		this.hunters = hunters.stream().map(Entity::getUniqueId).collect(Collectors.toList());
		waiting.clear();

		// Get the default headstart time or the time given by a players permission
		long time = 20L * Util.getPermissionVariable("core.headstart", hunted).max().orElse(
				plugin.getBungeeConfig().getHuntedHeadstart()) + 100L;

		// Send message to proxy to allow players to reconnect
		ByteArrayDataOutput endOutput = ByteStreams.newDataOutput();
		endOutput.writeUTF("start");
		endOutput.writeUTF(getPlayers().stream().map(x -> x.getUniqueId().toString()).collect(Collectors.joining(" ")));
		getHuntedPlayer().sendPluginMessage(plugin, "manhunt:game", endOutput.toByteArray());

		// Create a BiFunction to reduce code
		// Shows the countdown of being released to hunted / hunter
		BiFunction<List<Player>, Boolean, Runnable> f = (players, huntedPlayer) -> new Runnable() {
			private int counter = 6;

			@Override
			public void run() {
				counter--;
				if (counter == 0) {
					Bukkit.broadcastMessage(ChatColor.DARK_RED + (huntedPlayer ? "The hunted has been released" : "Hunters have been released"));
					running = true;
					countdown = huntedPlayer;
				} else {
					players.forEach(p -> p.sendTitle(ChatColor.RED + "" + counter, null, 5, 20, 0));
				}
			}
		};

		// Give the correct compass to the hunters
		hunters.forEach(this::giveCompass);

		// Register the tasks to start the countdowns
		// Cancel the tasks after they are done
		BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, f.apply(Collections.singletonList(hunted), true), 0, 20);
		Bukkit.getScheduler().runTaskLater(plugin, task::cancel, 101L);

		BukkitTask task1 = Bukkit.getScheduler().runTaskTimer(plugin, f.apply(hunters, false), time - 100L, 20);
		Bukkit.getScheduler().runTaskLater(plugin, task1::cancel, time + 1);
	}

	public void end(boolean hunterWin, ManhuntCore plugin) {
		ended = true;
		winners = hunterWin ? PlayerType.HUNTER : PlayerType.HUNTED;

		List<Player> winningPlayers = getWinnerPlayers();
		final String winningPlayersStr = winningPlayers.stream().map(Player::getDisplayName).collect(Collectors.joining(", "));
		final String winningTeam = winners.toString().toLowerCase();
		final String wonText = plugin.getBungeeConfig().getWonGame()
				.replace("%winners", winningPlayersStr)
				.replace("%winningTeam", winningTeam);
		final String lostText = plugin.getBungeeConfig().getLostGame()
				.replace("%winners", winningPlayersStr)
				.replace("%winningTeam", winningTeam);

		winningPlayers.forEach(x -> x.sendTitle(wonText, null, 5, 15 * 20, 5));
		List<Player> losers = getPlayers();
		losers.removeAll(winningPlayers);
		losers.forEach(x -> x.sendTitle(lostText, null, 5, 15 * 20, 5));
		getPlayers().forEach(player -> player.sendMessage(ChatColor.DARK_RED + "Returning to lobby in 15 seconds"));

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			ByteArrayDataOutput endOutput = ByteStreams.newDataOutput();
			endOutput.writeUTF("end");
			endOutput.writeBoolean(ended);
			getHuntedPlayer().sendPluginMessage(plugin, "manhunt:game", endOutput.toByteArray());
		}, 15 * 20L);
		Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 15 * 20L + 100);
	}

	public void giveCompass(Player hunter) {
		if (hunter.hasPermission("core.compass+")) hunter.getInventory().addItem(ManhuntCore.COMPASS_PLUS);
		else hunter.getInventory().addItem(ManhuntCore.COMPASS);
		for (int i = rods.getOrDefault(hunter.getUniqueId(), 0); i < Util.getPermissionVariable("core.rod", hunter).max().orElse(0); i++) {
			hunter.getInventory().addItem(ManhuntCore.TRACKING_ROD);
		}
	}

	public boolean isPreGame(UUID uuid) {
		return !running || (countdown && hunters.contains(uuid));
	}

	private enum PlayerType {
		HUNTER, HUNTED
	}
}