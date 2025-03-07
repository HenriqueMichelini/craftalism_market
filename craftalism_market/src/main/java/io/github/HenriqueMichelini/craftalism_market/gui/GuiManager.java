package io.github.HenriqueMichelini.craftalism_market.gui;

import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.gui.components.*;
import io.github.HenriqueMichelini.craftalism_market.logic.DataLoader;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class GuiManager {
    // Region: Dependencies
    private final DataLoader dataLoader;
    private final CraftalismMarket plugin;
    private final MarketManager marketManager;

    // Region: GUI Components
    private MarketGui marketGui;
    private final Map<String, CategoryItemsGui> categoryGuis = new HashMap<>();

    public GuiManager(DataLoader dataLoader, CraftalismMarket plugin, MarketManager marketManager) {
        this.dataLoader = dataLoader;
        this.plugin = plugin;
        this.marketManager = marketManager;
        initializeGUIs();
    }

    // Region: Initialization
    private void initializeGUIs() {
        // Initialize main market GUI
        this.marketGui = new MarketGui(
                plugin,
                dataLoader,
                this::handleCategorySelection
        );

        // Preload category GUIs
        dataLoader.getMarketCategories().values().forEach(category -> categoryGuis.put(category.title(), new CategoryItemsGui(
                category.title(),
                plugin,
                dataLoader,
                this::handleItemSelection,
                this::openMarket
        )));
    }

    // Region: Public Interface
    public void openMarket(Player player) {
        marketGui.open(player);
    }

    private void handleCategorySelection(Player player, String category) {
        CategoryItemsGui categoryGui = categoryGuis.get(category);
        if (categoryGui != null) {
            categoryGui.open(player);
        } else {
            plugin.getLogger().warning("Attempted to open invalid category: " + category);
            player.sendMessage(Component.text("Invalid category!", NamedTextColor.RED));
        }
    }

    private void handleItemSelection(Player player, String itemName) {
        new ItemNegotiationGui(
                itemName,
                plugin,
                dataLoader,
                marketManager,
                p -> returnToCategory(p, getItemCategory(itemName)), this
        ).open(player);
    }

    // Region: Navigation Helpers
    private void returnToCategory(Player player, String category) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            CategoryItemsGui categoryGui = categoryGuis.get(category);
            if (categoryGui != null && player.isOnline()) {
                categoryGui.open(player);
            } else if (player.isOnline()) {
                openMarket(player);
            }
        }, 1L);
    }

    public void refreshCategoryItem(String category, String itemName) {
        CategoryItemsGui categoryGui = categoryGuis.get(category);
        if (categoryGui != null) {
            categoryGui.refreshItem(itemName);
        }
    }

    private String getItemCategory(String itemName) {
        return dataLoader.getMarketItems().get(itemName).getCategory();
    }
}