package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import java.math.BigDecimal;
import java.util.List;

public class MarketManager {
    private final DataLoader dataLoader;

    public MarketManager(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    // Called when an item is bought
    public void handlePurchase(MarketItem item, int quantity) {
        // Update stock
        int newAmount = item.getAmount() - quantity;
        if (newAmount < 0) newAmount = 0;
        item.setAmount(newAmount);

        // Adjust price upwards based on demand
        BigDecimal priceIncrease = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getPriceAdjustmentFactor()));
        BigDecimal newPrice = item.getPrice().add(priceIncrease);
        item.setPrice(newPrice);

        // Update price history
        updatePriceHistory(item, newPrice);

        // Save changes
        dataLoader.saveItemsData();
    }

    // Called when an item is sold
    public void handleSale(MarketItem item, int quantity) {
        // Update stock (capped at maxAmount)
        int newAmount = item.getAmount() + quantity;
        if (newAmount > item.getMaxAmount()) newAmount = item.getMaxAmount();
        item.setAmount(newAmount);

        // Adjust price downwards based on supply
        BigDecimal priceDecrease = item.getPrice().multiply(BigDecimal.valueOf(item.getPriceAdjustmentFactor()));
        BigDecimal newPrice = item.getPrice().subtract(priceDecrease);
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) newPrice = BigDecimal.ZERO;
        item.setPrice(newPrice);

        // Update price history
        updatePriceHistory(item, newPrice);

        // Save changes
        dataLoader.saveItemsData();
    }

    private void updatePriceHistory(MarketItem item, BigDecimal newPrice) {
        List<BigDecimal> history = item.getPriceHistory();
        history.add(0, newPrice); // Add new price to front

        // Keep only last 10 entries
        if (history.size() > 10) {
            history = history.subList(0, 10);
        }
        item.setPriceHistory(history);
    }

    // Add other economic simulations as needed
}