package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.gui.util.GuiItemData;
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

    // Populate the categories screen with different materials and titles
    private void populateGui() {
        Map<Integer, GuiItemData> categoryItems = Map.of(
                19, new GuiItemData(Material.STONE, "Natural Resources"),
                21, new GuiItemData(Material.OAK_LOG, "Woods"),
                23, new GuiItemData(Material.PURPLE_WOOL, "Wools"),
                25, new GuiItemData(Material.EMERALD, "Ores"),
                29, new GuiItemData(Material.CYAN_DYE, "Dyes"),
                31, new GuiItemData(Material.WHEAT, "Livestock"),
                33, new GuiItemData(Material.STRING, "Mob Drops")
        );

        categoryItems.forEach((slot, data) -> gui.setItem(slot, createGuiItem(data)));
    }

    // Create a GUI item for each category
    private GuiItem createGuiItem(GuiItemData data) {
        return ItemBuilder.from(data.getMaterial())
                .name(Component.text(data.getTitle(), NamedTextColor.WHITE))
                .asGuiItem(event -> openSubCategory(event, data));
    }

    // Open the subcategory when a category item is clicked
    private void openSubCategory(InventoryClickEvent event, GuiItemData data) {
        if (event.getWhoClicked() instanceof Player player) {
            // Determine which subcategory to open based on clicked material
            Map<Integer, Material> subCategoryItems = switch (data.getMaterial()) {
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
                MarketSubCategories subCategoryGui = new MarketSubCategories(data.getTitle(), subCategoryItems);
                subCategoryGui.getGui().open(player);
            }
        }
    }

    // Return the constructed GUI
    public Gui getGui() {
        return gui;
    }
}
