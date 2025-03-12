package io.github.HenriqueMichelini.craftalism_market.gui.manager;

import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.gui.components.*;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class GuiManager {
    // Region: Dependencies
    private final ConfigManager configManager;
    private final CraftalismMarket plugin;
    private final MarketUtils marketUtils;

    // Region: GUI Components
    private MarketGUI marketGui;
    private final Map<String, CategoryGUI> categoryGuis = new HashMap<>();

    public GuiManager(ConfigManager configManager, CraftalismMarket plugin, MarketUtils marketUtils) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.marketUtils = marketUtils;
        initializeGUIs();
    }

    // Region: Initialization
    private void initializeGUIs() {
        // Initialize main market GUI
        this.marketGui = new MarketGUI(
                plugin,
                configManager,
                this::handleCategorySelection
        );

        // Preload category GUIs
        configManager.getCategories().values().forEach(category ->
                categoryGuis.put(category.title(), new CategoryGUI(
                        category.title(),
                        plugin,
                        configManager,
                        this::handleItemSelection,
                        this::openMarket
                ))
        );
    }

    // Region: Public Interface
    public void openMarket(Player player) {
        marketGui.open(player);
    }

    private void handleCategorySelection(Player player, String category) {
        CategoryGUI categoryGui = categoryGuis.get(category);
        if (categoryGui != null) {
            categoryGui.open(player);
        } else {
            plugin.getLogger().warning("Attempted to open invalid category: " + category);
            player.sendMessage(Component.text("Invalid category!", NamedTextColor.RED));
        }
    }

    private void handleItemSelection(Player player, String itemName) {
        new TradeGUI(
                itemName,
                plugin,
                configManager,
                marketUtils,
                p -> returnToCategory(p, getItemCategory(itemName)), this
        ).open(player);
    }

    // Region: Navigation Helpers
    private void returnToCategory(Player player, String category) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            CategoryGUI categoryGui = categoryGuis.get(category);
            if (categoryGui != null && player.isOnline()) {
                categoryGui.open(player);
            } else if (player.isOnline()) {
                openMarket(player);
            }
        }, 1L);
    }

    public void refreshCategoryItem(String category, String itemName) {
        CategoryGUI categoryGui = categoryGuis.get(category);
        if (categoryGui != null) {
            categoryGui.refreshItem(itemName);
        }
    }

    private String getItemCategory(String itemName) {
        return configManager.getItems().get(itemName).getCategory();
    }
}