package io.github.HenriqueMichelini.craftalism_market.stock;

import io.github.HenriqueMichelini.craftalism_economy.economy.util.MoneyFormat;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.stock.listener.StockUpdateListener;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.HenriqueMichelini.craftalism_economy.economy.util.MoneyFormat.DECIMAL_SCALE;

public class StockHandler {
    private static final Logger LOGGER = Logger.getLogger(StockHandler.class.getName());
    private final PriorityQueue<MarketItem> activeItemsQueue = new PriorityQueue<>(Comparator.comparingLong(MarketItem::getNextUpdateTime));
    private final Set<MarketItem> activeItemsSet = new HashSet<>();
    private final List<StockUpdateListener> listeners = new ArrayList<>();
    private final ConfigManager configManager;
    private final MoneyFormat moneyFormat;

    private static final long MINUTE_IN_MILLIS = 60 * 1000L;

    public StockHandler(ConfigManager configManager, MoneyFormat moneyFormat) {
        this.configManager = Objects.requireNonNull(configManager, "ConfigManager cannot be null");
        this.moneyFormat = moneyFormat;
        if (configManager.getStockUpdateInterval() <= 0) {
            LOGGER.warning("Invalid stock update interval, using default of 5 minutes");
            configManager.setStockUpdateInterval(5);
        }
        initializeActiveItems();
    }

    private void initializeActiveItems() {
        long now = System.currentTimeMillis();
        long intervalMillis = configManager.getStockUpdateInterval() * MINUTE_IN_MILLIS;

        configManager.getItems().values().forEach(item -> {
            if (item.getCurrentStock() != item.getBaseStock()) {
                long storedNextUpdateTime = item.getNextUpdateTime();
                if (storedNextUpdateTime < now) {
                    processMissedIntervals(item, now, intervalMillis);
                    if (item.getCurrentStock() != item.getBaseStock()) {
                        markItemForUpdate(item);
                    }
                } else {
                    item.setNextUpdateTime(storedNextUpdateTime);
                    activeItemsQueue.add(item);
                    activeItemsSet.add(item);
                }
            }
        });
    }

    private void processMissedIntervals(MarketItem item, long now, long intervalMillis) {
        int delta = item.getBaseStock() - item.getCurrentStock();
        if (delta == 0) return;

        long overdueMillis = now - item.getNextUpdateTime();
        int intervalsPassed = (int) (overdueMillis / intervalMillis) + 1;

        for (int i = 0; i < intervalsPassed; i++) {
            if (item.getCurrentStock() == item.getBaseStock()) break;
            processItemStock(item);
        }
    }

    public void addStockUpdateListener(StockUpdateListener listener) {
        listeners.add(listener);
    }

    private void notifyStockUpdated(MarketItem item) {
        new ArrayList<>(listeners).forEach(listener -> listener.onStockUpdated(item));
    }

    public void getUpdateIntervalMinutes() {
        configManager.getStockUpdateInterval();
    }

    public void markItemForUpdate(MarketItem item) {
        synchronized (activeItemsSet) {
            // Allow activation when currentStock != baseStock (even if currentStock > baseStock)
            if (item.getCurrentStock() == item.getBaseStock()) return;

            long intervalMillis = configManager.getStockUpdateInterval() * 60 * 1000L;
            long nextUpdate = System.currentTimeMillis() + intervalMillis;

            if (activeItemsSet.contains(item)) {
                activeItemsQueue.remove(item);
                activeItemsSet.remove(item);
            }

            item.setNextUpdateTime(nextUpdate);
            activeItemsQueue.add(item);
            activeItemsSet.add(item);
        }
    }

    public void processAllActiveItems() {
        long now = System.currentTimeMillis();
        while (!activeItemsQueue.isEmpty()) {
            MarketItem item = activeItemsQueue.peek();
            if (item.getNextUpdateTime() > now + (configManager.getStockUpdateInterval() * MINUTE_IN_MILLIS * 2)) {
                activeItemsQueue.remove(item);
                activeItemsSet.remove(item);
                markItemForUpdate(item);
                continue;
            }

            if (item.getNextUpdateTime() > now) break;

            activeItemsQueue.poll();
            activeItemsSet.remove(item);
            try {
                processItemStock(item);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing stock for item: " + item.getName(), e);
            }

            if (item.getCurrentStock() != item.getBaseStock()) {
                markItemForUpdate(item);
            }
        }
    }

