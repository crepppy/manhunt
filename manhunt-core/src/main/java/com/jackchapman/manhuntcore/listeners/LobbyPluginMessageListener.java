package com.jackchapman.manhuntcore.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.jackchapman.manhuntcore.BungeeConfiguration;
import com.jackchapman.manhuntcore.Game;
import com.jackchapman.manhuntcore.ManhuntCore;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class LobbyPluginMessageListener implements PluginMessageListener {
	private final ManhuntCore plugin;

	public LobbyPluginMessageListener(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String action = in.readUTF();
		System.out.println(action);
		if (action.equals("start")) {
			int mode = in.readInt();
			plugin.setGame(new Game(mode));
			World w = Bukkit.getServer().getWorlds().get(0);
			w.setDifficulty(Difficulty.PEACEFUL);
			w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		}
		if (action.equals("config")) {
			plugin.setBungeeConfig(new BungeeConfiguration(
					in.readInt(), in.readInt(), in.readUTF(), in.readUTF()
			));
		}
	}
}
