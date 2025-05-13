package io.github.HenriqueMichelini.craftalism_market.core;
import io.github.HenriqueMichelini.craftalism_economy.economy.managers.EconomyManager;
import io.github.HenriqueMichelini.craftalism_economy.economy.util.MoneyFormat;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.logic.InventoryHandler;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketMath;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.stock.StockHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TransactionHandler {
    private static final long DECIMAL_SCALE = MoneyFormat.DECIMAL_SCALE;
    private final MoneyFormat moneyFormat;
    private final EconomyManager economyManager;
    private final MarketMath marketMath;
    private final ConfigManager configManager;
    private final Player player;
    private final StockHandler stockHandler;

    public TransactionHandler(
            MoneyFormat moneyFormat,
            Player          player,
            EconomyManager  economyManager,
            ConfigManager   configManager,
            MarketMath      marketMath,
            StockHandler    stockHandler
    )
    {
        if (marketMath == null || configManager == null || player == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        this.moneyFormat        = moneyFormat;
        this.economyManager     = economyManager;
        this.stockHandler       = stockHandler;
        this.marketMath         = marketMath;
        this.configManager      = configManager;
        this.player             = player;
    }

    public boolean performBuyTransaction(String itemName, int requestedAmount) {
        MarketItem item = getItemOrSendError(itemName);
        if (item == null) return false;

        int adjustedAmount = adjustAmount(item, requestedAmount);
        if (!processPurchase(item, adjustedAmount)) return false;

        updateMarketItemPriceAndStock(item, adjustedAmount, true);
        parseUpgradeBaseStockValues(item, requestedAmount);

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
                .formatted(adjustedAmount, item.getName(),
                        moneyFormat.formatPrice(result.earningsAfterTax()),
                        moneyFormat.formatPrice((long) result.tax())));
        return true;
    }

    private int adjustAmountBasedOnStockAvailability(MarketItem item, int requestedAmount) {
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

    public int adjustAmount(MarketItem item, int amount) {
        int adjustedAmount = adjustAmountBasedOnStockAvailability(item, amount);
        return InventoryHandler.addItemToPlayer(player, item.getMaterial(), adjustedAmount);
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

    public void parseUpgradeBaseStockValues(MarketItem item, int amount) {
        double increasePercent = configManager.getStockIncreasePercentage();
        int increaseNumber = (int) (amount * increasePercent);

        stockHandler.upgradeBaseStock(item, increaseNumber);
    }

    public void updateMarketItemPriceAndStock(MarketItem item, int soldAmount, boolean isBuy) {
        long lastPrice = marketMath.getLastPriceOfItem(item, soldAmount, isBuy);
        item.setCurrentPrice(lastPrice);
        item.setCurrentStock(item.getCurrentStock() + (isBuy ? -soldAmount : soldAmount));
        marketMath.updatePriceHistory(item, lastPrice);
        stockHandler.markItemForUpdate(item);
    }

    private boolean removeItemsFromInventory(MarketItem item, int amount) {
        if (!InventoryHandler.removeItemFromPlayer(player, item.getMaterial(), amount)) {
            sendError("Failed to remove items from inventory");
            return false;
        }
        return true;
    }

    private MarketItem getItemOrSendError(String itemName) {
        MarketItem item = configManager.getItems().get(itemName);
        if (item == null) sendError("Item not found!");
        return item;
    }

    private TransactionResult calculateTransactionResult(MarketItem item, int amount) {
        long totalBeforeTax = marketMath.getTotalPriceOfItem(item, amount, false);
        double tax = totalBeforeTax * item.getTaxRate();
        long earningsAfterTax = (long) (totalBeforeTax - tax);
        return new TransactionResult(earningsAfterTax, tax);
    }

    private void completeSellTransaction(MarketItem item, int amount, TransactionResult result) {
        economyManager.deposit(player.getUniqueId(), result.earningsAfterTax());
        updateMarketItemPriceAndStock(item, amount, false);
        parseUpgradeBaseStockValues(item, amount);
    }

    private boolean processPurchase(MarketItem item, int amount) {
        UUID playerId = player.getUniqueId();

        long totalPrice = marketMath.getTotalPriceOfItem(item, amount, true);

        if (!economyManager.withdraw(playerId, totalPrice)) {
            InventoryHandler.removeItemFromPlayer(player, item.getMaterial(), amount);
            sendError("Payment failed after partial item addition!");
            return false;
        }

        sendSuccess("Successfully purchased %d %s for %s".formatted(amount, item.getName(), moneyFormat.formatPrice(totalPrice)));

        return true;
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

    private record TransactionResult(long earningsAfterTax, double tax) {}
}