    private void processItemStock(MarketItem item) {
        int base = item.getBaseStock();
        int current = item.getCurrentStock();

        if (current == base || base == 0) return;

        int adjustment = calculateSafeAdjustment(item, base, current);
        int newStock = current + adjustment;

        updateStockAndPrice(item, base, newStock);
    }

    private int calculateSafeAdjustment(MarketItem item, int base, int current) {
        double minRegen = 0.01;
        double regenRate = Math.max(item.getStockRegenRate(), minRegen);

        int maxAdjustment = (int) Math.round(base * regenRate);
        int delta = base - current;

        // Handle overflow (currentStock > baseStock)
        if (current > base) {
            delta = current - base; // Positive delta for reduction
            maxAdjustment = (int) Math.round(current * regenRate); // Use current as base for overflow
        }

        int adjustment = Integer.signum(delta) * Math.max(1, Math.min(maxAdjustment, Math.abs(delta)));

        LOGGER.fine(() -> String.format(
                "Stock adjustment for %s: base=%d, current=%d, regenRate=%.2f, adjustment=%d",
                item.getName(), base, current, item.getStockRegenRate(), adjustment
        ));

        return adjustment;
    }

    private void updateStockAndPrice(MarketItem item, int base, int newStock) {
        int adjustment = newStock - item.getCurrentStock();
        long newPrice = calculateNewPrice(item, adjustment);

        // Handle stock bounds and base price reset
        newStock = clampStockToBounds(base, newStock);
        if (newStock == base) {
            newPrice = item.getBasePrice();
        }

        updateItemState(item, newStock, newPrice);
    }

    private long calculateNewPrice(MarketItem item, int adjustment) {
        if (adjustment == 0) return item.getCurrentPrice();

        boolean isAddingStock = adjustment > 0;
        long reverseMultiplier = getReverseMultiplier(item, adjustment, isAddingStock);

        // Apply geometric progression formula matching getLastPriceOfItem()
        long newPrice = (item.getCurrentPrice() * reverseMultiplier) / (long) Math.pow(DECIMAL_SCALE, Math.abs(adjustment));

        // Ensure minimum price of 1 (representing 0.01 when scaled)
        return Math.max(1, newPrice);
    }

    private long getReverseMultiplier(MarketItem item, int adjustment, boolean isAddingStock) {
        long variation = item.getPriceVariationPerOperation();
        int steps = Math.abs(adjustment);

        // Get transaction multipliers matching MarketUtils logic
        long transactionMultiplier = isAddingStock ?
                DECIMAL_SCALE + variation :  // Reverse buy multiplier
                DECIMAL_SCALE - variation;   // Reverse sell multiplier

        // Calculate inverse multiplier with scaling
        return (long) Math.pow(DECIMAL_SCALE, 2 * steps) /
                (long) Math.pow(transactionMultiplier, steps);
    }

    private int clampStockToBounds(int base, int newStock) {
        double maxOverflow = configManager.getMaxStockOverflow();
        int maxStock = (int) (base * maxOverflow);
        return Math.max(0, Math.min(newStock, maxStock));
    }

    private void updateItemState(MarketItem item, int newStock, long newPrice) {
        int oldStock = item.getCurrentStock();
        long oldPrice = item.getCurrentPrice();
        item.setCurrentStock(newStock);
        item.setCurrentPrice(newPrice);
        item.getPriceHistory().add(newPrice);
        logStockUpdate(item, oldStock, oldPrice, newStock, newPrice);
        notifyStockUpdated(item);
    }

    private void logStockUpdate(MarketItem item, int oldStock, long oldPrice, int newStock, long newPrice) {
        LOGGER.info(() -> String.format(
                "%s | Stock: %d → %d | Price: %s → %s",
                item.getName(),
                oldStock,
                newStock,
                moneyFormat.formatPrice(oldPrice),
                moneyFormat.formatPrice(newPrice)
        ));
    }

    public void upgradeBaseStock(MarketItem item, int increaseNumber) {
        int baseStock = item.getBaseStock();
        item.setBaseStock(baseStock + increaseNumber);
        LOGGER.info(() -> String.format(
                "%s | Stock upgrade: %d → %d (+%d @ %.1f%%)", // Show % from config
                item.getName(),
                baseStock,
                item.getBaseStock(),
                increaseNumber,
                configManager.getStockIncreasePercentage() * 100 // Display as percentage
        ));
    }
}