package com.jackchapman.manhuntcore.listeners;

import com.jackchapman.manhuntcore.Game;
import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PregameListeners implements Listener {
	private final ManhuntCore plugin;

	public PregameListeners(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo() == null || e.getTo().toVector().equals(e.getFrom().toVector())) return;
		Game game = plugin.getGame();
		// Stop player from moving if the game is waiting to start
		// or only the hunted should be able to move
		if (game != null && (!game.isRunning() || game.isCountdown() && game.getHunters().contains(e.getPlayer().getUniqueId()))) {
			Location from = e.getFrom();
			Location loc = new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), e.getTo().getYaw(), e.getTo().getPitch());
			e.setTo(loc);
		}

	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Game game = plugin.getGame();
		if (game == null || (!game.isRunning() || game.isCountdown() && game.getHunters().contains(e.getPlayer().getUniqueId())))
			e.setCancelled(true);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Game game = plugin.getGame();
		if (game == null || (!game.isRunning() || game.isCountdown() && game.getHunters().contains(e.getPlayer().getUniqueId())))
			e.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (!plugin.getGame().isRunning() && e.getEntity() instanceof Player) e.setCancelled(true);
	}
}
