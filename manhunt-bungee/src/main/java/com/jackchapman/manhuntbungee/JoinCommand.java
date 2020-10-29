package com.jackchapman.manhuntbungee;

import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.party.PartyManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JoinCommand extends Command {
	private final ManhuntBungee plugin;

	public JoinCommand(ManhuntBungee plugin) {
		super("join");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new ComponentBuilder("You must be a player to run this command").color(ChatColor.RED).create());
			return;
		}

		if (args.length == 0) {
			sender.sendMessage(new ComponentBuilder("Incorrect format: /join <# of players>").color(ChatColor.RED).create());
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		List<ProxiedPlayer> party = Collections.singletonList(player);
		if (plugin.getProxy().getPluginManager().getPlugin("PartyAndFriends") == null) {
			if(!PartyManager.getInstance().getParty(player.getUniqueId()).getLeader().getPlayer().equals(player)) {
				player.sendMessage(new ComponentBuilder("Only the party leader can join the queue").color(ChatColor.RED).create());
				return;
			}
			party = PartyManager.getInstance().getParty(player.getUniqueId()).getAllPlayers().stream().map(OnlinePAFPlayer::getPlayer).collect(Collectors.toList());
		}
		if(plugin.getQueue().contains(party)) {
			player.sendMessage(new ComponentBuilder("You are already in the queue. Please do /leavequeue to queue again").color(ChatColor.RED).create());
			return;
		}
		if (args[0].chars().allMatch(Character::isDigit)) {
			int mode = Integer.parseInt(args[0]);
			if (mode == 1) {
				sender.sendMessage(new ComponentBuilder("Gamemode must be at least 2 players").color(ChatColor.RED).create());
			} else if (party.size() > mode) {
				sender.sendMessage(new ComponentBuilder("Your party is too big for this mode!").color(ChatColor.RED).create());
			} else
				plugin.addGame(party, mode);
		} else {
			sender.sendMessage(new ComponentBuilder("Number of players is not a valid number").color(ChatColor.RED).create());
		}
	}
}
