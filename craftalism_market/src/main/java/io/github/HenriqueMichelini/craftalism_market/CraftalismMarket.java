package io.github.HenriqueMichelini.craftalism_market;

import io.github.HenriqueMichelini.craftalism_economy.CraftalismEconomy;
import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.command.MarketCommand;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.gui.manager.GuiManager;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketUtils;
import io.github.HenriqueMichelini.craftalism_market.stock.StockHandler;
import io.github.HenriqueMichelini.craftalism_market.task.StockUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CraftalismMarket extends JavaPlugin {
    private static final String ECONOMY_PLUGIN_NAME = "CraftalismEconomy";
    private static CraftalismMarket instance;
    private ConfigManager configManager;
    private EconomyManager economyManager;
    private GuiManager guiManager;
    private MarketUtils marketUtils;
    private StockHandler stockHandler;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Disabling plugin due to missing economy dependency");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Fix 2: Change initialization order
        configManager = new ConfigManager(getDataFolder());
        initializeStockHandler();  // Must come first
        initializeComponents();
        registerCommands();
        initializeAutoSave();

        getLogger().info("Craftalism Market has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all market data
        configManager.saveItems();
        getLogger().info("Market data saved successfully!");
        instance = null;
    }

    private void initializeAutoSave() {
        // Save every 5 minutes (300 seconds * 20 ticks)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            configManager.saveItems();
            getLogger().fine("Auto-saved market data");
        }, 0L, 300 * 20L);
    }

    private boolean setupEconomy() {
        Plugin economyPlugin = Bukkit.getPluginManager().getPlugin(ECONOMY_PLUGIN_NAME);
        if (!(economyPlugin instanceof CraftalismEconomy)) {
            getLogger().severe(ECONOMY_PLUGIN_NAME + " not found or incompatible!");
            return false;
        }
        economyManager = ((CraftalismEconomy) economyPlugin).getEconomyManager();
        return true;
    }

    // Update initializeComponents() (remove configManager creation)
    private void initializeComponents() {
        // Fix 3: StockHandler is now initialized first
        marketUtils = new MarketUtils(stockHandler);
        guiManager = new GuiManager(configManager, this, marketUtils, stockHandler);
    }

    private void initializeStockHandler() {
        this.stockHandler = new StockHandler(configManager);
        long checkIntervalTicks = 20L; // Check every 1 second (20 ticks)
        new StockUpdateTask(stockHandler).runTaskTimer(this, 0L, checkIntervalTicks);
    }

    private void registerCommands() {
        MarketCommand marketCommand = new MarketCommand(guiManager);
        Objects.requireNonNull(getCommand("market")).setExecutor(marketCommand);
    }

    //region Getters
    public static CraftalismMarket getInstance() {
        return instance;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StockHandler getStockHandler() {
        return stockHandler;
    }
    //endregion
}