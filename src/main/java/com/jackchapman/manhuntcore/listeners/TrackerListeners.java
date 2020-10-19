package com.jackchapman.manhuntcore.listeners;

import com.jackchapman.manhuntcore.ManhuntCore;
import com.jackchapman.manhuntcore.Util;
import org.bukkit.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TrackerListeners implements Listener {
	private final ManhuntCore plugin;

	public TrackerListeners(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInteractEvent(PlayerInteractEvent e) {
		if(plugin.getGame() == null || !plugin.getGame().isRunning()) return;
		if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().equals(ManhuntCore.TRACKING_ROD)) {
			e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			e.getPlayer().getInventory().setItemInMainHand(null);
			ChatColor color = Util.colorFromDimension(e.getPlayer().getLocation());
			Location l = e.getPlayer().getLocation();
			plugin.getGame().getHuntersAsPlayer().forEach(p -> p.sendMessage(
					ChatColor.translateAlternateColorCodes('&', String.format("&eDimension: %1$s%2$s\n&eX: %1$s%3$s\n&eY: %1$s%4$s\n&eZ: %1$s%5$s", color, Util.dimensionName(l), l.getBlockX(), l.getBlockY(), l.getBlockZ()))
			));
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
}
