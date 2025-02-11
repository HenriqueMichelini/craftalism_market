package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MarketManager {

    public MarketManager(DataLoader dataLoader) {
    }

    // Use geometric series for total price
    public BigDecimal getTotalPriceOfItem(MarketItem item, int amount, boolean isAdding) {
        BigDecimal multiplier = getMultiplier(item, isAdding);
        BigDecimal numerator = item.getBasePrice().multiply(
                multiplier.pow(amount).subtract(BigDecimal.ONE)
        );
        BigDecimal denominator = multiplier.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, RoundingMode.HALF_UP);
    }

    private BigDecimal getMultiplier(MarketItem item, boolean isAdding) {
        return isAdding
                ? BigDecimal.ONE.add(item.getPriceVariationPerOperation())
                : BigDecimal.ONE.subtract(item.getPriceVariationPerOperation());
    }

    public BigDecimal getLastPriceOfItem(MarketItem item, int termNumber, boolean isAdding) {
        BigDecimal multiplier = isAdding
                ? BigDecimal.ONE.add(item.getPriceVariationPerOperation())
                : BigDecimal.ONE.subtract(item.getPriceVariationPerOperation());
        return item.getBasePrice().multiply(multiplier.pow(termNumber));
    }

    public BigDecimal getArithmeticSequenceSumOfTerms(BigDecimal firstTerm, BigDecimal lastTerm, int numberOfTerms) {
        return firstTerm.add(lastTerm) // (firstTerm + lastTerm)
                .multiply(BigDecimal.valueOf(numberOfTerms)) // * numberOfTerms
                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP); // / 2
    }

    public BigDecimal getArithmeticSequenceTerm(BigDecimal firstTerm, BigDecimal commonDifference, int termNumber) {
        return firstTerm.add(commonDifference.multiply(BigDecimal.valueOf(termNumber - 1)));
    }

    public void updatePriceHistory(MarketItem item, BigDecimal newPrice) {
        List<BigDecimal> history = item.getPriceHistory();
        history.addFirst(newPrice); // Add new price to front

        // Keep only last 10 entries
        if (history.size() > 10) {
            history = history.subList(0, 10);
        }
        item.setPriceHistory(history);
    }

}