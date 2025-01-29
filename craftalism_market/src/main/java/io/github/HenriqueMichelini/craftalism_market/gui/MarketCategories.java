package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

public class MarketCategories {
    private final Gui gui;

    public MarketCategories() {
        gui = Gui.gui()
                .title(Component.text("Market", NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions() // Disable all interactions by default
                .create();
        populateGui();
    }

    // Populate the categories screen with different materials
    private void populateGui() {
        Map<Integer, Material> categoryItems = Map.of(
                19, Material.STONE,       // Natural Resources
                21, Material.OAK_LOG,     // Woods
                23, Material.PURPLE_WOOL, // Wools
                25, Material.EMERALD,     // Ores
                29, Material.CYAN_DYE,    // Dyes
                31, Material.WHEAT,       // Livestock
                33, Material.STRING       // Mob Drops
        );

        categoryItems.forEach((slot, material) -> gui.setItem(slot, createGuiItem(material)));
    }

    // Create a GUI item for each category
    private GuiItem createGuiItem(Material material) {
        return ItemBuilder.from(material)
                .asGuiItem(event -> openSubCategory(event, material));
    }

    // Open the subcategory when a category item is clicked
    private void openSubCategory(InventoryClickEvent event, Material material) {
        if (event.getWhoClicked() instanceof Player player) {
            // Determine which subcategory to open based on clicked material
            Map<Integer, Material> subCategoryItems = switch (material) {
                case STONE -> MarketSubCategories.naturalResourcesCategory();
                case OAK_LOG -> MarketSubCategories.woodCategory();
                case PURPLE_WOOL -> MarketSubCategories.woolCategory();
                case EMERALD -> MarketSubCategories.oresCategory();
                case CYAN_DYE -> MarketSubCategories.dyesCategory();
                case WHEAT -> MarketSubCategories.livestockCategory();
                case STRING -> MarketSubCategories.mobDropsCategory();
                default -> null;
            };

            if (subCategoryItems != null) {
                // Create and open the appropriate subcategory GUI
                MarketSubCategories subCategoryGui = new MarketSubCategories(material.name(), subCategoryItems);
                subCategoryGui.getGui().open(player);
            }
        }
    }

    // Return the constructed GUI
    public Gui getGui() {
        return gui;
    }
}
