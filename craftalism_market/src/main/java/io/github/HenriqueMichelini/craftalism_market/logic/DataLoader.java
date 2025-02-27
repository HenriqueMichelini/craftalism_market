package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.model.MarketCategoryItem;
import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles loading and saving market data from configuration files.
 */
public class DataLoader {
    private final CraftalismMarket plugin;
    private final Map<String, MarketCategoryItem> marketCategories = new HashMap<>();
    private final Map<String, MarketItem> marketItems = new HashMap<>();

    /**
     * Initializes the DataLoader with the plugin instance.
     *
     * @param plugin The CraftalismMarket plugin instance.
     */
    public DataLoader(CraftalismMarket plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads market category items from the configuration file.
     */
    public void loadMarketCategories() {
        ConfigurationSection itemsSection = loadConfigurationSection(
                new File(plugin.getDataFolder(), "market_category_items.yml"),
                "market_category_items.yml",
                "items"
        );
        if (itemsSection == null) return;

        for (String itemKey : itemsSection.getKeys(false)) {
            ConfigurationSection itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            String materialName = itemData.getString("material");
            String title = itemData.getString("title");
            int slot = itemData.getInt("slot");

            if (materialName == null || title == null) {
                plugin.getLogger().warning("Invalid data for item: " + itemKey);
                continue;
            }

            Material material = validateMaterial(materialName, itemKey);
            if (material == null) continue;

            marketCategories.put(itemKey, new MarketCategoryItem(material, title, slot));
        }
    }

    /**
     * Loads market items from the configuration file.
     */
    public void loadItemsData() {
        ConfigurationSection itemsSection = loadConfigurationSection(
                new File(plugin.getDataFolder(), "items_data.yml"),
                "items_data.yml",
                "items"
        );
        if (itemsSection == null) return;

        for (String itemKey : itemsSection.getKeys(false)) {
            ConfigurationSection itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            String category = itemData.getString("category");
            String materialName = itemData.getString("material");
            int slot = itemData.getInt("slot");
            BigDecimal basePrice = BigDecimal.valueOf(itemData.getDouble("basePrice"));
            BigDecimal priceVariationPerOperation = BigDecimal.valueOf(itemData.getDouble("priceVariationPerOperation"));
            BigDecimal sellTax = BigDecimal.valueOf(itemData.getDouble("sellTax"));
            int amount = itemData.getInt("amount");
            long lastActivity = itemData.getLong("lastActivity");

            List<Double> priceHistoryDouble = itemData.getDoubleList("price_history");
            List<BigDecimal> priceHistory = priceHistoryDouble.stream()
                    .map(BigDecimal::valueOf)
                    .collect(Collectors.toList());

            if (category == null || materialName == null) {
                plugin.getLogger().warning("Invalid data for item: " + itemKey);
                continue;
            }

            Material material = validateMaterial(materialName, itemKey);
            if (material == null) continue;

            marketItems.put(itemKey, new MarketItem(
                    category,
                    material,
                    slot,
                    basePrice,
                    priceVariationPerOperation,
                    sellTax,
                    amount,
                    lastActivity,
                    priceHistory
            ));
        }
    }

    /**
     * Saves market items to the configuration file.
     */
    public void saveItemsData() {
        File file = new File(plugin.getDataFolder(), "items_data.yml");
        YamlConfiguration config = new YamlConfiguration();

        ConfigurationSection itemsSection = config.createSection("items");

        for (Map.Entry<String, MarketItem> entry : marketItems.entrySet()) {
            String itemKey = entry.getKey();
            MarketItem item = entry.getValue();

            ConfigurationSection itemSection = itemsSection.createSection(itemKey);
            itemSection.set("category", item.getCategory());
            itemSection.set("material", item.getMaterial().name());
            itemSection.set("slot", item.getSlot());
            itemSection.set("basePrice", item.getBasePrice().toString()); // Preserve precision
            itemSection.set("priceVariationPerOperation", item.getPriceVariationPerOperation().toString());
            itemSection.set("sellTax", item.getSellTax().toString());
            itemSection.set("amount", item.getAmount());
            itemSection.set("lastActivity", item.getLastActivity());

            List<String> priceHistory = item.getPriceHistory().stream()
                    .map(BigDecimal::toString)
                    .collect(Collectors.toList());
            itemSection.set("price_history", priceHistory);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save items_data.yml: " + e.getMessage());
            throw new RuntimeException("Failed to save items_data.yml", e); // Rethrow for caller handling
        }
    }

    /**
     * Returns an unmodifiable view of the market categories.
     *
     * @return An unmodifiable map of market categories.
     */
    public Map<String, MarketCategoryItem> getMarketCategories() {
        return Collections.unmodifiableMap(marketCategories);
    }

    /**
     * Returns an unmodifiable view of the market items.
     *
     * @return An unmodifiable map of market items.
     */
    public Map<String, MarketItem> getMarketItems() {
        return Collections.unmodifiableMap(marketItems);
    }

    /**
     * Loads a configuration section from a file.
     *
     * @param file       The file to load.
     * @param fileName   The name of the file (for logging).
     * @param sectionPath The path to the configuration section.
     * @return The loaded configuration section, or null if the file or section is invalid.
     */
    private ConfigurationSection loadConfigurationSection(File file, String fileName, String sectionPath) {
        if (!file.exists()) {
            plugin.getLogger().warning(fileName + " does not exist!");
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection(sectionPath);
        if (section == null) {
            plugin.getLogger().warning("Invalid section in " + fileName);
        }
        return section;
    }

    /**
     * Validates a material name and logs a warning if invalid.
     *
     * @param materialName The name of the material.
     * @param itemKey      The key of the item (for logging).
     * @return The validated Material, or null if invalid.
     */
    private Material validateMaterial(String materialName, String itemKey) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Invalid material: " + materialName + " for item: " + itemKey);
        }
        return material;
    }
}