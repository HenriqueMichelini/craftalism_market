package io.github.HenriqueMichelini.craftalism_market.config;

import io.github.HenriqueMichelini.craftalism_market.config.loader.DataParser;
import io.github.HenriqueMichelini.craftalism_market.config.loader.FileLoader;
import io.github.HenriqueMichelini.craftalism_market.config.validation.SchemaValidator;
import io.github.HenriqueMichelini.craftalism_market.models.Category;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import java.io.File;
import java.util.Map;

public class ConfigManager {
    private final FileLoader fileLoader;
    private final DataParser dataParser;

    public ConfigManager(File dataFolder) {
        this.fileLoader = new FileLoader(dataFolder);
        this.dataParser = new DataParser(fileLoader);
        reload();
    }

    public void reload() {
        // Load sequence
        fileLoader.loadFiles();

        // Validate
        SchemaValidator.validateCategories(fileLoader.getCategoriesConfig());
        SchemaValidator.validateItems(fileLoader.getItemsConfig());

        // Parse data
        dataParser.parseData();
    }

    public Map<String, Category> getCategories() {
        return dataParser.getCategories();
    }

    public Map<String, MarketItem> getItems() {
        return dataParser.getItems();
    }
}