package io.github.HenriqueMichelini.craftalism_market.model;

import org.bukkit.Material;
import java.math.BigDecimal;
import java.util.List;

public class MarketItem {
    // Immutable fields
    private final String category;
    private final Material material;
    private final int slot;
    private final Double priceSellRatio;
    private final int maxAmount;
    private final double regenerationRate;
    private final double priceAdjustmentFactor;
    private final double regenAdjustmentFactor;
    private final double decayRate;
    private final double productivity;

    // Mutable fields
    private BigDecimal price;
    private BigDecimal priceSell;
    private int amount;
    private List<BigDecimal> priceHistory;
    private long lastActivity;

    public MarketItem(String category,
                      Material material,
                      int slot,
                      BigDecimal price,
                      BigDecimal priceSell,
                      Double priceSellRatio,
                      int amount,
                      int maxAmount,
                      double regenerationRate,
                      double priceAdjustmentFactor,
                      double regenAdjustmentFactor,
                      double decayRate,
                      double productivity,
                      long lastActivity,
                      List<BigDecimal> priceHistory) {
        this.category = category;
        this.material = material;
        this.slot = slot;
        this.price = price;
        this.priceSell = priceSell;
        this.priceSellRatio = priceSellRatio;
        this.amount = amount;
        this.maxAmount = maxAmount;
        this.regenerationRate = regenerationRate;
        this.priceAdjustmentFactor = priceAdjustmentFactor;
        this.regenAdjustmentFactor = regenAdjustmentFactor;
        this.decayRate = decayRate;
        this.productivity = productivity;
        this.lastActivity = lastActivity;
        this.priceHistory = priceHistory;
    }

    // Getters (all fields) -------------------------------------
    public String getCategory() { return category; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getPriceSell() { return priceSell; }
    public Double getPriceSellRatio() { return priceSellRatio; }
    public int getAmount() { return amount; }
    public int getMaxAmount() { return maxAmount; }
    public double getRegenerationRate() { return regenerationRate; }
    public double getPriceAdjustmentFactor() { return priceAdjustmentFactor; }
    public double getRegenAdjustmentFactor() { return regenAdjustmentFactor; }
    public double getDecayRate() { return decayRate; }
    public double getProductivity() { return productivity; }
    public long getLastActivity() { return lastActivity; }
    public List<BigDecimal> getPriceHistory() { return priceHistory; }

    // Setters (mutable fields only) ----------------------------
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setPriceSell(BigDecimal priceSell) { this.priceSell = priceSell; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setLastActivity(long lastActivity) { this.lastActivity = lastActivity; }
    public void setPriceHistory(List<BigDecimal> priceHistory) { this.priceHistory = priceHistory; }
}