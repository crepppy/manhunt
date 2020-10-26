package com.jackchapman.manhuntlobby;

import com.sun.tools.javac.util.List;
import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.party.PartyManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.stream.Collectors;

public class JoinCommand extends Command {
	private final ManhuntLobby plugin;

	public JoinCommand(ManhuntLobby plugin) {
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
		PlayerParty party = PartyManager.getInstance().getParty(player.getUniqueId());
		if(args[0].chars().allMatch(Character::isDigit)) {
			plugin.addGame(party == null ? List.of(player) : party.getAllPlayers().stream().map(OnlinePAFPlayer::getPlayer).collect(Collectors.toList()), Integer.parseInt(args[0]));
		} else {
			sender.sendMessage(new ComponentBuilder("Number of players is not a valid number").color(ChatColor.RED).create());
		}
	}
}
