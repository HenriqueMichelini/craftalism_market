package io.github.HenriqueMichelini.craftalism_market;

import io.github.HenriqueMichelini.craftalism_economy.CraftalismEconomy;
import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.command.MarketCommand;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.gui.manager.GuiManager;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketManager;
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

    private boolean setupEconomy() {
        Plugin economyPlugin = Bukkit.getPluginManager().getPlugin(ECONOMY_PLUGIN_NAME);
        if (!(economyPlugin instanceof CraftalismEconomy)) {
            getLogger().severe(ECONOMY_PLUGIN_NAME + " not found or incompatible!");
            return false;
        }
        economyManager = ((CraftalismEconomy) economyPlugin).getEconomyManager();
        return true;
    }

    private void initializeComponents() {
        // Initialize config system
        configManager = new ConfigManager(getDataFolder());

        // Initialize market logic
        marketManager = new MarketManager();
        guiManager = new GuiManager(configManager, this, marketManager);
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
    //endregion
}