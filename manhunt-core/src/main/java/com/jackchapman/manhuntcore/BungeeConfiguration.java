package com.jackchapman.manhuntcore;

import org.bukkit.ChatColor;

public class BungeeConfiguration {
	private final int huntedHeadstart;
	private final int hunterChance;
	private final String wonGame;
	private final String lostGame;

	public BungeeConfiguration() {
		this.huntedHeadstart = 30;
		this.hunterChance = 50;
		this.wonGame = ChatColor.GREEN + "You Won!";
		this.lostGame = ChatColor.RED + "You Lost!";
	}

	public BungeeConfiguration(int huntedHeadstart, int hunterChance, String wonGame, String lostGame) {
		this.huntedHeadstart = huntedHeadstart;
		this.hunterChance = hunterChance;
		this.wonGame = ChatColor.translateAlternateColorCodes('&', wonGame);
		this.lostGame = ChatColor.translateAlternateColorCodes('&', lostGame);
	}

	public int getHuntedHeadstart() {
		return huntedHeadstart;
	}

	public int getHunterChance() {
		return hunterChance;
	}

	public String getWonGame() {
		return wonGame;
	}

	public String getLostGame() {
		return lostGame;
	}
}
