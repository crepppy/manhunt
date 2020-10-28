package com.jackchapman.manhuntlobby;

import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerRoutingListener implements Listener {
	private ManhuntLobby plugin;

	public PlayerRoutingListener(ManhuntLobby plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent e) {
		if(plugin.getRunning().stream().anyMatch(x -> x.getServer().equals(e.getPlayer().getServer().getInfo())) && e.getPlayer().getServer().getInfo().getPlayers().isEmpty()) {
			plugin.getRunning().removeIf(x -> x.getServer().equals(e.getPlayer().getServer().getInfo()));
		}
	}

	@EventHandler
	public void onJoin(ServerConnectEvent e) {
		e.setTarget(plugin.getRunning().stream().filter(x -> x.getPlayers().contains(e.getPlayer().getUniqueId())).findAny().map(Game::getServer).orElse(e.getTarget()));
	}
}
