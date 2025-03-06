package io.github.HenriqueMichelini.craftalism_market.core;

import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.logic.DataLoader;
import io.github.HenriqueMichelini.craftalism_market.logic.InventoryManager;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketManager;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
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
        if (economyManager == null || marketManager == null || dataLoader == null || player == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        this.economyManager = economyManager;
        this.marketManager = marketManager;
        this.dataLoader = dataLoader;
        this.player = player;
    }

    public boolean performBuyTransaction(String itemName, int requestedAmount) {
        MarketItem item = getItemOrSendError(itemName);
        if (item == null) return false;

        int adjustedAmount = adjustAmountBasedOnStock(item, requestedAmount);
        BigDecimal totalPrice = calculateTotalPrice(item, adjustedAmount, true);

        if (!checkBalance(totalPrice, itemName, adjustedAmount)) return false;

        if (!processPurchase(item, adjustedAmount, totalPrice)) return false;

        updateMarketItemPriceAndStock(item, adjustedAmount, true);
        sendSuccess("Successfully purchased %d %s for %s".formatted(adjustedAmount, item.getName(), formatPrice(totalPrice)));
        return true;
    }

    public boolean performSellTransaction(String itemName, int requestedAmount) {
        MarketItem item = getItemOrSendError(itemName);
        if (item == null) return false;

        int adjustedAmount = adjustAmountBasedOnInventory(item, requestedAmount);
        if (adjustedAmount <= 0) return false;

        if (!removeItemsFromInventory(item, adjustedAmount)) return false;

        TransactionResult result = calculateTransactionResult(item, adjustedAmount);
        completeSellTransaction(item, adjustedAmount, result);
        sendSuccess("Successfully sold %d %s for %s. Tax of %s deducted."
                .formatted(adjustedAmount, item.getName(), formatPrice(result.earningsAfterTax()), formatPrice(result.tax())));
        return true;
    }

    private MarketItem getItemOrSendError(String itemName) {
        MarketItem item = dataLoader.getMarketItems().get(itemName);
        if (item == null) sendError("Item not found!");
        return item;
    }

    private int adjustAmountBasedOnStock(MarketItem item, int requestedAmount) {
        int availableStock = item.getAmount();
        if (availableStock == 0) {
            sendError("There is no stock available");
            return 0;
        }
        if (availableStock >= requestedAmount) return requestedAmount;

        sendWarning("The amount selected (%d) exceeds stock (%d). Buying only %d."
                .formatted(requestedAmount, availableStock, availableStock));
        return availableStock;
    }

    private BigDecimal calculateTotalPrice(MarketItem item, int amount, boolean isBuy) {
        return marketManager.getTotalPriceOfItem(item, amount, isBuy);
    }

    private boolean checkBalance(BigDecimal totalPrice, String itemName, int amount) {
        UUID playerId = player.getUniqueId();
        if (economyManager.hasBalance(playerId, totalPrice)) return true;

        sendError("Insufficient funds for %d %s. Needed: %s, Available: %s"
                .formatted(amount, itemName, formatPrice(totalPrice), formatPrice(economyManager.getBalance(playerId))));
        return false;
    }

    private boolean processPurchase(MarketItem item, int amount, BigDecimal totalPrice) {
        UUID playerId = player.getUniqueId();
        if (!economyManager.withdraw(playerId, totalPrice)) {
            //sendError("Failed to process payment");
            return false;
        }

        if (!InventoryManager.addItemToPlayer(player, item.getMaterial(), amount)) {
            economyManager.deposit(playerId, totalPrice);
            sendWarning("Inventory full for %d %s. Dropping the exceeds.".formatted(amount, item.getName()));
        }
        return true;
    }

    private void updateMarketItemPriceAndStock(MarketItem item, int soldAmount, boolean isBuy) {
        BigDecimal lastPrice = marketManager.getLastPriceOfItem(item, soldAmount, isBuy);
        item.setBasePrice(lastPrice);
        item.setAmount(item.getAmount() + (isBuy ? -soldAmount : soldAmount));
        marketManager.updatePriceHistory(item, lastPrice);
    }

    private int adjustAmountBasedOnInventory(MarketItem item, int requestedAmount) {
        int playerItems = InventoryManager.countItems(player, item.getMaterial());
        if (playerItems <= 0) {
            sendError("You have no %s to sell".formatted(item.getName()));
            return 0;
        }

        int adjustedAmount = Math.min(requestedAmount, playerItems);
        if (adjustedAmount < requestedAmount) {
            sendWarning("Attempting to sell %d, only %d available".formatted(requestedAmount, playerItems));
        }
        return adjustedAmount;
    }

    private boolean removeItemsFromInventory(MarketItem item, int amount) {
        if (!InventoryManager.removeItemFromPlayer(player, item.getMaterial(), amount)) {
            sendError("Failed to remove items from inventory");
            return false;
        }
        return true;
    }

    private TransactionResult calculateTransactionResult(MarketItem item, int amount) {
        BigDecimal totalBeforeTax = calculateTotalPrice(item, amount, false);
        BigDecimal tax = totalBeforeTax.multiply(item.getSellTax())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal earningsAfterTax = totalBeforeTax.subtract(tax);
        return new TransactionResult(earningsAfterTax, tax);
    }

    private void completeSellTransaction(MarketItem item, int amount, TransactionResult result) {
        economyManager.deposit(player.getUniqueId(), result.earningsAfterTax());
        updateMarketItemPriceAndStock(item, amount, false);
    }

    private String formatPrice(BigDecimal price) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
        currencyFormat.setMinimumFractionDigits(2);
        return currencyFormat.format(price);
    }

    private void sendError(String message) {
        player.sendMessage(Component.text(message, NamedTextColor.RED));
    }

    private void sendWarning(String message) {
        player.sendMessage(Component.text(message, NamedTextColor.GOLD));
    }

    private void sendSuccess(String message) {
        player.sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    private record TransactionResult(BigDecimal earningsAfterTax, BigDecimal tax) {}
}