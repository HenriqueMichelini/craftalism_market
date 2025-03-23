package io.github.HenriqueMichelini.craftalism_market.models;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import java.math.BigDecimal;
import java.util.List;

public class MarketItem {
    // Immutable fields
    private final String category;
    private final Material material;
    private final int slot;
    private final BigDecimal priceVariationPerOperation;
    private final BigDecimal taxRate;
    private final BigDecimal basePrice;
    private final int baseStock;

    // Mutable fields
    private BigDecimal currentPrice;
    private int currentStock;
    private long lastActivity;
    private List<BigDecimal> priceHistory;

    public MarketItem
    (
            String              category,
            Material            material,
            int                 slot,
            BigDecimal          basePrice,
            BigDecimal          currentPrice,
            BigDecimal          priceVariationPerOperation,
            BigDecimal          taxRate,
            int                 baseStock,
            int                 currentStock,
            long                lastActivity,
            List<BigDecimal>    priceHistory
    )
    {
            this.category = category;
            this.material = material;
            this.slot = slot;
            this.basePrice = basePrice;
            this.currentPrice = currentPrice;
            this.priceVariationPerOperation = priceVariationPerOperation;
            this.taxRate = taxRate;
            this.baseStock = baseStock;
            this.currentStock = currentStock;
            this.lastActivity = lastActivity;
            this.priceHistory = priceHistory;
    }

    // Getters (all fields) -------------------------------------
    public String           getCategory()                   { return category; }
    public Material         getMaterial()                   { return material; }
    public int              getSlot()                       { return slot; }
    public BigDecimal       getBasePrice()                  { return basePrice; }
    public BigDecimal       getCurrentPrice()               { return currentPrice; }
    public BigDecimal       getPriceVariationPerOperation() { return priceVariationPerOperation; }
    public BigDecimal       getTaxRate()                    { return taxRate; }
    public int              getBaseStock()                  { return baseStock; }
    public int              getCurrentStock()               { return currentStock; }
    public long             getLastActivity()               { return lastActivity; }
    public List<BigDecimal> getPriceHistory()               { return priceHistory; }

    public String getName() {
        String translationKey = material.isBlock()
                ? "block.minecraft." + material.name().toLowerCase()
                : "item.minecraft." + material.name().toLowerCase();

        Component translatedComponent = Component.translatable(translationKey);

        return PlainTextComponentSerializer.plainText().serialize(translatedComponent);
    }

    // Setters (mutable fields only) ----------------------------
    public void setCurrentPrice(BigDecimal currentPrice)        { this.currentPrice = currentPrice; }
    public void setCurrentStock(int currentStock)               { this.currentStock = currentStock; }
    public void setLastActivity(long lastActivity)              { this.lastActivity = lastActivity; }
    public void setPriceHistory(List<BigDecimal> priceHistory)  { this.priceHistory = priceHistory; }
}