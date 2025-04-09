package io.github.HenriqueMichelini.craftalism_market.config;

import io.github.HenriqueMichelini.craftalism_market.config.loader.DataParser;
import io.github.HenriqueMichelini.craftalism_market.config.loader.FileLoader;
import io.github.HenriqueMichelini.craftalism_market.config.validation.SchemaValidator;
import io.github.HenriqueMichelini.craftalism_market.models.Category;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;

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

    // In ConfigManager.java
    public void saveItems() {
        File itemsFile = new File(fileLoader.getConfigFolder(), "items.yml");
        YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        // Clear existing items section
        itemsConfig.set("items", null);

        // Serialize all MarketItems
        for (Map.Entry<String, MarketItem> entry : dataParser.getItems().entrySet()) {
            String key = entry.getKey();
            MarketItem item = entry.getValue();

            String path = "items." + key + ".";
            itemsConfig.set(path + "material", item.getMaterial().name());
            itemsConfig.set(path + "category", item.getCategory());
            itemsConfig.set(path + "slot", item.getSlot());
            itemsConfig.set(path + "base_price", item.getBasePrice().doubleValue());
            itemsConfig.set(path + "current_price", item.getCurrentPrice().doubleValue());
            itemsConfig.set(path + "price_variation", item.getPriceVariationPerOperation().doubleValue());
            itemsConfig.set(path + "tax_rate", item.getTaxRate().doubleValue());
            itemsConfig.set(path + "base_stock", item.getBaseStock());
            itemsConfig.set(path + "current_stock", item.getCurrentStock());
            itemsConfig.set(path + "stock_regen_rate", item.getStockRegenRate());
            itemsConfig.set(path + "next_update_time", item.getNextUpdateTime());
            itemsConfig.set(path + "last_activity", item.getLastActivity());

            // Save price history as double list
            List<Double> priceHistory = item.getPriceHistory().stream()
                    .map(BigDecimal::doubleValue)
                    .collect(Collectors.toList());
            itemsConfig.set(path + "price_history", priceHistory);
        }

        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            getLogger().severe("Failed to save items config: " + e.getMessage());
        }
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