package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.gui.util.GuiItemData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;
import java.util.Set;

public class MarketCategories {
    private final Gui gui;

    public MarketCategories() {
        gui = Gui.gui()
                .title(Component.text("Market", NamedTextColor.GREEN))
                .rows(6) // Fixed number of rows
                .disableAllInteractions() // Disable all interactions by default
                .create();

        populateGui();
    }

    // Populate the categories screen dynamically from market_category.yml
    private void populateGui() {
        FileConfiguration config = CraftalismMarket.getInstance().getMarketCategoryConfig(); // Load market_category.yml

        if (!config.contains("items")) return; // Avoid errors if the file is empty

        Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false);

        for (String key : keys) {
            String path = "items." + key;
            String title = config.getString(path + ".title");
            String materialStr = config.getString(path + ".material");
            int slot = config.getInt(path + ".slot");

            if (title == null || materialStr == null) continue; // Skip if data is missing

            Material material = Material.matchMaterial(materialStr);
            if (material == null) continue; // Skip invalid materials

            GuiItemData data = new GuiItemData(material, title);
            gui.setItem(slot, createGuiItem(data));
        }
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
            MarketItems subCategoryGui = new MarketItems(data.getTitle());
            subCategoryGui.getGui().open(player);
        }
    }

    // Return the constructed GUI
    public Gui getGui() {
        return gui;
    }
}
