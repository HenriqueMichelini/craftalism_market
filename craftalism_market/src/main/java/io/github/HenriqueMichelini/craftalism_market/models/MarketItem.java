package io.github.HenriqueMichelini.craftalism_market.models;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import java.util.List;

public class MarketItem {
    // Immutable fields
    private final String category;
    private final Material material;
    private final int slot;
    private final double taxRate;
    private final long basePrice;
    private final int originalStock;

    // Mutable fields
    private long currentPrice;
    private long priceVariationPerOperation;
    private int currentStock;
    private int baseStock;
    private double stockRegenerationMultiplier;
    private double stockRegenerationRate;
    private long nextUpdateTime;
    private int stockSurplus;

    private long lastActivity;
    private List<Long> priceHistory;

    public MarketItem
    (
            String              category,
            Material            material,
            int                 slot,
            long                basePrice,
            long                currentPrice,
            long                priceVariationPerOperation,
            double              taxRate,
            int                 originalStock,
            int                 baseStock,
            int                 currentStock,
            double              stockRegenerationMultiplier,
            double              stockRegenerationRate,
            long                nextUpdateTime,
            int                 stockSurplus,
            long                lastActivity,
            List<Long>          priceHistory
    )
    {
            this.category = category;
            this.material = material;
            this.slot = slot;
            this.basePrice = basePrice;
            this.currentPrice = currentPrice;
            this.priceVariationPerOperation = priceVariationPerOperation;
            this.taxRate = taxRate;
            this.originalStock = originalStock;
            this.baseStock = baseStock;
            this.currentStock = currentStock;
            this.stockRegenerationMultiplier = stockRegenerationMultiplier;
            this.stockRegenerationRate = stockRegenerationRate;
            this.nextUpdateTime = nextUpdateTime;
            this.stockSurplus = stockSurplus;
            this.lastActivity = lastActivity;
            this.priceHistory = priceHistory;
    }

    // Getters (all fields) -------------------------------------
    public String       getCategory()                   { return category; }
    public Material     getMaterial()                   { return material; }
    public int          getSlot()                       { return slot; }
    public long         getBasePrice()                  { return basePrice; }
    public long         getCurrentPrice()               { return currentPrice; }
    public long         getPriceVariationPerOperation() { return priceVariationPerOperation; }
    public double       getTaxRate()                    { return taxRate; }
    public int          getOriginalStock()              { return originalStock; }
    public int          getBaseStock()                  { return baseStock; }
    public int          getCurrentStock()               { return currentStock; }
    public double       getStockRegenerationMultiplier(){ return  stockRegenerationMultiplier; }
    public double       getStockRegenerationRate()      { return stockRegenerationRate; }
    public long         getNextUpdateTime()             { return nextUpdateTime; }
    public int          getStockSurplus()               { return stockSurplus; }
    public long         getLastActivity()               { return lastActivity; }
    public List<Long>   getPriceHistory()               { return priceHistory; }

    public String getName() {
        String translationKey = material.isBlock()
                ? "block.minecraft." + material.name().toLowerCase()
                : "item.minecraft." + material.name().toLowerCase();

        Component translatedComponent = Component.translatable(translationKey);

        return PlainTextComponentSerializer.plainText().serialize(translatedComponent);
    }

    // Setters (mutable fields only) ----------------------------
    public void setCurrentPrice(long currentPrice)                                  { this.currentPrice = currentPrice; }
    public void setPriceVariationPerOperation(long priceVariationPerOperation)      { this.priceVariationPerOperation = priceVariationPerOperation; }
    public void setCurrentStock(int currentStock)                                   { this.currentStock = currentStock; }
    public void setBaseStock(int baseStock)                                         { this.baseStock = baseStock;       }
    public void setStockRegenerationMultiplier(double stockRegenerationMultiplier)  { this.stockRegenerationMultiplier = stockRegenerationMultiplier; }
    public void setStockRegenerationRate(double stockRegenerationRate)              { this.stockRegenerationRate = stockRegenerationRate; }
    public void setNextUpdateTime(long nextUpdateTime)                              { this.nextUpdateTime = nextUpdateTime; }
    public void setStockSurplus(int stockSurplus)                                   { this.stockSurplus = stockSurplus; }
    public void setLastActivity(long lastActivity)                                  { this.lastActivity = lastActivity; }
    public void setPriceHistory(List<Long> priceHistory)                            { this.priceHistory = priceHistory; }
}