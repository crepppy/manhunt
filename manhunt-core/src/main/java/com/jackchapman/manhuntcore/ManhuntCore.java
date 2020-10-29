package com.jackchapman.manhuntcore;

import com.jackchapman.manhuntcore.commands.ForceStartCommand;
import com.jackchapman.manhuntcore.commands.GiveTrackerCommand;
import com.jackchapman.manhuntcore.listeners.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ManhuntCore extends JavaPlugin {
	public static final ItemStack COMPASS = Util.createItem(Material.COMPASS, "&c&lTracker Compass", "&7Points towards the hunted player");
	public static final ItemStack COMPASS_PLUS = Util.createItem(Material.COMPASS, "&c&lTracker Compass&r&c+", "&7Points towards and shows the distance from the hunter player");
	public static final ItemStack TRACKING_ROD = Util.createItem(Material.FISHING_ROD, "&c&lTracking Rod", "&7Shows the current coordinates of the hunted played");
	private Game game;
	private BungeeConfiguration bungeeConfig;

	@Override
	public void onEnable() {
		// Register plugin channels for communication between lobby and game plugin
		getServer().getMessenger().registerIncomingPluginChannel(this, "manhunt:game", new LobbyPluginMessageListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, "manhunt:game");

		// Register BungeeCord channel to send player between servers
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

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

		// Set empty config, will be set when bungee sends config
		bungeeConfig = new BungeeConfiguration();

		// Every .5 seconds
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			if (game != null && game.isRunning()) {
				// Update the compass to the players location
				Player hunted = game.getHuntedPlayer();
				if (hunted == null) return;
				final Location overworldLoc;
				final Location loadstoneLoc;
				switch (hunted.getWorld().getEnvironment()) {
					case NETHER:
						overworldLoc = game.getNetherPortalOverworld();
						loadstoneLoc = hunted.getLocation();
						break;
					case THE_END:
						overworldLoc = game.getEndPortal();
						loadstoneLoc = game.getNetherPortalNether();
						break;
					default:
						overworldLoc = hunted.getLocation();
						loadstoneLoc = game.getNetherPortalNether();
						break;
				}
				game.getHunterPlayers().forEach(p -> {
					if (p.getWorld().getEnvironment() == World.Environment.NETHER) {
						if(loadstoneLoc == null) return;
						for(int i = 0; i < 36; i++) {
							ItemStack it = p.getInventory().getStorageContents()[i];
							if(it.getItemMeta() != null && it.getType() == Material.COMPASS) {
								CompassMeta meta = (CompassMeta) it.getItemMeta();
								meta.setLodestoneTracked(false);
								meta.setLodestone(loadstoneLoc);
								it.setItemMeta(meta);
							}
						}
					} else {
						p.setCompassTarget(overworldLoc);
					}
				});

				// If a player is holding a compass+ then update their action bar to show the distance from the player
				// the hunted player is in

				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.getInventory().getItemInMainHand().equals(COMPASS_PLUS) || p.getInventory().getItemInOffHand().equals(COMPASS_PLUS)) {
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
								new ComponentBuilder()
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "Dimension: ")
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "§l" + Util.dimensionName(hunted.getLocation()))
										.append(" ")
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "Distance: ")
										.append(Util.colorFromDimension(hunted.getLocation()).toString() + "§l" + Math.round(p.getLocation().distance(p.getWorld().getEnvironment() == World.Environment.NETHER ? loadstoneLoc : overworldLoc)) + "m")
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

	public BungeeConfiguration getBungeeConfig() {
		return bungeeConfig;
	}

	public void setBungeeConfig(BungeeConfiguration bungeeConfig) {
		this.bungeeConfig = bungeeConfig;
	}

}
