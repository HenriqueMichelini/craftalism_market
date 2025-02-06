package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.util.InventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.UUID;

public class Transaction {
    private final EconomyManager economyManager;
    private final DataLoader dataLoader;
    private final Player player;

    public Transaction(Player player, EconomyManager economyManager, DataLoader dataLoader) {
        if (economyManager == null) {
            throw new IllegalStateException("EconomyManager not initialized!");
        }

        this.player = player;
        this.economyManager = economyManager;
        this.dataLoader = dataLoader;
    }

    public boolean performBuyTransaction(String itemName, int amount) {
        UUID playerUUID = player.getUniqueId();
        MarketItem item = dataLoader.getMarketItems().get(itemName);

        if (item == null) {
            player.sendMessage(Component.text("Item not found!", NamedTextColor.RED));
            return false;
        }

        BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(amount));

        // Check balance and stock
        if (!economyManager.hasBalance(playerUUID, totalPrice)) {
            player.sendMessage(Component.text("You don't have enough money to buy " + amount + " of " + itemName + ". (must be $" + totalPrice + " but you only have $" + economyManager.getBalance(playerUUID), NamedTextColor.RED));
            return false;
        }

        if (item.getAmount() < amount) {
            player.sendMessage(Component.text("The amount selected (" + amount + ") is more than available in the stock (" + item.getAmount() + "). Buying all the stock.", NamedTextColor.GOLD));
            amount = item.getAmount();
            totalPrice = item.getPrice().multiply(BigDecimal.valueOf(amount));
        }

        // Perform transaction
        if (economyManager.withdraw(playerUUID, totalPrice)) {
            // Add items to player's inventory
            boolean success = InventoryManager.addItemToPlayer(player, item.getMaterial(), amount);

            if (!success) {
                // Refund if inventory is full
                economyManager.deposit(playerUUID, totalPrice);
                player.sendMessage(Component.text("There isn't enough inventory space to comport " + amount + " of " + itemName + ". The remaining was dropped in the floor.", NamedTextColor.GOLD));
            }

            // Update market stock (if needed)
            // item.setAmount(item.getAmount() - amount);
            // dataLoader.saveMarketItems();

            player.sendMessage(Component.text("Successfully purchased " + amount + " " + itemName + " for $" + totalPrice, NamedTextColor.GREEN));
            return true;
        }
        return false;
    }

    public boolean performSellTransaction(String itemName, int amount) {
        UUID playerUUID = player.getUniqueId();
        MarketItem item = dataLoader.getMarketItems().get(itemName);

        if (item == null) {
            player.sendMessage(Component.text("Item not found!", NamedTextColor.RED));
            return false;
        }

        // Check if player has enough items
        if (!InventoryManager.hasItems(player, item.getMaterial(), amount)) {
            int playerItems = InventoryManager.countItems(player, item.getMaterial());
            player.sendMessage(Component.text("The amount selected (" + amount + ") is more than available in your inventory (" + playerItems + "). Selling every " + itemName + " of your inventory.", NamedTextColor.GOLD));
            amount = playerItems;
        }

        BigDecimal totalEarnings = item.getPriceSell().multiply(BigDecimal.valueOf(amount));

        // Remove items from inventory
        boolean removed = InventoryManager.removeItemFromPlayer(player, item.getMaterial(), amount);
        if (!removed) {
            player.sendMessage(Component.text("Failed to remove items from inventory!", NamedTextColor.RED));
            return false;
        }

        // Deposit money
        economyManager.deposit(playerUUID, totalEarnings);

        // Update market stock (if needed)
        // item.setAmount(item.getAmount() + amount);
        // dataLoader.saveMarketItems();

        player.sendMessage(Component.text("Successfully sold " + amount + " " + itemName + " for $" + totalEarnings, NamedTextColor.GREEN));
        return true;
    }
}