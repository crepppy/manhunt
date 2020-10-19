package com.jackchapman.manhuntcore.commands;

import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveTrackerCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Incorrect format: /givetracker <player> <compass | compass+ | rod>");
		} else {
			Player player = Bukkit.getPlayer(args[0]);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "This player is not online");
			} else {
				switch (args[1].toLowerCase()) {
					case "compass":
						player.getInventory().addItem(ManhuntCore.COMPASS);
						break;
					case "compass+":
						player.getInventory().addItem(ManhuntCore.COMPASS_PLUS);
						break;
					case "rod":
						player.getInventory().addItem(ManhuntCore.TRACKING_ROD);
						break;
				}

			}
		}
		return true;
	}
}
