package io.github.HenriqueMichelini.craftalism_market.config;

import io.github.HenriqueMichelini.craftalism_market.config.loader.DataParser;
import io.github.HenriqueMichelini.craftalism_market.config.loader.FileLoader;
import io.github.HenriqueMichelini.craftalism_market.config.validation.SchemaValidator;
import io.github.HenriqueMichelini.craftalism_market.models.Category;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.Map;

public class ConfigManager {
    private final FileLoader fileLoader;
    private final DataParser dataParser;
    private YamlConfiguration mainConfig;

    public ConfigManager(File dataFolder) {
        this.fileLoader = new FileLoader(dataFolder);
        this.dataParser = new DataParser(fileLoader);
        reload();
    }

    public void reload() {
        // Load all configuration files
        fileLoader.loadFiles();

        // Get main config reference
        this.mainConfig = fileLoader.getMainConfig();

        // Validate data files
        SchemaValidator.validateCategories(fileLoader.getCategoriesConfig());
        SchemaValidator.validateItems(fileLoader.getItemsConfig());

        // Parse data
        dataParser.parseData();
    }

    // New config access methods
    public int getStockUpdateInterval() {
        return mainConfig.getInt("stock-update-interval", 10);
    }

    public double getMaxStockOverflow() {
        return mainConfig.getDouble("max-stock-overflow", 2.0);
    }

    public int getPriceDecimalPlaces() {
        return mainConfig.getInt("price-decimal-places", 2);
    }

    // Existing data access methods
    public Map<String, Category> getCategories() {
        return dataParser.getCategories();
    }

    public Map<String, MarketItem> getItems() {
        return dataParser.getItems();
    }
}