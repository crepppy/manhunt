package com.jackchapman.manhuntcore.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EndgameListener implements Listener {
	private final ManhuntCore plugin;

	public EndgameListener(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onHuntedKilled(PlayerDeathEvent e) {
		if (e.getEntity().getUniqueId().equals(plugin.getGame().getHunted())) {
			// Hunted killed - end game
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF("hub");
			e.getEntity().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
		}
	}
}
