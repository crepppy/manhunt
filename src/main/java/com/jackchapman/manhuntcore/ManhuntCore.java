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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ManhuntCore extends JavaPlugin {
	public static final ItemStack COMPASS = Util.createItem(Material.COMPASS, "&c&lTracker Compass", "&7Points towards the hunted player");
	public static final ItemStack COMPASS_PLUS = Util.createItem(Material.COMPASS, "&c&lTracker Compass&r&c+", "&7Points towards and shows the distance from the hunter player");
	public static final ItemStack TRACKING_ROD = Util.createItem(Material.FISHING_ROD, "&c&lTracking Rod", "&7Shows the current coordinates of the hunted played");
	private Game game;

	@Override
	public void onEnable() {
		saveResource("config.yml", false);

		// Register listeners
		Bukkit.getPluginManager().registerEvents(new PregameListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new PermissionVariableListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new TrackerListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new EndgameListener(this), this);

		// Register commands
		GiveTrackerCommand trackerCommand = new GiveTrackerCommand();
		getCommand("forcestart").setExecutor(new ForceStartCommand(this));
		getCommand("givetracker").setExecutor(trackerCommand);
		getCommand("givetracker").setTabCompleter(trackerCommand);

		// Register BungeeCord channel to send player between servers
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		// Every .5 seconds
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			if (game != null) {
				// Update the compass to the players location
				Player hunted = game.getHuntedPlayer();
				if(hunted == null) return;
				game.getHunterPlayers().forEach(p -> p.setCompassTarget(hunted.getLocation()));

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
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "§l" + Math.round(hunted.getLocation().distance(p.getLocation())) + "m")
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

	/**
	 * Starts the current plugin game
	 */
	public void startGame() {
		// Don't attempt to start a running game or a game that doesn't exist
		if (game == null || game.isRunning()) return;

		this.game.start(this);
	}

	/**
	 * @param hunterWin <code>true</code> if hunters win, <code>false</code> if hunted player wins
	 */
	public void endGame(boolean hunterWin) {
		this.game.end(hunterWin, this);
	}

}
