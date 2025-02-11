package io.github.HenriqueMichelini.craftalism_market.model;

import org.bukkit.Material;
import java.math.BigDecimal;
import java.util.List;

public class MarketItem {
    // Immutable fields
    private final String category;
    private final Material material;
    private final int slot;
    private final BigDecimal priceVariationPerOperation;
    private final BigDecimal sellTax;

    // Mutable fields
    private BigDecimal basePrice;
    private int amount;
    private long lastActivity;
    private List<BigDecimal> priceHistory;

    public MarketItem(String category,
                      Material material,
                      int slot,
                      BigDecimal basePrice,
                      BigDecimal priceVariationPerOperation,
                      BigDecimal sellTax,
                      int amount,
                      long lastActivity,
                      List<BigDecimal> priceHistory) {
        this.category = category;
        this.material = material;
        this.slot = slot;
        this.basePrice = basePrice;
        this.priceVariationPerOperation = priceVariationPerOperation;
        this.sellTax = sellTax;
        this.amount = amount;
        this.lastActivity = lastActivity;
        this.priceHistory = priceHistory;
    }

    // Getters (all fields) -------------------------------------
    public String getCategory() { return category; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public BigDecimal getBasePrice() { return basePrice; }
    public BigDecimal getPriceVariationPerOperation() { return priceVariationPerOperation; }
    public BigDecimal getSellTax() { return sellTax; }
    public int getAmount() { return amount; }
    public long getLastActivity() { return lastActivity; }
    public List<BigDecimal> getPriceHistory() { return priceHistory; }

    // Setters (mutable fields only) ----------------------------
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setLastActivity(long lastActivity) { this.lastActivity = lastActivity; }
    public void setPriceHistory(List<BigDecimal> priceHistory) { this.priceHistory = priceHistory; }
}