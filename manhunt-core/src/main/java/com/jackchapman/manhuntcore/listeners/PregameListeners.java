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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

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
		}
	}

	@EventHandler
	public void postPlayerJoin(PlayerJoinEvent e) {
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (Bukkit.getOnlinePlayers().size() == plugin.getGame().getMode() && plugin.getGame().getPreGameTask() == null) {
				// Start starting game timer as enough players have joined
				plugin.getGame().setPreGameTask(Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
					private int timer = 10;

					@Override
					public void run() {
						if (timer % 5 == 0 || timer < 5) {
							Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.2F));
							Bukkit.broadcastMessage(ChatColor.YELLOW + "Game is starting in: " + timer + "s");
						}
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
		}, 20);
	}


	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if (plugin.getGame().getPreGameTask() != null && !plugin.getGame().getPreGameTask().isCancelled()) {
			plugin.getGame().getPreGameTask().cancel();
			plugin.getGame().setPreGameTask(null);
			Bukkit.broadcastMessage(ChatColor.RED + "Failed to start game - not enough players!");
		}
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			// If the game has started and all players left reset the game
			if (plugin.getGame() != null && plugin.getGame().isRunning() && Bukkit.getOnlinePlayers().isEmpty()) {
				Bukkit.shutdown();
			}
		}, 20);

	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getTo() == null || plugin.getGame() == null || plugin.getGame().getHunted() == null) return;
		if (e.getTo().toVector().equals(e.getFrom().toVector())) return;
//		if (e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ() && e.getTo().getY() != e.getFrom().getY())	return;
		Game game = plugin.getGame();
		// Stop player from moving if the game is waiting to start
		// or only the hunted should be able to move
		if (game.isPreGame(e.getPlayer().getUniqueId())) {
			Location from = e.getFrom();
			Location loc = new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), e.getTo().getYaw(), e.getTo().getPitch());
			e.setTo(loc);
		}

	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Game game = plugin.getGame();
		if (game == null || game.isPreGame(e.getPlayer().getUniqueId()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Game game = plugin.getGame();
		if (game == null || game.isPreGame(e.getPlayer().getUniqueId()))
			e.setCancelled(true);
		else if (game.isCountdown() && e.getClickedBlock() != null && e.getClickedBlock().getLocation().distance(game.getHunterPlayers().get(0).getLocation()) <= 8) {
			e.getPlayer().sendMessage(ChatColor.RED + "Cannot build this close to hunters at the beginning of the game");
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPickup(EntityPickupItemEvent e) {
		Game game = plugin.getGame();
		if (e.getEntity() instanceof Player && (game == null || game.isPreGame(e.getEntity().getUniqueId())))
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerFood(FoodLevelChangeEvent e) {
		Game game = plugin.getGame();
		if (e.getEntity() instanceof Player && (game == null || game.isPreGame(e.getEntity().getUniqueId())))
			e.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player && (plugin.getGame() == null || plugin.getGame().isPreGame(e.getEntity().getUniqueId())))
			e.setCancelled(true);
	}
}
