package com.jackchapman.manhuntcore.commands;

import com.jackchapman.manhuntcore.Game;
import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ForceStartCommand implements CommandExecutor {
	private final ManhuntCore plugin;

	public ForceStartCommand(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 1) {
			List<UUID> players = new ArrayList<>();
			for (String name : args) {
				Player p = Bukkit.getPlayer(name);
				if (p == null) {
					sender.sendMessage(ChatColor.RED + name + " does no exists. Make sure it is spelt correctly");
					return true;
				}
				players.add(p.getUniqueId());
			}
			plugin.setGame(new Game(players));
			plugin.startGame();
		} else {
			sender.sendMessage(ChatColor.RED + "Incorrect Format: /forcestart <player1> <player2>...");
		}

		return true;
	}
}
