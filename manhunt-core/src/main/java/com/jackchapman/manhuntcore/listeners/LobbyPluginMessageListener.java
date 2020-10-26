package com.jackchapman.manhuntcore.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.jackchapman.manhuntcore.Game;
import com.jackchapman.manhuntcore.ManhuntCore;
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

		if (action.equals("start")) {
			int mode = in.readInt();
			plugin.setGame(new Game(mode));
		}
	}
}
