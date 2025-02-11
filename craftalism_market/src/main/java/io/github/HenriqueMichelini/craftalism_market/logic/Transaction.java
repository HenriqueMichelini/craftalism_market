package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class Transaction {
    private final EconomyManager economyManager;
    private final MarketManager marketManager;
    private final DataLoader dataLoader;
    private final Player player;

    public Transaction(Player player, EconomyManager economyManager, DataLoader dataLoader, MarketManager marketManager) {
        if (economyManager == null) {
            throw new IllegalStateException("EconomyManager not initialized!");
        }

        this.economyManager = economyManager;
        this.marketManager = marketManager;
        this.dataLoader = dataLoader;
        this.player = player;
    }

    public boolean performBuyTransaction(String itemName, int amount) {
        UUID playerUUID = player.getUniqueId();
        MarketItem item = dataLoader.getMarketItems().get(itemName);

        if (item == null) {
            player.sendMessage(Component.text("Item not found!", NamedTextColor.RED));
            return false;
        }

        BigDecimal totalPrice = marketManager.getTotalPriceOfItem(item, amount, true);

        // Check balance and stock
        if (!economyManager.hasBalance(playerUUID, totalPrice)) {
            player.sendMessage(Component.text("You don't have enough money to buy " + amount + " of " + itemName + ". (must be " + formatPrice(totalPrice) + " but you only have $" + economyManager.getBalance(playerUUID), NamedTextColor.RED));
            return false;
        }

        if (item.getAmount() < amount) {
            player.sendMessage(Component.text("The amount selected (" + amount + ") is more than available in the stock (" + item.getAmount() + "). Buying all the stock.", NamedTextColor.GOLD));
            amount = item.getAmount();
            totalPrice = marketManager.getTotalPriceOfItem(item, amount, true);
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

            player.sendMessage(Component.text("Successfully purchased " + amount + " " + itemName + " for " + formatPrice(totalPrice), NamedTextColor.GREEN));

            //  Set the last price negotiated
            BigDecimal lastPriceOfItem = marketManager.getLastPriceOfItem(item, amount, true);

            item.setBasePrice(lastPriceOfItem);
            marketManager.updatePriceHistory(item, lastPriceOfItem);

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

        int playerItems = InventoryManager.countItems(player, item.getMaterial());

        if (playerItems <= 0) {
            player.sendMessage(Component.text("You don't have any of " + itemName, NamedTextColor.RED));
            return false;
        }

        // Check if player has enough items
        if (!InventoryManager.hasItems(player, item.getMaterial(), amount)) {
            player.sendMessage(Component.text("The amount selected (" + amount + ") is more than available in your inventory (" + playerItems + "). Selling every " + itemName + " of your inventory.", NamedTextColor.GOLD));
            amount = playerItems;
        }

        // Remove items from inventory
        boolean removed = InventoryManager.removeItemFromPlayer(player, item.getMaterial(), amount);

        if (!removed) {
            player.sendMessage(Component.text("Failed to remove items from inventory!", NamedTextColor.RED));
            return false;
        }

        BigDecimal totalEarningsBeforeTax = marketManager.getTotalPriceOfItem(item, amount, false);
        BigDecimal taxPercentage = item.getSellTax();
        BigDecimal totalEarningsAfterTax = totalEarningsBeforeTax.multiply(BigDecimal.ONE.subtract(taxPercentage));
        BigDecimal tax = totalEarningsBeforeTax.subtract(totalEarningsAfterTax)
                .setScale(2, RoundingMode.HALF_UP);

        // Deposit money
        economyManager.deposit(playerUUID, totalEarningsAfterTax);
        item.setBasePrice(marketManager.getLastPriceOfItem(item, amount, false));

        player.sendMessage(Component.text("Successfully sold " + amount + " " + itemName + " for " + formatPrice(totalEarningsAfterTax) + ". Tax of " + formatPrice(tax) + " deducted.", NamedTextColor.GREEN));

        return true;
    }

    private String formatPrice(BigDecimal price) {
        // Use BigDecimal's scaling instead:
        price = price.setScale(2, RoundingMode.HALF_UP);
        return "$" + price.toPlainString();
    }
}