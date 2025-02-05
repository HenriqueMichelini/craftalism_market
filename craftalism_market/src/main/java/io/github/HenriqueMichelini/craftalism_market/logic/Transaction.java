package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Map;
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

    public void performBuyTransaction(String itemName, int amount) {
        UUID playerUUID = player.getUniqueId();
        MarketItem item = dataLoader.getMarketItems().get(itemName);

        if (item == null) {
            player.sendMessage("§cItem not found!");
            return;
        }

        BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(amount));

        // Check balance and stock
        if (!economyManager.hasBalance(playerUUID, totalPrice)) {
            player.sendMessage("§cInsufficient funds!");
            return;
        }

        if (item.getAmount() < amount) {
            player.sendMessage("§cNot enough stock available!");
            return;
        }

        // Perform transaction
        if (economyManager.withdraw(playerUUID, totalPrice)) {
            // Add items to player's inventory
            ItemStack itemStack = new ItemStack(item.getMaterial(), amount);
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(itemStack);

            if (!remaining.isEmpty()) {
                // Refund if inventory is full
                economyManager.deposit(playerUUID, totalPrice);
                player.sendMessage("§cNot enough inventory space!");
                return;
            }

            // Update market stock
//            item.setAmount(item.getAmount() - amount);
//            dataLoader.saveMarketItems();

            player.sendMessage("§aSuccessfully purchased " + amount + " " + itemName + " for $" + totalPrice);
        }
    }

    public void performSellTransaction(String itemName, int amount) {
        UUID playerUUID = player.getUniqueId();
        MarketItem item = dataLoader.getMarketItems().get(itemName);

        if (item == null) {
            player.sendMessage("§cItem not found!");
            return;
        }

        // Check if player has enough items
        if (!player.getInventory().containsAtLeast(new ItemStack(item.getMaterial()), amount)) {
            player.sendMessage("§cYou don't have enough items to sell!");
            return;
        }

        BigDecimal totalEarnings = item.getPrice().multiply(BigDecimal.valueOf(amount));

        // Remove items from inventory
        player.getInventory().removeItem(new ItemStack(item.getMaterial(), amount));

        // Deposit money
        economyManager.deposit(playerUUID, totalEarnings);

//        // Update market stock
//        item.setAmount(item.getAmount() + amount);
//        dataLoader.saveMarketItems();

        player.sendMessage("§aSuccessfully sold " + amount + " " + itemName + " for $" + totalEarnings);
    }
}