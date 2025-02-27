package com.jackchapman.manhuntcore.listeners;

import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EndgameListener implements Listener {
	private final ManhuntCore plugin;

	public EndgameListener(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onHuntedKilled(PlayerDeathEvent e) {
		if (e.getEntity().getUniqueId().equals(plugin.getGame().getHunted())) {
			// Hunted killed - hunters end game
			plugin.endGame(true);
		}
	}

	@EventHandler
	public void onEnderDragonDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof EnderDragon) {
			// Enderdragon killed - hunted player wins
			plugin.endGame(false);
		}
	}

	@EventHandler
	public void onDamageEvent(EntityDamageEvent e) {
		// Stop players from taking damage after the game has ended
		if (e.getEntity() instanceof Player && plugin.getGame() != null && plugin.getGame().isEnded()) {
			e.setCancelled(true);
		}
	}
}
