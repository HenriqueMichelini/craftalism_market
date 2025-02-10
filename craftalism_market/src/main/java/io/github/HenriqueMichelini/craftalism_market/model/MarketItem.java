package io.github.HenriqueMichelini.craftalism_market.model;

import org.bukkit.Material;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MarketItem {
    // Immutable fields
    private final String category;
    private final Material material;
    private final int slot;
    private final BigDecimal buySellPriceRatio;

    // Mutable fields
    private BigDecimal currentBuyPrice;
    private BigDecimal currentSellPrice;
    private int currentAmount;
    private long lastActivity;
    private List<BigDecimal> priceHistory;

    public MarketItem(String category,
                      Material material,
                      int slot,
                      BigDecimal currentBuyPrice,
                      BigDecimal currentSellPrice,
                      BigDecimal buySellPriceRatio,
                      int currentAmount,
                      long lastActivity,
                      List<BigDecimal> priceHistory) {
        this.category = category;
        this.material = material;
        this.slot = slot;
        this.currentBuyPrice = currentBuyPrice;
        this.currentSellPrice = currentSellPrice;
        this.buySellPriceRatio = buySellPriceRatio.setScale(10, RoundingMode.HALF_UP);;
        this.currentAmount = currentAmount;
        this.lastActivity = lastActivity;
        this.priceHistory = priceHistory;
    }

    // Getters (all fields) -------------------------------------
    public String getCategory() { return category; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public BigDecimal getCurrentBuyPrice() { return currentBuyPrice; }
    public BigDecimal getCurrentSellPrice() { return currentSellPrice; }
    public BigDecimal getBuySellPriceRatio() { return buySellPriceRatio; }
    public int getCurrentAmount() { return currentAmount; }
    public long getLastActivity() { return lastActivity; }
    public List<BigDecimal> getPriceHistory() { return priceHistory; }

    // Setters (mutable fields only) ----------------------------
    public void setCurrentBuyPrice(BigDecimal currentBuyPrice) { this.currentBuyPrice = currentBuyPrice; }
    public void setCurrentSellPrice(BigDecimal currentSellPrice) { this.currentSellPrice = currentSellPrice; }
    public void setCurrentAmount(int currentAmount) { this.currentAmount = currentAmount; }
    public void setLastActivity(long lastActivity) { this.lastActivity = lastActivity; }
    public void setPriceHistory(List<BigDecimal> priceHistory) { this.priceHistory = priceHistory; }
}