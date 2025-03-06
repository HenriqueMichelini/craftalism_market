package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class MarketManager {
    private static final int PRICE_SCALE = 2;
    private static final int MAX_HISTORY_ENTRIES = 10;
    private static final BigDecimal ONE = BigDecimal.ONE;

    /**
     * Calculates total price for a transaction using geometric progression
     * @throws IllegalArgumentException if item is null or amount is negative
     */
    public BigDecimal getTotalPriceOfItem(MarketItem item, int amount, boolean isAdding) {
        validateInput(item, amount);

        BigDecimal multiplier = getMultiplier(item, isAdding);
        if (multiplier.compareTo(ONE) == 0) {
            return item.getBasePrice().multiply(BigDecimal.valueOf(amount));
        }

        BigDecimal powered = multiplier.pow(amount);
        BigDecimal numerator = item.getBasePrice().multiply(powered.subtract(ONE));
        BigDecimal denominator = multiplier.subtract(ONE);

        return numerator.divide(denominator, PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Gets the multiplier for price calculation
     */
    private BigDecimal getMultiplier(MarketItem item, boolean isAdding) {
        BigDecimal variation = item.getPriceVariationPerOperation();
        return isAdding ? ONE.add(variation) : ONE.subtract(variation);
    }

    /**
     * Calculates the last price in the sequence
     * @throws IllegalArgumentException if item is null or termNumber is negative
     */
    public BigDecimal getLastPriceOfItem(MarketItem item, int termNumber, boolean isAdding) {
        validateInput(item, termNumber);
        BigDecimal multiplier = getMultiplier(item, isAdding);
        return item.getBasePrice()
                .multiply(multiplier.pow(termNumber))
                .setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Updates price history with new price entry
     * Maintains only the last MAX_HISTORY_ENTRIES prices
     */
    public void updatePriceHistory(MarketItem item, BigDecimal newPrice) {
        Objects.requireNonNull(item, "MarketItem cannot be null");
        Objects.requireNonNull(newPrice, "New price cannot be null");

        LinkedList<BigDecimal> history = new LinkedList<>(item.getPriceHistory());
        history.addFirst(newPrice);

        // Trim history to max size
        while (history.size() > MAX_HISTORY_ENTRIES) {
            history.removeLast();
        }

        item.setPriceHistory(new ArrayList<>(history));
    }

    private void validateInput(MarketItem item, int number) {
        Objects.requireNonNull(item, "MarketItem cannot be null");
        if (number < 0) {
            throw new IllegalArgumentException("Amount/term number cannot be negative");
        }
    }
}