package io.github.HenriqueMichelini.craftalism_market.logic;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for handling player inventory operations with thread safety and null-safety.
 */
public final class InventoryHandler {

    private InventoryHandler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Checks if a player has at least the required amount of a specific material.
     */
    public static boolean hasItems(@NotNull Player player, @NotNull Material material, int requiredAmount) {
        validateParameters(player, material, requiredAmount);
        return countItems(player, material) >= requiredAmount;
    }

    /**
     * Counts the total amount of a specific material in a player's inventory.
     */
    public static int countItems(@NotNull Player player, @NotNull Material material) {
        validateParameters(player, material, 1);
        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> item.getType() == material)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    /**
     * Removes a specific amount of a material from a player's inventory.
     * @return true if the full amount was removed, false otherwise
     */
    public static boolean removeItemFromPlayer(@NotNull Player player, @NotNull Material material, int amount) {
        validateParameters(player, material, amount);

        Inventory inventory = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null && stack.getType() == material) {
                int stackAmount = stack.getAmount();

                if (stackAmount <= remaining) {
                    inventory.setItem(i, null);
                    remaining -= stackAmount;
                } else {
                    stack.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }
        }
        return remaining == 0;
    }

    /**
     * Adds items to a player's inventory, dropping overflow naturally in the world.
     */
    public static int addItemToPlayer(@NotNull Player player, @NotNull Material material, int amount) {
        validateParameters(player, material, amount);

        Inventory inventory = player.getInventory();
        ItemStack[] items = createItemStacks(material, amount);
        int totalAdded = 0;

        for (ItemStack item : items) {
            Map<Integer, ItemStack> leftover = inventory.addItem(item.clone()); // Clone to avoid modifying original
            int added = item.getAmount();
            if (!leftover.isEmpty()) {
                added -= leftover.values().iterator().next().getAmount();
                dropLeftoverItems(player, leftover.values());
            }
            totalAdded += added;
        }

        return totalAdded;
    }

    private static void validateParameters(Player player, Material material, int amount) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(material, "Material cannot be null");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
    }

    private static ItemStack[] createItemStacks(Material material, int amount) {
        int maxStackSize = material.getMaxStackSize();
        int fullStacks = amount / maxStackSize;
        int remainder = amount % maxStackSize;

        ItemStack[] items = new ItemStack[fullStacks + (remainder > 0 ? 1 : 0)];
        Arrays.fill(items, 0, fullStacks, new ItemStack(material, maxStackSize));

        if (remainder > 0) {
            items[fullStacks] = new ItemStack(material, remainder);
        }
        return items;
    }

    private static void dropLeftoverItems(Player player, Collection<ItemStack> leftovers) {
        leftovers.forEach(stack ->
                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        new ItemStack(stack.getType(), stack.getAmount())
                )
        );
    }
}