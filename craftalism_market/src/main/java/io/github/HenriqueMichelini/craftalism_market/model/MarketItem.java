package io.github.HenriqueMichelini.craftalism_market.model;

import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.List;

public class MarketItem {
    private final String category;
    private final Material material;
    private final int slot;
    private final BigDecimal price;
    private final BigDecimal priceSell;
    private final Double priceSellRatio;
    private final int amount;
    private final List<BigDecimal> price_history;

    public MarketItem(String category, Material material, int slot, BigDecimal price, BigDecimal priceSell, Double priceSellRatio, int amount, List<BigDecimal> priceHistory) {
        this.category = category;
        this.material = material;
        this.slot = slot;
        this.price = price;
        this.priceSell = priceSell;
        this.priceSellRatio = priceSellRatio;
        this.amount = amount;
        price_history = priceHistory;
    }

    // Getters
    public String getCategory() { return category; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getPriceSell() { return priceSell; }
    public Double getPriceSellRatio() { return priceSellRatio; }
    public int getAmount() { return amount; }
    public List<BigDecimal> getPriceHistory() { return price_history; }
}
