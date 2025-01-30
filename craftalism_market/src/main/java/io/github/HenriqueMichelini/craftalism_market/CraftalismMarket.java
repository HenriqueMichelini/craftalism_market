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

    private File marketCategoryFile;
    private FileConfiguration marketCategoryConfig;

    @Override
    public void onEnable() {
        instance = this;
        loadItemsData(); // Load market_items.yml
        loadMarketCategoryData(); // Load market_category_items.yml

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
        itemsDataFile = new File(getDataFolder(), "market_items.yml");

        if (!itemsDataFile.exists()) {
            saveResource("market_items.yml", false);
        }

        itemsDataConfig = YamlConfiguration.loadConfiguration(itemsDataFile);
    }

    private void loadMarketCategoryData() {
        marketCategoryFile = new File(getDataFolder(), "market_category_items.yml");

        if (!marketCategoryFile.exists()) {
            saveDefaultMarketCategory();
        }

        marketCategoryConfig = YamlConfiguration.loadConfiguration(marketCategoryFile);
    }

    private void saveDefaultMarketCategory() {
        marketCategoryFile.getParentFile().mkdirs(); // Ensure the directory exists

        try {
            if (marketCategoryFile.createNewFile()) {
                marketCategoryConfig = YamlConfiguration.loadConfiguration(marketCategoryFile);

                // Set default categories
                marketCategoryConfig.set("items.stone.material", "STONE");
                marketCategoryConfig.set("items.stone.category", "Natural Resources");
                marketCategoryConfig.set("items.stone.title", "Natural Resources");
                marketCategoryConfig.set("items.stone.slot", 19);

                marketCategoryConfig.set("items.cyan_dye.material", "CYAN_DYE");
                marketCategoryConfig.set("items.cyan_dye.category", "Dyes");
                marketCategoryConfig.set("items.cyan_dye.title", "Dyes");
                marketCategoryConfig.set("items.cyan_dye.slot", 29);

                marketCategoryConfig.set("items.oak_log.material", "OAK_LOG");
                marketCategoryConfig.set("items.oak_log.category", "Woods");
                marketCategoryConfig.set("items.oak_log.title", "Woods");
                marketCategoryConfig.set("items.oak_log.slot", 21);

                marketCategoryConfig.set("items.wheat.material", "WHEAT");
                marketCategoryConfig.set("items.wheat.category", "Livestock");
                marketCategoryConfig.set("items.wheat.title", "Livestock");
                marketCategoryConfig.set("items.wheat.slot", 31);

                marketCategoryConfig.set("items.purple_wool.material", "PURPLE_WOOL");
                marketCategoryConfig.set("items.purple_wool.category", "Wools");
                marketCategoryConfig.set("items.purple_wool.title", "Wools");
                marketCategoryConfig.set("items.purple_wool.slot", 23);

                marketCategoryConfig.set("items.string.material", "STRING");
                marketCategoryConfig.set("items.string.category", "Mob Drops");
                marketCategoryConfig.set("items.string.title", "Mob Drops");
                marketCategoryConfig.set("items.string.slot", 33);

                marketCategoryConfig.set("items.emerald.material", "EMERALD");
                marketCategoryConfig.set("items.emerald.category", "Ores");
                marketCategoryConfig.set("items.emerald.title", "Ores");
                marketCategoryConfig.set("items.emerald.slot", 25);

                marketCategoryConfig.save(marketCategoryFile);
            }
        } catch (IOException e) {
            getLogger().severe("Could not create market_category_items.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getItemsDataConfig() {
        return itemsDataConfig;
    }

    public FileConfiguration getMarketCategoryConfig() {
        return marketCategoryConfig;
    }

    public void saveItemsData() {
        try {
            itemsDataConfig.save(itemsDataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save market_items.yml!");
            e.printStackTrace();
        }
    }

    public void saveMarketCategoryData() {
        try {
            marketCategoryConfig.save(marketCategoryFile);
        } catch (IOException e) {
            getLogger().severe("Could not save market_category_items.yml!");
            e.printStackTrace();
        }
    }
}
