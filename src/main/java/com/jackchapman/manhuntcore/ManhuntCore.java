package com.jackchapman.manhuntcore;

import com.jackchapman.manhuntcore.commands.ForceStartCommand;
import com.jackchapman.manhuntcore.commands.GiveTrackerCommand;
import com.jackchapman.manhuntcore.listeners.EndgameListener;
import com.jackchapman.manhuntcore.listeners.PermissionVariableListeners;
import com.jackchapman.manhuntcore.listeners.PregameListeners;
import com.jackchapman.manhuntcore.listeners.TrackerListeners;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public class ManhuntCore extends JavaPlugin {
	public static final ItemStack COMPASS = Util.createItem(Material.COMPASS, "&c&lTracker Compass", "&7Points towards the hunted player");
	public static final ItemStack COMPASS_PLUS = Util.createItem(Material.COMPASS, "&c&lTracker Compass&r&c+", "&7Points towards and shows the distance from the hunter player");
	public static final ItemStack TRACKING_ROD = Util.createItem(Material.FISHING_ROD, "&c&lTracking Rod", "&7Shows the current coordinates of the hunted played");
	private Game game;
	private List<Player> holdingCompass;

	public List<Player> getHoldingCompass() {
		return holdingCompass;
	}

	@Override
	public void onEnable() {
		holdingCompass = new ArrayList<>();
		saveResource("config.yml", false);

		// Register listeners
		Bukkit.getPluginManager().registerEvents(new PregameListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new PermissionVariableListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new TrackerListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new EndgameListener(this), this);

		// Register commands
		getCommand("forcestart").setExecutor(new ForceStartCommand(this));
		getCommand("givetracker").setExecutor(new GiveTrackerCommand());

		// Register BungeeCord channel to send player between servers
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		// Every .5 seconds
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			if (game != null) {
				// Update the compass to the players location
				Player hunted = Bukkit.getPlayer(game.getHunted());
				game.getHunters().forEach(p -> {
					if (Bukkit.getPlayer(p) != null)
						Bukkit.getPlayer(p).setCompassTarget(hunted.getLocation());
				});

				// If a player is holding a compass+ then update their action bar to show the current dimension
				// the hunted player is in

				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.getInventory().getItemInMainHand().equals(COMPASS_PLUS) || p.getInventory().getItemInOffHand().equals(COMPASS_PLUS)) {
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
								new ComponentBuilder()
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "Dimension: ")
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "§l" + Util.dimensionName(hunted.getLocation()))
										.append(" ")
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "Distance: ")
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "§l" + hunted.getLocation().distance(p.getLocation()) + "m")
										.create());
					} else if (p.getInventory().getItemInMainHand().equals(COMPASS) || p.getInventory().getItemInOffHand().equals(COMPASS)) {
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
								new ComponentBuilder()
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "Dimension: ")
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "§l" + Util.dimensionName(hunted.getLocation()))
										.create());
					}

				}

			}
		}, 100, 10);
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public void startGame() {
		// Get the default headstart time or the time given by a players permission
		long time = 20L * Util.getPermissionVariable("core.headstart", Bukkit.getPlayer(game.getHunted())).max().orElse(
				getConfig().getInt("hunted-headstart")) + 100L;

		// Create a BiFunction to reduce code
		// Shows the countdown of being released to hunted / hunter
		BiFunction<List<UUID>, Boolean, Runnable> f = (players, hunted) -> new Runnable() {
			private int counter = 6;

			@Override
			public void run() {
				counter--;
				if (counter == 0) {
					Bukkit.broadcastMessage(ChatColor.DARK_RED + (hunted ? "The hunted has been released" : "Hunters have been released"));
					game.setRunning(true);
					game.setCountdown(hunted);
				} else {
					players.forEach(p -> Bukkit.getPlayer(p).sendTitle(ChatColor.RED + "" + counter, null, 5, 20, 0));
				}
			}
		};

		// Register the tasks to start the countdowns
		// Cancel the tasks after they are done
		BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, f.apply(Collections.singletonList(game.getHunted()), true), 0, 20);
		Bukkit.getScheduler().runTaskLater(this, task::cancel, 101L);

		BukkitTask task1 = Bukkit.getScheduler().runTaskTimer(this, f.apply(game.getHunters(), false), time - 100L, 20);
		Bukkit.getScheduler().runTaskLater(this, task1::cancel, time + 1);
	}
}
