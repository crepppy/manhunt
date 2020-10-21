package com.jackchapman.manhuntcore;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Game {
	private final List<UUID> waiting;
	private List<UUID> hunters;
	private UUID hunted;
	private boolean running;
	private boolean ended;
	private PlayerType winners;
	private boolean countdown; // If the hunted player is able to move but the hunters are still frozen

	public Game(List<UUID> players) {
		this.waiting = players;
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
		if(hunters.isEmpty()) return Collections.emptyList();
		return hunters.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
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
		// Teleport all players back to spawn so they don't get an advantage by
		// running whilst players are joining
		World world = getWaitingPlayers().get(0).getWorld();
		getWaitingPlayers().forEach(p -> p.teleport(world.getSpawnLocation().clone().add(0, 1, 0)));

		// Compute which player will be the hunter
		int defWeight = plugin.getConfig().getInt("hunter-chance");
		TreeMap<Integer, Player> items = new TreeMap<>();
		int weight = 0;
		for (Player player : getWaitingPlayers()) {
			weight += Util.getPermissionVariable("core.hunter", player).max().orElse(defWeight);
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
				plugin.getConfig().getInt("hunted-headstart")) + 100L;

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

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF("hub");
		getHuntedPlayer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}

	private enum PlayerType {
		HUNTER, HUNTED
	}


}