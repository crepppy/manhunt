package com.jackchapman.manhuntlobby;

import net.md_5.bungee.api.config.ServerInfo;

public class Game {
	private ServerInfo server;
	private boolean running;
	private int mode;

	public ServerInfo getServer() {
		return server;
	}

	public int getMode() {
		return mode;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isRunning() {
		return running;
	}

	public Game(ServerInfo server, int mode) {
		this.server = server;
		this.mode = mode;
	}
}
