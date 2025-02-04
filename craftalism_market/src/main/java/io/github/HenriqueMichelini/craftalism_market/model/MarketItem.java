package io.github.HenriqueMichelini.craftalism_market.model;

import org.bukkit.Material;

import java.math.BigDecimal;

public class MarketItem {
    private final String category;
    private final Material material;
    private final int slot;
    private final BigDecimal price;
    private final int amount;

    public MarketItem(String category, Material material, int slot, BigDecimal price, int amount) {
        this.category = category;
        this.material = material;
        this.slot = slot;
        this.price = price;
        this.amount = amount;
    }

    // Getters
    public String getCategory() { return category; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public BigDecimal getPrice() { return price; }
    public int getAmount() { return amount; }
}
