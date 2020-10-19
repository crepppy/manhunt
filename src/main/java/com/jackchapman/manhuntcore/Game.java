package com.jackchapman.manhuntcore;

import java.util.List;
import java.util.UUID;

public class Game {
	private final List<UUID> hunters;
	private UUID hunted;
	private boolean running;
	private boolean countdown; // If the hunted player is able to move but the hunters are still frozen

	public Game(UUID hunted, List<UUID> hunters) {
		this.hunters = hunters;
		this.hunted = hunted;
	}

	/**
	 * The countdown stage is the stage between the hunted player being released and the hunters being released
	 *
	 * @return <code>true</code> if the hunters are waiting to be released
	 */
	public boolean isCountdown() {
		return countdown;
	}

	public void setCountdown(boolean countdown) {
		this.countdown = countdown;
	}

	public UUID getHunted() {
		return hunted;
	}

	public void setHunted(UUID hunted) {
		this.hunted = hunted;
	}

	public List<UUID> getHunters() {
		return hunters;
	}

	/**
	 * Whether the game is running (note if the hunted player hasn't been released yet the game is not running
	 *
	 * @return <code>true</code> if the game is running
	 */
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}