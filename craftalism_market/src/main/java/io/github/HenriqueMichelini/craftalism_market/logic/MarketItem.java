package io.github.HenriqueMichelini.craftalism_market.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;

public class MarketItem {
    private final String name;
    private final String category;
    private BigDecimal price;
    private int stock;
    private int maxStock;
    private double regenerationRate;
    private final Deque<BigDecimal> priceHistory = new ArrayDeque<>(10);

    private final double priceAdjustmentFactor = 0.1;
    private final double regenAdjustmentFactor = 0.05;
    private final double sellMultiplier = 0.8;
    private final double decayFactor = 0.02;
    private final double saturationFactor = 0.05;

    public MarketItem(String name, String category, double price, int stock, int maxStock, double regenerationRate) {
        this.name = name;
        this.category = category;
        this.price = BigDecimal.valueOf(price);
        this.stock = stock;
        this.maxStock = maxStock;
        this.regenerationRate = regenerationRate;
        for (int i = 0; i < 10; i++) {
            priceHistory.addLast(this.price);
        }
    }

    public void updateMarket(int demand) {
        // Ajuste de preço com base na demanda
        BigDecimal priceChangeFactor = BigDecimal.valueOf(priceAdjustmentFactor)
                .multiply(BigDecimal.valueOf(demand - stock)
                        .divide(BigDecimal.valueOf(maxStock), 4, RoundingMode.HALF_UP));
        price = price.multiply(BigDecimal.ONE.add(priceChangeFactor)).max(BigDecimal.valueOf(0.01));

        // Ajuste de regeneração
        double regenChangeFactor = regenAdjustmentFactor * ((double) (demand - stock) / maxStock);
        regenerationRate *= (1 + regenChangeFactor);
        regenerationRate = Math.max(0.5, regenerationRate);

        // Regeneração de estoque
        stock += Math.round(regenerationRate);
        stock = Math.min(stock, maxStock);

        // Atualiza histórico de preços
        if (priceHistory.size() >= 10) {
            priceHistory.pollFirst();
        }
        priceHistory.addLast(price);
    }

    public BigDecimal getSellPrice() {
        return price.multiply(BigDecimal.valueOf(sellMultiplier));
    }

    public BigDecimal getMovingAverage() {
        return priceHistory.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(priceHistory.size()), RoundingMode.HALF_UP);
    }

    public void buy(int amount) {
        if (amount > stock) {
            System.out.println("Estoque insuficiente para " + name);
            return;
        }
        stock -= amount;
        updateMarket(amount);
    }

    public void sell(int amount) {
        stock += amount;
        updateMarket(-amount);
    }

    public void displayInfo() {
        System.out.println(name + " | Preço: " + price.setScale(2, RoundingMode.HALF_UP) +
                " | Estoque: " + stock + "/" + maxStock +
                " | Regeneração: " + String.format("%.2f", regenerationRate));
    }
}
