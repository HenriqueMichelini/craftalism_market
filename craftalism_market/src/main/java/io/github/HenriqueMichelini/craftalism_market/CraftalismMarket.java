package io.github.HenriqueMichelini.craftalism_market;

import io.github.HenriqueMichelini.craftalism_market.command.MarketCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CraftalismMarket extends JavaPlugin {
    private static CraftalismMarket instance;
    private File itemsDataFile;
    private FileConfiguration itemsDataConfig;

    @Override
    public void onEnable() {
        instance = this;
        loadItemsData(); // Load custom items_data.yml
        getLogger().info("Craftalism Market Plugin Enabled!");
        Objects.requireNonNull(getCommand("market")).setExecutor(new MarketCommand());
    }

    @Override
    public void onDisable() {
        getLogger().info("Craftalism Market Plugin Disabled!");
    }

    public static CraftalismMarket getInstance() {
        return instance;
    }

    private void loadItemsData() {
        itemsDataFile = new File(getDataFolder(), "items_data.yml");

        if (!itemsDataFile.exists()) {
            saveResource("items_data.yml", false);
        }

        itemsDataConfig = YamlConfiguration.loadConfiguration(itemsDataFile);
    }

    public FileConfiguration getItemsDataConfig() {
        return itemsDataConfig;
    }

    public void saveItemsData() {
        try {
            itemsDataConfig.save(itemsDataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save items_data.yml!");
            e.printStackTrace();
        }
    }
}
