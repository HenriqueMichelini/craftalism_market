package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public class Transaction {
    private final Player player;
    private final ItemStack item;
    private final BigDecimal pricePerUnit;
    private final int itemAmount;
    private final EconomyManager economyManager;

    // 1. Single EconomyManager instance
    public Transaction(Player player, ItemStack item, BigDecimal pricePerUnit,
                       int itemAmount, EconomyManager economyManager) {
        this.player = player;
        this.item = item;
        this.pricePerUnit = pricePerUnit;
        this.itemAmount = itemAmount;
        this.economyManager = economyManager;
    }

    public boolean performBuy() {
        // 2. Check inventory space first
        if (!hasInventorySpace(player, item.getType(), itemAmount)) {
            sendMessage("Not enough inventory space!", NamedTextColor.RED);
            return false;
        }

        BigDecimal totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(itemAmount));

        // 3. Use proper economy manager instance
        BigDecimal playerBalance = economyManager.getBalance(player.getUniqueId());
        if (playerBalance.compareTo(totalPrice) < 0) {
            sendMessage("You don't have enough money!", NamedTextColor.RED);
            return false;
        }

        // 4. Deduct before adding items
        economyManager.withdraw(player.getUniqueId(), totalPrice);

        ItemStack itemsToAdd = new ItemStack(item.getType(), itemAmount);
        player.getInventory().addItem(itemsToAdd).values().forEach(leftover -> {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        });

        // 5. Use formatted components
        sendMessage("Purchased " + itemAmount + " " + getItemName() + " for $" + totalPrice,
                NamedTextColor.GREEN);
        return true;
    }

    public boolean performSell() {
        int availableAmount = countItemsInInventory();
        if (availableAmount < itemAmount) {
            sendMessage("You only have " + availableAmount + " " + getItemName() + " to sell!",
                    NamedTextColor.RED);
            return false;
        }

        BigDecimal totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(itemAmount));
        removeItemsFromInventory(itemAmount);

        economyManager.deposit(player.getUniqueId(), totalPrice);
        sendMessage("Sold " + itemAmount + " " + getItemName() + " for $" + totalPrice,
                NamedTextColor.GREEN);
        return true;
    }

    private boolean hasInventorySpace(Player player, Material material, int amount) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }
        return (amount / material.getMaxStackSize()) <= emptySlots;
    }

    private String getItemName() {
        return item.getItemMeta().hasDisplayName() ?
                item.getItemMeta().getDisplayName() :
                item.getType().toString().toLowerCase().replace("_", " ");
    }

    private int countItemsInInventory() {
        return player.getInventory().all(item.getType())
                .values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    private void removeItemsFromInventory(int amount) {
        player.getInventory().removeItemAnySlot(new ItemStack(item.getType(), amount));
    }

    private void sendMessage(String message, NamedTextColor color) {
        player.sendMessage(
                Component.text(message)
                        .color(color)
                        .hoverEvent(Component.text("Transaction details"))
        );
    }
}