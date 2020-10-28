package com.jackchapman.manhuntlobby;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Optional;

public class PlayerRoutingListener implements Listener {
	private ManhuntLobby plugin;

	public PlayerRoutingListener(ManhuntLobby plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLeave(ServerDisconnectEvent e) {
		Optional<Game> game = plugin.getRunning().stream().filter(x -> x.getServer().equals(e.getTarget())).findAny();
		if (game.isPresent() && e.getTarget().getPlayers().isEmpty()) {
			if (game.get().isRunning())
				plugin.getClosed().add(e.getTarget());
			plugin.getRunning().remove(game.get());
		}
	}

	@EventHandler
	public void onJoin(ServerConnectEvent e) {
		e.setTarget(plugin.getRunning().stream().filter(Game::isRunning).filter(x -> x.getPlayers().contains(e.getPlayer().getUniqueId())).findAny().map(Game::getServer).orElse(e.getTarget()));
	}
}
