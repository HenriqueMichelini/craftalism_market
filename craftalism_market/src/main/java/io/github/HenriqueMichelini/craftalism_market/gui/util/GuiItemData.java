package io.github.HenriqueMichelini.craftalism_market.gui.util;

import org.bukkit.Material;

public class GuiItemData {
    private final Material material;
    private final String title;

    public GuiItemData(Material material, String title) {
        this.material = material;
        this.title = title;
    }

    public Material getMaterial() {
        return material;
    }

    public String getTitle() {
        return title;
    }
}
