package io.github.HenriqueMichelini.craftalism_market.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;

public class InventoryManager {

    public InventoryManager() {}

    public static boolean hasItems(Player player, Material material, int requiredAmount) {
        return countItems(player, material) >= requiredAmount;
    }

    public static int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    public static boolean removeItemFromPlayer(Player player, Material material, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;

        for (ItemStack stack : inventory.getContents()) {
            if (stack != null && stack.getType() == material) {
                int stackAmount = stack.getAmount();

                if (stackAmount <= remaining) {
                    inventory.remove(stack);
                    remaining -= stackAmount;
                } else {
                    stack.setAmount(stackAmount - remaining);
                    remaining = 0;
                }

                if (remaining <= 0) break;
            }
        }
        return remaining == 0;
    }

    public static void addItemToPlayer(Player player, Material material, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;

        while (remaining > 0) {
            int stackSize = Math.min(remaining, material.getMaxStackSize());
            ItemStack stack = new ItemStack(material, stackSize);

            HashMap<Integer, ItemStack> leftovers = inventory.addItem(stack);
            if (!leftovers.isEmpty()) {
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                    remaining -= leftover.getAmount();
                }
            }
            remaining -= stackSize;
        }
    }
}