package com.jackchapman.manhuntcore;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
	public static IntStream getPermissionVariable(String root, Player player) {
		return player.getEffectivePermissions()
				.stream()
				.filter(PermissionAttachmentInfo::getValue)
				.filter(perm -> perm.getPermission().startsWith(root))
				.mapToInt(perm -> {
					String[] split = perm.getPermission().split("\\.");
					return Integer.parseInt(split[split.length - 1]);
				});
	}

	public static ChatColor colorFromDimension(Location location) {
		switch (location.getWorld().getEnvironment()) {
			case NETHER:
				return ChatColor.RED;
			case THE_END:
				return ChatColor.LIGHT_PURPLE;
		}
		return ChatColor.GREEN;
	}

	public static String dimensionName(Location location) {
		switch (location.getWorld().getEnvironment()) {
			case NETHER:
				return "Nether";
			case THE_END:
				return "The End";
		}
		return "Overworld";
	}

	public static ItemStack createItem(Material material, String name, String... lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (name != null)
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&r" + name));
		if (lore != null)
			meta.setLore(Arrays.stream(lore).map(x -> ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', x)).collect(Collectors.toList()));
		item.setItemMeta(meta);
		return item;
	}
}
