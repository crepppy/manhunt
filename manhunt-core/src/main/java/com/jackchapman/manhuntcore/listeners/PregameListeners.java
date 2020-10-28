package com.jackchapman.manhuntcore.listeners;

import com.jackchapman.manhuntcore.Game;
import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.stream.Collectors;

public class PregameListeners implements Listener {
	private final ManhuntCore plugin;


	public PregameListeners(ManhuntCore plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
		if (plugin.getGame() == null || plugin.getGame().isRunning()) return;
		if (Bukkit.getOnlinePlayers().size() + 1 > plugin.getGame().getMode()) {
			e.setKickMessage(ChatColor.RED + "This server is currently full. Please try again in a few seconds");
			e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_FULL);
			return;
		}
		if (Bukkit.getOnlinePlayers().size() + 1 == plugin.getGame().getMode()) {
			// Start starting game timer as enough players have joined
			boolean playerLeft;
			plugin.getGame().setPreGameTask(Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
				private int timer = 10;

				@Override
				public void run() {
					if (timer % 5 == 0 || timer < 5)
						Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.2F));
					Bukkit.broadcastMessage(ChatColor.YELLOW + "Game is starting in: " + timer + "s");
					timer--;
				}
			}, 20, 20));
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (!plugin.getGame().getPreGameTask().isCancelled())
					plugin.getGame().getPreGameTask().cancel();
				if (Bukkit.getOnlinePlayers().size() == plugin.getGame().getMode()) {
					plugin.getGame().getWaiting().addAll(Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList()));
					plugin.startGame();
				} else {
					Bukkit.broadcastMessage(ChatColor.RED + "Failed to start game - not enough players!");
				}

			}, 10 * 20 + 1);
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if (plugin.getGame().getPreGameTask() != null && !plugin.getGame().getPreGameTask().isCancelled()
				&& plugin.getGame().getWaiting().contains(e.getPlayer().getUniqueId())) {
			plugin.getGame().getPreGameTask().cancel();
			plugin.getGame().setPreGameTask(null);
			Bukkit.broadcastMessage(ChatColor.RED + "Failed to start game - not enough players!");
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo() == null || plugin.getGame() == null || plugin.getGame().getWaiting().isEmpty()) return;
		if (e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ() && e.getTo().getY() != e.getFrom().getY())
			return;
		Game game = plugin.getGame();
		// Stop player from moving if the game is waiting to start
		// or only the hunted should be able to move
		if (!game.isRunning() || (game.isCountdown() && game.getHunters().contains(e.getPlayer().getUniqueId()))) {
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
		if (plugin.getGame() == null || !plugin.getGame().isRunning() && e.getEntity() instanceof Player)
			e.setCancelled(true);
	}
}
