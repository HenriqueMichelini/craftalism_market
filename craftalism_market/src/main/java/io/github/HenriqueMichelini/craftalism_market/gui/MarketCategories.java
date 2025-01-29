package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Map;

public class MarketCategories {
    private final Gui gui;

    public MarketCategories() {
        gui = Gui.gui()
                .title(Component.text("Market", NamedTextColor.GREEN))
                .rows(6)
                .create();
        populateGui();
    }

    private void populateGui() {
        Map<Integer, Material> categoryItems = Map.of(
                19, Material.STONE,
                21, Material.OAK_LOG,
                23, Material.PURPLE_WOOL,
                25, Material.EMERALD,
                29, Material.CYAN_DYE,
                31, Material.WHEAT,
                33, Material.STRING
        );

        categoryItems.forEach((slot, material) -> gui.setItem(slot, createGuiItem(material)));
    }

    private GuiItem createGuiItem(Material material) {
        return ItemBuilder.from(material).asGuiItem(event -> {});
    }

    public Gui getGui() {
        return gui;
    }
}
