package io.github.HenriqueMichelini.craftalism_market.gui.manager;

import io.github.HenriqueMichelini.craftalism_economy.economy.util.MoneyFormat;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.gui.components.*;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketMath;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.stock.StockHandler;
import io.github.HenriqueMichelini.craftalism_market.stock.listener.StockUpdateListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GuiManager implements StockUpdateListener {
    // Region: Dependencies
    private final ConfigManager configManager;
    private final CraftalismMarket plugin;
    private final MarketMath marketMath;
    private final StockHandler stockHandler;

    // Region: GUI Components
    private MarketGUI marketGui;
    private final Map<String, CategoryGUI> categoryGuis = new HashMap<>();
    private final Map<String, Set<TradeGUI>> openTradeGuis = new HashMap<>();
    private final MoneyFormat moneyFormat;


    @Override
    public void onStockUpdated(MarketItem item) {
        String itemName = item.getMaterial().name().toLowerCase();

        // Update category GUI
        refreshCategoryItem(item.getCategory(), itemName);

        // Update open trade GUIs
        if (openTradeGuis.containsKey(itemName)) {
            new ArrayList<>(openTradeGuis.get(itemName)).forEach(TradeGUI::refresh);
        }
    }

    public void registerTradeGui(String itemName, TradeGUI gui) {
        openTradeGuis.computeIfAbsent(itemName, k -> new HashSet<>()).add(gui);
    }

    public void unregisterTradeGui(String itemName, TradeGUI gui) {
        if (openTradeGuis.containsKey(itemName)) {
            openTradeGuis.get(itemName).remove(gui);
            if (openTradeGuis.get(itemName).isEmpty()) {
                openTradeGuis.remove(itemName);
            }
        }
    }

    public GuiManager(ConfigManager configManager, CraftalismMarket plugin, MarketMath marketMath, StockHandler stockHandler, MoneyFormat moneyFormat) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.marketMath = marketMath;
        this.stockHandler = stockHandler;
        this.moneyFormat = moneyFormat;
        stockHandler.addStockUpdateListener(this);
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
                categoryGuis.put(category.category(), new CategoryGUI(
                        category.category(),
                        plugin,
                        configManager,
                        this::handleItemSelection,
                        this::openMarket,
                        moneyFormat
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
                marketMath,
                p -> returnToCategory(p, getItemCategory(itemName)),
                this,
                stockHandler,
                moneyFormat).open(player);
    }

    // Region: Navigation Helpers
    public void returnToCategory(Player player, String category) {
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