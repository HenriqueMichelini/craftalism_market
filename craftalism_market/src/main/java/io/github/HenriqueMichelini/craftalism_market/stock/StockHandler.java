package io.github.HenriqueMichelini.craftalism_market.stock;

import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.stock.listener.StockUpdateListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

public class StockHandler {
    private static final Logger LOGGER = Logger.getLogger(StockHandler.class.getName());
    private final PriorityQueue<MarketItem> activeItemsQueue = new PriorityQueue<>(Comparator.comparingLong(MarketItem::getNextUpdateTime));
    private final Set<MarketItem> activeItemsSet = new HashSet<>();
    private final List<StockUpdateListener> listeners = new ArrayList<>();
    private final ConfigManager configManager;

    public StockHandler(ConfigManager configManager) {
        this.configManager = configManager;
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


    public void processAllActiveItems() {
        long now = System.currentTimeMillis();
        while (!activeItemsQueue.isEmpty()) {
            MarketItem item = activeItemsQueue.peek();
            if (item.getNextUpdateTime() > now) break;

            activeItemsQueue.poll();
            activeItemsSet.remove(item);

            processItemStock(item);

            // Reschedule if not at base stock
            if (item.getCurrentStock() != item.getBaseStock()) {
                long nextUpdate = now + (configManager.getStockUpdateInterval() * 60 * 1000L);
                item.setNextUpdateTime(nextUpdate);
                activeItemsQueue.add(item);
                activeItemsSet.add(item);
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

        LOGGER.fine(String.format(
                "Stock adjustment for %s: base=%d, current=%d, regenRate=%.2f, adjustment=%d",
                item.getName(), base, current, item.getStockRegenRate(), adjustment
        ));

        // Ensure at least 1 item change
        return adjustment;
    }

    private void updateStockAndPrice(MarketItem item, int base, int newStock) {
        // Get config values
        double maxOverflow = configManager.getMaxStockOverflow();
        int decimalPlaces = configManager.getPriceDecimalPlaces();

        // Determine price direction
        boolean isAdding = newStock > item.getCurrentStock();
        BigDecimal multiplier = isAdding ?
                BigDecimal.ONE.subtract(item.getTaxRate()) :
                BigDecimal.ONE.add(item.getTaxRate());

        // Calculate new price
        BigDecimal newPrice = item.getCurrentPrice()
                .multiply(multiplier)
                .setScale(decimalPlaces, RoundingMode.HALF_UP)
                .max(BigDecimal.valueOf(0.01));

        // Apply stock boundaries
        newStock = Math.max(0, Math.min(newStock, (int) (base * maxOverflow)));

        // Check if stock has returned to base level and reset price
        if (newStock == base) {
            newPrice = item.getBasePrice(); // Reset to base price
        }

        // Update item state
        int oldStock = item.getCurrentStock();
        BigDecimal oldPrice = item.getCurrentPrice();
        item.setCurrentStock(newStock);
        item.setCurrentPrice(newPrice);
        item.getPriceHistory().add(newPrice);

        int finalNewStock = newStock;
        BigDecimal finalNewPrice = newPrice;
        LOGGER.info(() -> String.format(
                "%s | Stock: %d → %d | Price: $%.2f → $%.2f (Multiplier: %s)",
                item.getName(),
                oldStock,
                finalNewStock,
                oldPrice,
                finalNewPrice,
                multiplier
        ));

        notifyStockUpdated(item);
    }
}