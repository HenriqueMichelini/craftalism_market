package io.github.HenriqueMichelini.craftalism_market.model;

import org.bukkit.Material;

public class MarketCategoryItem {
    private final Material material;
    private final String title;
    private final int slot;

    public MarketCategoryItem(Material material, String title, int slot) {
        this.material = material;
        this.title = title;
        this.slot = slot;
    }

    // Getters
    public Material getMaterial() { return material; }
    public String getTitle() { return title; }
    public int getSlot() { return slot; }
}
