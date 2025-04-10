package io.github.HenriqueMichelini.craftalism_market.core;

import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.logic.InventoryHandler;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketUtils;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.stock.StockHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class TransactionHandler {
    private final EconomyManager economyManager;
    private final MarketUtils marketUtils;
    private final ConfigManager configManager;
    private final Player player;
    private final StockHandler stockHandler; // Add this field

    public TransactionHandler(Player player, EconomyManager economyManager, ConfigManager configManager, MarketUtils marketUtils, StockHandler stockHandler) {
        this.stockHandler = stockHandler;
        if (economyManager == null || marketUtils == null || configManager == null || player == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        this.economyManager = economyManager;
        this.marketUtils = marketUtils;
        this.configManager = configManager;
        this.player = player;
    }

    public boolean performBuyTransaction(String itemName, int requestedAmount) {
        MarketItem item = getItemOrSendError(itemName);
        if (item == null) return false;

        int adjustedAmount = adjustAmountBasedOnStock(item, requestedAmount);
        BigDecimal totalPrice = calculateTotalPrice(item, adjustedAmount, true);

        if (!checkBalance(totalPrice, itemName, adjustedAmount)) return false;
        if (!processPurchase(item, adjustedAmount, totalPrice)) return false;

        // Update base stock based on actual added amount (now handled in processPurchase)
        double increasePercent = configManager.getStockIncreasePercentage();
        int increaseNumber = (int) (adjustedAmount * increasePercent);
        int currentBaseStock = item.getBaseStock();
        int newBaseStock = currentBaseStock + increaseNumber;

        marketUtils.updateTaxRate(item, currentBaseStock, newBaseStock, item.getTaxRate());
        stockHandler.upgradeBaseStock(item, increaseNumber);

        sendSuccess("Successfully purchased %d %s for %s"
                .formatted(adjustedAmount, item.getName(), formatPrice(totalPrice)));
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
        MarketItem item = configManager.getItems().get(itemName);
        if (item == null) sendError("Item not found!");
        return item;
    }

    private int adjustAmountBasedOnStock(MarketItem item, int requestedAmount) {
        int availableStock = item.getCurrentStock();
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
        return marketUtils.getTotalPriceOfItem(item, amount, isBuy);
    }

    private boolean checkBalance(BigDecimal totalPrice, String itemName, int amount) {
        UUID playerId = player.getUniqueId();
        if (economyManager.hasBalance(playerId, totalPrice)) return true;

        sendError("Insufficient funds for %d %s. Needed: %s, Available: %s"
                .formatted(amount, itemName, formatPrice(totalPrice), formatPrice(economyManager.getBalance(playerId))));
        return false;
    }

    private boolean processPurchase(MarketItem item, int requestedAmount, BigDecimal totalPrice) {
        UUID playerId = player.getUniqueId();

        // Attempt to add items first (temporarily)
        int actualAdded = InventoryHandler.addItemToPlayer(player, item.getMaterial(), requestedAmount);
        if (actualAdded <= 0) {
            sendError("No space in inventory!");
            return false;
        }

        // Calculate price for the actual added amount
        BigDecimal pricePerItem = totalPrice.divide(
                BigDecimal.valueOf(requestedAmount),
                10,
                RoundingMode.HALF_UP
        );
        BigDecimal actualPrice = pricePerItem.multiply(BigDecimal.valueOf(actualAdded));

        // Withdraw only the actual price
        if (!economyManager.withdraw(playerId, actualPrice)) {
            // Remove items if payment fails
            InventoryHandler.removeItemFromPlayer(player, item.getMaterial(), actualAdded);
            sendError("Payment failed after partial item addition!");
            return false;
        }

        // Update stock and price based on actualAdded
        updateMarketItemPriceAndStock(item, actualAdded, true);

        // Handle leftover items (if any)
        int leftover = requestedAmount - actualAdded;
        if (leftover > 0) {
            sendWarning("Only %d %s fit in your inventory. %d were refunded."
                    .formatted(actualAdded, item.getName(), leftover));
        }

        return true;
    }

    public void updateMarketItemPriceAndStock(MarketItem item, int soldAmount, boolean isBuy) {
        BigDecimal lastPrice = marketUtils.getLastPriceOfItem(item, soldAmount, isBuy);
        item.setCurrentPrice(lastPrice);
        item.setCurrentStock(item.getCurrentStock() + (isBuy ? -soldAmount : soldAmount));
        marketUtils.updatePriceHistory(item, lastPrice);
        stockHandler.markItemForUpdate(item);
    }

    private int adjustAmountBasedOnInventory(MarketItem item, int requestedAmount) {
        int playerItems = InventoryHandler.countItems(player, item.getMaterial());
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
        if (!InventoryHandler.removeItemFromPlayer(player, item.getMaterial(), amount)) {
            sendError("Failed to remove items from inventory");
            return false;
        }
        return true;
    }

    private TransactionResult calculateTransactionResult(MarketItem item, int amount) {
        BigDecimal totalBeforeTax = calculateTotalPrice(item, amount, false);
        BigDecimal tax = totalBeforeTax.multiply(item.getTaxRate())
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