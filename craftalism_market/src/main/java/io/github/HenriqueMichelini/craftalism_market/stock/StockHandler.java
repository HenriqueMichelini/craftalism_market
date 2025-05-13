package io.github.HenriqueMichelini.craftalism_market.stock;

import io.github.HenriqueMichelini.craftalism_economy.economy.util.MoneyFormat;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.stock.listener.StockUpdateListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockHandler {
    private static final Logger LOGGER = Logger.getLogger(StockHandler.class.getName());
    private final PriorityQueue<MarketItem> activeItemsQueue = new PriorityQueue<>(Comparator.comparingLong(MarketItem::getNextUpdateTime));
    private final Set<MarketItem> activeItemsSet = new HashSet<>();
    private final List<StockUpdateListener> listeners = new ArrayList<>();
    private final ConfigManager configManager;
    private final MoneyFormat moneyFormat;
    private final long DECIMAL_SCALE = MoneyFormat.DECIMAL_SCALE;

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
            if (item.getCurrentStock() == item.getBaseStock()) return;

            long intervalMillis = configManager.getStockUpdateInterval() * 1000L;
//            long intervalMillis = configManager.getStockUpdateInterval() * 60 * 1000L;
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

    private void logCalculateSafeAdjustment(String itemName, int base, int current, double stockRegenRate, int adjustment) {
        LOGGER.fine(() -> String.format(
                "Stock adjustment for %s: base=%d, current=%d, regenRate=%.2f, adjustment=%d",
                itemName, base, current, stockRegenRate, adjustment
        ));
    }

    private int calculateSafeAdjustment(MarketItem item, int base, int current) {
        double minRegen = 0.01;
        double regenRate = Math.max(item.getStockRegenRate(), minRegen);

        int delta = base - current;
        int maxAdjustment;

        if (delta > 0) {
            maxAdjustment = (int) Math.round(base * regenRate);
        } else {
            maxAdjustment = (int) Math.round(current * regenRate);
        }

        int absDelta = Math.abs(delta);
        int absAdjustment = Math.min(maxAdjustment, absDelta);
        absAdjustment = Math.max(1, absAdjustment);
        int adjustment = delta > 0 ? absAdjustment : -absAdjustment;

        logCalculateSafeAdjustment(item.getName(), base, current, item.getStockRegenRate(), adjustment);

        return adjustment;
    }

    private void updateStockAndPrice(MarketItem item, int base, int newStock) {
        int adjustment = newStock - item.getCurrentStock();
        long newPrice = calculateNewPrice(item, adjustment);

        newStock = clampStockToBounds(base, newStock);
        updateItemState(item, newStock, newPrice);
    }

    private long calculateNewPrice(MarketItem item, int adjustment) {
        if (adjustment == 0) return Math.max(100, item.getCurrentPrice());

        long reverseMultiplier = getReverseMultiplier(item, adjustment, adjustment > 0);
        LOGGER.fine("Reverse multiplier: " + reverseMultiplier);
        long newPrice = (item.getCurrentPrice() * reverseMultiplier) / DECIMAL_SCALE;

        return Math.max(100, newPrice);
    }

    private long getReverseMultiplier(MarketItem item, int adjustment, boolean isAddingStock) {
        long variation = item.getPriceVariationPerOperation();
        int steps = Math.abs(adjustment);

        variation = Math.max(variation, 1L);

        long transactionMultiplier = isAddingStock ?
                DECIMAL_SCALE + variation :
                DECIMAL_SCALE - variation;

        BigDecimal stepMultiplier = BigDecimal.valueOf(DECIMAL_SCALE)
                .divide(BigDecimal.valueOf(transactionMultiplier), 10, RoundingMode.HALF_UP);
        BigDecimal totalMultiplier = BigDecimal.valueOf(DECIMAL_SCALE);

        for (int i = 0; i < steps; i++) {
            totalMultiplier = totalMultiplier.multiply(stepMultiplier);
        }

        return totalMultiplier.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private int clampStockToBounds(int base, int newStock) {
        double maxOverflow = configManager.getMaxStockOverflow();
        int maxStock = (int) (base * maxOverflow);
        return Math.max(0, Math.min(newStock, maxStock));
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

    private void updateItemState(MarketItem item, int newStock, long newPrice) {
        int oldStock = item.getCurrentStock();
        long oldPrice = item.getCurrentPrice();
        item.setCurrentStock(newStock);
        item.setCurrentPrice(newPrice);
        item.getPriceHistory().add(newPrice);
        logStockUpdate(item, oldStock, oldPrice, newStock, newPrice);
        notifyStockUpdated(item);
    }

    private void logUpgradeBaseStock(String itemName, long oldBaseStock, long newBaseStock, long increaseNumber, long newVariation) {
        LOGGER.info(() -> String.format(
                "%s | Stock upgrade: %d → %d (+%d) | New Variation: %.4f%%",
                itemName,
                oldBaseStock,
                newBaseStock,
                increaseNumber,
                (newVariation / (double) DECIMAL_SCALE) * 100 // Correctly format as percentage
        ));
    }

    public void upgradeBaseStock(MarketItem item, int increaseNumber) {
        int oldBaseStock = item.getBaseStock();
        int newBaseStock = oldBaseStock + increaseNumber;
        long oldVariation = item.getPriceVariationPerOperation();

        long newVariation = (oldVariation * oldBaseStock) / newBaseStock;
        newVariation = Math.max(newVariation, 1L);

        item.setBaseStock(newBaseStock);
        item.setPriceVariationPerOperation(newVariation);

        logUpgradeBaseStock(item.getName(), oldBaseStock, newBaseStock, increaseNumber, newVariation);
    }
}