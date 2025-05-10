package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_economy.economy.util.MoneyFormat;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Logger;

public class MarketMath {
    private static final int MAX_HISTORY_ENTRIES = 10;
    private static final long DECIMAL_SCALE = MoneyFormat.DECIMAL_SCALE;
    private static final Logger LOGGER = Logger.getLogger(MarketMath.class.getName());

    /**
     * Calculates total price for a transaction using geometric progression
     * @throws IllegalArgumentException if item is null or amount is negative
     */
    public long getTotalPriceOfItem(MarketItem item, int amount, boolean isAdding) {
        validateInput(item, amount);

        long multiplier = getMultiplier(item, isAdding);
        long currentPrice = item.getCurrentPrice();

        if (multiplier == DECIMAL_SCALE) {
            return currentPrice * amount;
        }

        long powered = pow(multiplier, amount);
        long numerator = currentPrice * (powered - DECIMAL_SCALE);
        long denominator = multiplier - DECIMAL_SCALE;

        return numerator / denominator;
    }

    /**
     * Gets the multiplier for price calculation
     */
    private long getMultiplier(MarketItem item, boolean isAdding) {
        long variation = item.getPriceVariationPerOperation();
        return isAdding ? DECIMAL_SCALE + variation : DECIMAL_SCALE - variation;
    }

    /**
     * Calculates the last price in the sequence
     * @throws IllegalArgumentException if item is null or termNumber is negative
     */
    public long getLastPriceOfItem(MarketItem item, int termNumber, boolean isAdding) {
        validateInput(item, termNumber);
        long multiplier = getMultiplier(item, isAdding);
        long powered = pow(multiplier, termNumber);
        long scalePowered = pow(DECIMAL_SCALE, termNumber);
        return (item.getCurrentPrice() * powered) / scalePowered;
    }

    /**
     * Updates price history with new price entry
     * Maintains only the last MAX_HISTORY_ENTRIES prices
     */
    public void updatePriceHistory(MarketItem item, long newPrice) {
        Objects.requireNonNull(item, "MarketItem cannot be null");

        LinkedList<Long> history = new LinkedList<>(item.getPriceHistory());
        history.addFirst(newPrice);

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

    private long pow(long base, int exponent) {
        if (exponent == 0) return DECIMAL_SCALE;
        long result = base;
        for (int i = 1; i < exponent; i++) {
            result = (result * base) / DECIMAL_SCALE;
        }
        return result;
    }
}