package io.github.HenriqueMichelini.craftalism_market;

import io.github.HenriqueMichelini.craftalism_economy.CraftalismEconomy;
import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.command.MarketCommand;
import io.github.HenriqueMichelini.craftalism_market.file.FileManager;
import io.github.HenriqueMichelini.craftalism_market.gui.GuiManager;
import io.github.HenriqueMichelini.craftalism_market.logic.DataLoader;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CraftalismMarket extends JavaPlugin {
    private static final String ECONOMY_PLUGIN_NAME = "CraftalismEconomy";

    private static CraftalismMarket instance;
    private FileManager fileManager;
    private DataLoader dataLoader;
    private EconomyManager economyManager;
    private GuiManager guiManager;
    private MarketManager marketManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Disabling plugin due to missing economy dependency");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        initializeComponents();
        registerCommands();

        getLogger().info("Craftalism Market has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Craftalism Market has been disabled!");
    }

    private boolean setupEconomy() {
        Plugin economyPlugin = Bukkit.getPluginManager().getPlugin(ECONOMY_PLUGIN_NAME);
        if (!(economyPlugin instanceof CraftalismEconomy)) {
            getLogger().severe(ECONOMY_PLUGIN_NAME + " not found or incompatible!");
            return false;
        }

        economyManager = ((CraftalismEconomy) economyPlugin).getEconomyManager();
        getLogger().info("Successfully hooked into " + ECONOMY_PLUGIN_NAME);
        return true;
    }

    private void initializeComponents() {
        // Initialize file management
        fileManager = new FileManager(getDataFolder());

        // Initialize data loader with file configurations
        dataLoader = new DataLoader(this);
        dataLoader.loadMarketCategories(fileManager.getMarketCategoryConfig());
        dataLoader.loadItemsData(fileManager.getItemsDataConfig());

        // Initialize market logic
        marketManager = new MarketManager();
        guiManager = new GuiManager(dataLoader, this, marketManager);
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

    public FileManager getFileManager() {
        return fileManager;
    }

    public DataLoader getDataLoader() {
        return dataLoader;
    }
    //endregion
}