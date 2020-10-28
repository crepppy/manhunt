package com.jackchapman.manhuntbungee;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.UUID;

public class Game {
	private final ServerInfo server;
	private final int mode;
	private boolean running;
	private List<UUID> players;

	public Game(ServerInfo server, int mode) {
		this.server = server;
		this.mode = mode;
	}

	public List<UUID> getPlayers() {
		return players;
	}

	public void setPlayers(List<UUID> players) {
		this.players = players;
	}

	public ServerInfo getServer() {
		return server;
	}

	public int getMode() {
		return mode;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
