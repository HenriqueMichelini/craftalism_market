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

public class MarketSubCategories {
    private final Gui gui;

    // Constructor: Initialize with a subcategory name and items
    public MarketSubCategories(String subCategoryName, Map<Integer, Material> subCategoryItems) {
        gui = Gui.gui()
                .title(Component.text(subCategoryName, NamedTextColor.GREEN))
                .rows(6) // Assuming 6 rows for the GUI
                .disableAllInteractions() // Disable all interactions by default
                .create();

        populateGui(subCategoryItems);
        addBackButton();
    }

    // Populate the GUI with the subcategory items
    private void populateGui(Map<Integer, Material> subCategoryItems) {
        subCategoryItems.forEach((slot, material) -> gui.setItem(slot, createGuiItem(material)));
    }

    // Create the GuiItem with click event
    private GuiItem createGuiItem(Material material) {
        return ItemBuilder.from(material).asGuiItem(event -> {
            // Handle the click action here (if needed)
            event.getWhoClicked().sendMessage("You clicked on " + material.name());
        });
    }

    // Add a "Back to Categories" button to the GUI
    private void addBackButton() {
        GuiItem backButton = ItemBuilder.from(Material.BARRIER)
                .name(Component.text("Back to Categories", NamedTextColor.RED))
                .asGuiItem(this::goBackToCategories);

        gui.setItem(49, backButton); // Position the button in the center-bottom
    }

    // Action for the back button to return to the main categories screen
    private void goBackToCategories(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            // Reopen the MarketCategories GUI when clicked
            MarketCategories marketCategories = new MarketCategories();
            marketCategories.getGui().open(player);
        }
    }

    public static Map<Integer, Material> naturalResourcesCategory() {
        return Map.of(
                10, Material.STONE,
                12, Material.DIRT,
                14, Material.SAND,
                16, Material.GRAVEL
        );
    }

    public static Map<Integer, Material> woodCategory() {
        return Map.of(
                10, Material.OAK_LOG,
                12, Material.SPRUCE_LOG,
                14, Material.BIRCH_LOG,
                16, Material.JUNGLE_LOG
        );
    }

    public static Map<Integer, Material> woolCategory() {
        return Map.of(
                10, Material.WHITE_WOOL,
                12, Material.RED_WOOL,
                14, Material.BLUE_WOOL,
                16, Material.YELLOW_WOOL
        );
    }

    public static Map<Integer, Material> oresCategory() {
        return Map.of(
                10, Material.IRON_INGOT,
                12, Material.GOLD_INGOT,
                14, Material.DIAMOND,
                16, Material.EMERALD
        );
    }

    public static Map<Integer, Material> dyesCategory() {
        return Map.of(
                10, Material.RED_DYE,
                12, Material.BLUE_DYE,
                14, Material.GREEN_DYE,
                16, Material.YELLOW_DYE
        );
    }

    public static Map<Integer, Material> livestockCategory() {
        return Map.of(
                10, Material.WHEAT,
                12, Material.CARROT,
                14, Material.BEEF,
                16, Material.PORKCHOP
        );
    }

    public static Map<Integer, Material> mobDropsCategory() {
        return Map.of(
                10, Material.STRING,
                12, Material.BONE,
                14, Material.GUNPOWDER,
                16, Material.SLIME_BALL
        );
    }

    public Gui getGui() {
        return gui;
    }
}
