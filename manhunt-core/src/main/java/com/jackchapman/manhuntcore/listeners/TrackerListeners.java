package com.jackchapman.manhuntcore.listeners;

import com.jackchapman.manhuntcore.Game;
import com.jackchapman.manhuntcore.ManhuntCore;
import com.jackchapman.manhuntcore.Util;
import org.bukkit.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class TrackerListeners implements Listener {
	private final ManhuntCore plugin;

	public TrackerListeners(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInteractEvent(PlayerInteractEvent e) {
		if (plugin.getGame() == null || !plugin.getGame().isRunning()) return;
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().equals(ManhuntCore.TRACKING_ROD)) {
			e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			e.getItem().setAmount(0);
			ChatColor color = Util.colorFromDimension(e.getPlayer().getLocation());
			Location l = plugin.getGame().getHuntedPlayer().getLocation();
			plugin.getGame().getHunterPlayers().forEach(p -> p.sendMessage(
					ChatColor.translateAlternateColorCodes('&', String.format("&eDimension: %1$s%2$s\n&eX: %1$s%3$s\n&eY: %1$s%4$s\n&eZ: %1$s%5$s", color, Util.dimensionName(l), l.getBlockX(), l.getBlockY(), l.getBlockZ()))
			));
			plugin.getGame().getRods().merge(e.getPlayer().getUniqueId(), 1, Integer::sum);
		}
	}

	@EventHandler
	public void onCraftCompass(CraftItemEvent e) {
		if (e.getRecipe().getResult().getType() == Material.COMPASS && plugin.getGame().getHunters().contains(e.getWhoClicked().getUniqueId())) {
			e.setResult(Event.Result.DENY);
			e.getWhoClicked().sendMessage(ChatColor.RED + "You must only use the compass given.");
		}
	}

	@EventHandler
	public void onDropTracker(PlayerDropItemEvent e) {
		ItemStack is = e.getItemDrop().getItemStack();
		if (is.equals(ManhuntCore.COMPASS) || is.equals(ManhuntCore.COMPASS_PLUS) || is.equals(ManhuntCore.TRACKING_ROD))
			e.setCancelled(true);
	}

	@EventHandler
	public void onHunterDie(PlayerDeathEvent e) {
		Game game = plugin.getGame();
		if (game.getHunters().contains(e.getEntity().getUniqueId())) {
			e.getDrops().remove(ManhuntCore.COMPASS);
			e.getDrops().remove(ManhuntCore.COMPASS_PLUS);
			e.getDrops().removeIf(x -> x.equals(ManhuntCore.TRACKING_ROD));
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Game game = plugin.getGame();
		if (game.getHunters().contains(e.getPlayer().getUniqueId())) {
			game.giveCompass(e.getPlayer());
		}
	}

	@EventHandler
	public void onTrackerMoveToInventory(InventoryClickEvent e) {
		if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER && (e.getCurrentItem() == ManhuntCore.COMPASS || e.getCurrentItem() == ManhuntCore.COMPASS_PLUS || e.getCurrentItem() == ManhuntCore.TRACKING_ROD)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPortalEnter(PlayerPortalEvent e) {
		if (plugin.getGame() != null && plugin.getGame().getHunted().equals(e.getPlayer().getUniqueId()))
			if (e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL)
				plugin.getGame().setEndPortal(e.getFrom());
			else if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
				if (e.getFrom().getWorld().getEnvironment() == World.Environment.NORMAL)
					plugin.getGame().setNetherPortalOverworld(e.getFrom());
				if (e.getFrom().getWorld().getEnvironment() == World.Environment.NETHER)
					plugin.getGame().setNetherPortalNether(e.getFrom());
			}
	}
}
