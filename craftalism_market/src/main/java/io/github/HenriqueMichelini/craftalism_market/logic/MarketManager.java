package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MarketManager {
    private final DataLoader dataLoader;

    public MarketManager(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public void setItemPrice(MarketItem item, BigDecimal lastItemPrice) {
        item.setCurrentBuyPrice(lastItemPrice);

    }

    public BigDecimal getTotalPriceOfItem(MarketItem item, int termNumber) {
        BigDecimal firstTerm = item.getCurrentBuyPrice();
        BigDecimal lastTerm = getLastPriceOfItem(item, termNumber);

        return getArithmeticSequenceSumOfTerms(firstTerm, lastTerm, termNumber);
    }

    public BigDecimal getLastPriceOfItem(MarketItem item, int termNumber) {
        BigDecimal firstTerm = item.getCurrentBuyPrice();
        BigDecimal buySellMultiplier = BigDecimal.ONE.add(item.getBuySellPriceRatio()); // (1 + ratio)
        BigDecimal secondTerm = firstTerm.multiply(buySellMultiplier);
        BigDecimal commonDifference = secondTerm.subtract(firstTerm);

        return getArithmeticSequenceTerm(firstTerm, commonDifference, termNumber);
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