package com.jackchapman.manhuntcore.listeners;

import com.jackchapman.manhuntcore.ManhuntCore;
import com.jackchapman.manhuntcore.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class PermissionVariableListeners implements Listener {
	private final ManhuntCore plugin;
	private final HashMap<Location, Integer> furnaceTasks = new HashMap<>();
	private final HashMap<Location, Double> carry = new HashMap<>();

	public PermissionVariableListeners(ManhuntCore plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEndermanKill(EntityDeathEvent e) {
		if (e.getEntity().getKiller() == null || !(e.getEntity() instanceof Enderman)) return;
		int looting = e.getEntity().getKiller().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
		e.getDrops().clear();
		// Calculate the increase rate from permission
		double rate = Util.getPermissionVariable("core.enderman", e.getEntity().getKiller()).max().orElse(0) / 100F;
		// Find additional drops
		// https://minecraft.gamepedia.com/Drops#Looting
		// Default chance is .5F
		Random rand = new Random();
		int normal = rand.nextDouble() < (.5F * (1 + rate)) ? 1 : 0;
		int additional = (int) Math.round(rand.nextDouble() * (looting + rate));
		e.getDrops().add(new ItemStack(Material.ENDER_PEARL, (int) Math.min(normal + additional, looting + Math.ceil(rate / 1))));
	}

	@EventHandler
	public void onFurnaceSmelt(InventoryClickEvent e) {
		if ((e.getView().getTopInventory().getType() != InventoryType.FURNACE)
				|| e.getView().getTopInventory().getLocation() == null) return;
		if (e.getSlotType() == InventoryType.SlotType.CONTAINER && e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY)
			return;
		// Get the increase rate from permission
		double rate = Util.getPermissionVariable("core.furnace", (Player) e.getWhoClicked())
				.max().orElse(0) / 100F;

		// Remove current listeners so the furnace isn't getting updated more than once per tick
		if (furnaceTasks.containsKey(e.getView().getTopInventory().getLocation()))
			Bukkit.getScheduler().cancelTask(furnaceTasks.get(e.getView().getTopInventory().getLocation()));

		// Every tick change the cook time by the increased rate
		furnaceTasks.put(e.getInventory().getLocation(), Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			BlockState state = e.getView().getTopInventory().getLocation().getBlock().getState();
			if (!(state instanceof Furnace)) {
				int task = furnaceTasks.get(state.getLocation());
				furnaceTasks.remove(state.getLocation());
				Bukkit.getScheduler().cancelTask(task);
				return;
			}
			Furnace furnace = (Furnace) state;
			FurnaceInventory inv = furnace.getInventory();
			// If the furnace isn't currently active, stop the task to save lag
			if ((inv.getFuel() == null && furnace.getBurnTime() <= 0) || inv.getSmelting() == null || inv.getFuel().getAmount() == 0 || inv.getSmelting().getAmount() == 0 || (inv.getResult() != null && maxStack(inv.getResult()))) {
				int task = furnaceTasks.get(furnace.getLocation());
				furnaceTasks.remove(furnace.getLocation());
				Bukkit.getScheduler().cancelTask(task);
				return;
			}

			double c = carry.getOrDefault(furnace.getLocation(), 0D) + rate;
			carry.put(furnace.getLocation(), c % 1);

			furnace.setCookTime((short) (furnace.getCookTime() + c));
			furnace.setBurnTime((short) (furnace.getBurnTime() - ((int) c)));
			furnace.update();

		}, 1, 1).getTaskId());
	}

	private boolean maxStack(ItemStack item) {
		return item.getAmount() == item.getMaxStackSize();
	}
}
