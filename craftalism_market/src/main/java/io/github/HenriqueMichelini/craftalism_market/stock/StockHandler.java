package io.github.HenriqueMichelini.craftalism_market.stock;

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

    private static final long MINUTE_IN_MILLIS = 60 * 1000L;
    private static final double MIN_REGEN_RATE = 0.01;

    public StockHandler(ConfigManager configManager) {
        this.configManager = Objects.requireNonNull(configManager, "ConfigManager cannot be null");
        // Validate interval
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
                    // Process missed intervals
                    processMissedIntervals(item, now, intervalMillis);
                    // Schedule next update if needed
                    if (item.getCurrentStock() != item.getBaseStock()) {
                        markItemForUpdate(item);
                    }
                } else {
                    // Schedule normally
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
            // Check if adjustment is still needed
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

    public int getUpdateIntervalMinutes() {
        return configManager.getStockUpdateInterval();
    }

    public void markItemForUpdate(MarketItem item) {
        synchronized (activeItemsSet) {
            if (item.getCurrentStock() == item.getBaseStock()) return;

            long intervalMillis = configManager.getStockUpdateInterval() * 60 * 1000L;
            long nextUpdate = System.currentTimeMillis() + intervalMillis;

            // Remove and re-add to update priority
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
                // Handle stale item
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
        // Ensure minimum 1% regeneration
        double minRegen = 0.01;
        double regenRate = Math.max(item.getStockRegenRate(), minRegen);

        int maxAdjustment = (int) Math.round(base * regenRate);
        int delta = base - current;

        int adjustment = Integer.signum(delta) * Math.max(1, Math.min(maxAdjustment, Math.abs(delta)));

        LOGGER.fine(() -> String.format(
                "Stock adjustment for %s: base=%d, current=%d, regenRate=%.2f, adjustment=%d",
                item.getName(), base, current, item.getStockRegenRate(), adjustment
        ));

        // Ensure at least 1 item change
        return adjustment;
    }

    private void updateStockAndPrice(MarketItem item, int base, int newStock) {
        BigDecimal newPrice = calculateNewPrice(item);
        newStock = clampStockToBounds(base, newStock);
        if (newStock == base) {
            newPrice = item.getBasePrice();
        }
        updateItemState(item, newStock, newPrice);
    }

    private BigDecimal calculateNewPrice(MarketItem item) {
        boolean isAdding = item.getCurrentStock() < item.getBaseStock();
        BigDecimal multiplier = isAdding ?
                BigDecimal.ONE.subtract(item.getTaxRate()) :
                BigDecimal.ONE.add(item.getTaxRate());
        return item.getCurrentPrice()
                .multiply(multiplier)
                .setScale(configManager.getPriceDecimalPlaces(), RoundingMode.HALF_UP)
                .max(BigDecimal.valueOf(0.01));
    }

    private int clampStockToBounds(int base, int newStock) {
        double maxOverflow = configManager.getMaxStockOverflow();
        int maxStock = (int) (base * maxOverflow);
        return Math.max(0, Math.min(newStock, maxStock));
    }

    private void updateItemState(MarketItem item, int newStock, BigDecimal newPrice) {
        int oldStock = item.getCurrentStock();
        BigDecimal oldPrice = item.getCurrentPrice();
        item.setCurrentStock(newStock);
        item.setCurrentPrice(newPrice);
        item.getPriceHistory().add(newPrice);
        logStockUpdate(item, oldStock, oldPrice, newStock, newPrice);
        notifyStockUpdated(item);
    }

    private void logStockUpdate(MarketItem item, int oldStock, BigDecimal oldPrice, int newStock, BigDecimal newPrice) {
        LOGGER.info(() -> String.format(
                "%s | Stock: %d → %d | Price: %s → %s",
                item.getName(), oldStock, newStock, oldPrice, newPrice
        ));
    }

}