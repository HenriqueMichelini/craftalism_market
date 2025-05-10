package io.github.HenriqueMichelini.craftalism_market;

import io.github.HenriqueMichelini.craftalism_economy.CraftalismEconomy;
import io.github.HenriqueMichelini.craftalism_economy.economy.managers.EconomyManager;
import io.github.HenriqueMichelini.craftalism_economy.economy.util.MoneyFormat;
import io.github.HenriqueMichelini.craftalism_market.command.MarketCommand;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.gui.manager.GuiManager;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketMath;
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
    private StockHandler stockHandler;
    private MoneyFormat moneyFormat;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Disabling plugin due to missing economy dependency");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(getDataFolder());
        initializeStockHandler();
        initializeComponents();
        registerCommands();
        initializeAutoSave();

        getLogger().info("Craftalism Market has been enabled!");
    }

    @Override
    public void onDisable() {
        configManager.saveItems();
        getLogger().info("Market data saved successfully!");
        instance = null;
    }

    private void initializeAutoSave() {
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
        this.moneyFormat = ((CraftalismEconomy) economyPlugin).getMoneyFormat();
        return true;
    }

    private void initializeComponents() {
        MarketMath marketMath = new MarketMath();
        guiManager = new GuiManager(configManager, this, marketMath, stockHandler, moneyFormat);
    }

    private void initializeStockHandler() {
        this.stockHandler = new StockHandler(configManager, moneyFormat);
        long checkIntervalTicks = 20L * 5;
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