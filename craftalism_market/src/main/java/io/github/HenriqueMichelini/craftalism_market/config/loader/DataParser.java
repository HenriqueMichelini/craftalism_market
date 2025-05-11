package io.github.HenriqueMichelini.craftalism_market.config.loader;

import io.github.HenriqueMichelini.craftalism_market.models.Category;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import java.util.*;
import java.util.stream.Collectors;

public class DataParser {
    private final FileLoader fileLoader;
    private final Map<String, Category> categories = new HashMap<>();
    private final Map<String, MarketItem> items = new HashMap<>();

    public DataParser(FileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    public void parseData() {
        parseCategories();
        parseItems();
    }

    private void parseCategories() {
        ConfigurationSection section = fileLoader.getCategoriesConfig()
                .getConfigurationSection("categories");

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection catSection = section.getConfigurationSection(key);
            assert catSection != null;
            Category category = new Category(
                    Material.matchMaterial(Objects.requireNonNull(catSection.getString("material"))),
                    catSection.getString("category"),
                    catSection.getString("title"),
                    catSection.getInt("slot")
            );
            categories.put(key, category);
        }
    }

    private void parseItems() {
        ConfigurationSection section = fileLoader.getItemsConfig()
                .getConfigurationSection("items");

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            assert itemSection != null;

            MarketItem item = new MarketItem(
                    itemSection.getString("category"),
                    Material.matchMaterial(Objects.requireNonNull(itemSection.getString("material"))),
                    itemSection.getInt("slot"),
                    itemSection.getLong("base_price"),
                    itemSection.getLong("current_price"),
                    itemSection.getLong("price_variation"),
                    itemSection.getDouble("tax_rate"),
                    itemSection.getInt("original_stock"),
                    itemSection.getInt("base_stock"),
                    itemSection.getInt("current_stock"),
                    itemSection.getDouble("stock_regen_rate"),
                    itemSection.getLong("next_update_time"),
                    itemSection.getInt("stock_surplus"),
                    itemSection.getLong("last_activity"),
                    parsePriceHistory(itemSection)
            );
            items.put(key, item);
        }
    }

    private List<Long> parsePriceHistory(ConfigurationSection section) {
        return section.getDoubleList("price_history").stream()
                .map(Math::round)
                .collect(Collectors.toList());
    }

    public Map<String, Category> getCategories() {
        return Collections.unmodifiableMap(categories);
    }

    public Map<String, MarketItem> getItems() {
        return Collections.unmodifiableMap(items);
    }
}