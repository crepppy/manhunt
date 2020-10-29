package com.jackchapman.manhuntbungee;

import de.simonsator.partyandfriends.api.events.party.LeftPartyEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerRoutingListener implements Listener {
	private final ManhuntBungee plugin;
	private final List<UUID> sendBack;

	public PlayerRoutingListener(ManhuntBungee plugin) {
		this.plugin = plugin;
		sendBack = new ArrayList<>();
	}

	@EventHandler
	public void onLeave(ServerDisconnectEvent e) {
		plugin.removeQueue(e.getPlayer().getUniqueId());
		Optional<Game> game = plugin.getRunning().stream().filter(x -> x.getServer().equals(e.getTarget())).findAny();
		if (game.isPresent() && e.getTarget().getPlayers().isEmpty()) {
			if (game.get().isRunning())
				plugin.getClosed().add(e.getTarget());
			plugin.getRunning().remove(game.get());
		}
	}

	@EventHandler
	public void onLogin(PreLoginEvent e) {
		sendBack.add(e.getConnection().getUniqueId());
	}

	@EventHandler
	public void onJoin(ServerConnectEvent e) {
		if (sendBack.remove(e.getPlayer().getUniqueId())) {
			e.setTarget(plugin.getRunning().stream().filter(Game::isRunning).filter(x -> x.getPlayers().contains(e.getPlayer().getUniqueId())).findAny().map(Game::getServer).orElse(e.getTarget()));
		}
	}

	@EventHandler
	public void onLeaveParty(LeftPartyEvent e) {
		plugin.removeQueue(e.getPlayer().getUniqueId());
	}
}
