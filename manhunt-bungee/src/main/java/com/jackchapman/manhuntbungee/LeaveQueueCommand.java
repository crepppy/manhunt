package com.jackchapman.manhuntbungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LeaveQueueCommand extends Command {
	private final ManhuntBungee plugin;

	public LeaveQueueCommand(ManhuntBungee plugin) {
		super("leavequeue");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new ComponentBuilder("You must be a player to run this command").color(ChatColor.RED).create());
			return;
		}
		if(!plugin.removeQueue(((ProxiedPlayer) sender).getUniqueId())) {
			sender.sendMessage(new ComponentBuilder("You aren't in a queue").color(ChatColor.RED).create());
		}
	}
}
