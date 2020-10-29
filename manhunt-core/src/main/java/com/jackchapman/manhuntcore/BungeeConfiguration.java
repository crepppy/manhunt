package com.jackchapman.manhuntcore;

import org.bukkit.ChatColor;

public class BungeeConfiguration {
	private final int huntedHeadstart;
	private final int huntedChance;
	private final String wonGame;
	private final String lostGame;

	public BungeeConfiguration() {
		this.huntedHeadstart = 30;
		this.huntedChance = 50;
		this.wonGame = ChatColor.GREEN + "You Won!";
		this.lostGame = ChatColor.RED + "You Lost!";
	}

	public BungeeConfiguration(int huntedHeadstart, int huntedChance, String wonGame, String lostGame) {
		this.huntedHeadstart = huntedHeadstart;
		this.huntedChance = huntedChance;
		this.wonGame = ChatColor.translateAlternateColorCodes('&', wonGame);
		this.lostGame = ChatColor.translateAlternateColorCodes('&', lostGame);
	}

	public int getHuntedHeadstart() {
		return huntedHeadstart;
	}

	public int getHuntedChance() {
		return huntedChance;
	}

	public String getWonGame() {
		return wonGame;
	}

	public String getLostGame() {
		return lostGame;
	}
}
