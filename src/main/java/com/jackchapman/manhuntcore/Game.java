package com.jackchapman.manhuntcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Game {
	private final List<UUID> players;
	private List<UUID> hunters;
	private UUID hunted;
	private boolean running;
	private boolean countdown; // If the hunted player is able to move but the hunters are still frozen

	public Game(List<UUID> players) {
		this.players = players;
	}

	/**
	 * Returns the players waiting for game to start
	 * List is empty after game has started
	 *
	 * @return The players currently in the game
	 */
	public List<Player> getPlayers() {
		return players.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
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

	public List<UUID> getHunters() {
		return hunters;
	}

	public void setHunters(List<UUID> hunters) {
		this.hunters = hunters;
	}

	public UUID getHunted() {
		return hunted;
	}

	public void setHunted(UUID hunted) {
		this.hunted = hunted;
	}

	public Player getHuntedPlayer() {
		return Bukkit.getPlayer(hunted);
	}

	public List<Player> getHuntersAsPlayer() {
		return hunters.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
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