package com.jackchapman.manhuntcore.commands;

import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GiveTrackerCommand implements CommandExecutor, TabCompleter {
	private static final List<String> tabCompletion = Arrays.asList("rod", "compass", "compass+");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("core.givetracker")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to run this command");
			return true;
		}
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

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
		List<String> tab = new ArrayList<>();
		if (args.length == 0) return Collections.emptyList();
		if (args.length == 1) return null;
		if (args.length == 2) {
			StringUtil.copyPartialMatches(args[1], tabCompletion, tab);
			Collections.sort(tab);
			return tab;
		} else return Collections.emptyList();
	}
}
