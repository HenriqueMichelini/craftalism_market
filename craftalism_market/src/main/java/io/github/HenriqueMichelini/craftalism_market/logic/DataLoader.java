package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.models.Category;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
    private final Map<String, Category> marketCategories = new HashMap<>();
    private final Map<String, MarketItem> marketItems = new HashMap<>();

    public DataLoader(CraftalismMarket plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads market category items from configuration
     * @param config The FileConfiguration to load from
     */
    public void loadMarketCategories(FileConfiguration config) {
        ConfigurationSection itemsSection = getConfigurationSection(config, "items");
        if (itemsSection == null) return;

        for (String itemKey : itemsSection.getKeys(false)) {
            ConfigurationSection itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            processMarketCategoryItem(itemKey, itemData);
        }
    }

    /**
     * Loads market items from configuration
     * @param config The FileConfiguration to load from
     */
    public void loadItemsData(FileConfiguration config) {
        ConfigurationSection itemsSection = getConfigurationSection(config, "items");
        if (itemsSection == null) return;

        for (String itemKey : itemsSection.getKeys(false)) {
            ConfigurationSection itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            processMarketItem(itemKey, itemData);
        }
    }

    private void processMarketCategoryItem(String itemKey, ConfigurationSection itemData) {
        String materialName = itemData.getString("material");
        String title = itemData.getString("title");
        int slot = itemData.getInt("slot");

        if (materialName == null || title == null) {
            logInvalidItem(itemKey);
            return;
        }

        Material material = validateMaterial(materialName, itemKey);
        if (material != null) {
            marketCategories.put(itemKey, new Category(material, title, slot));
        }
    }

    private void processMarketItem(String itemKey, ConfigurationSection itemData) {
        String category = itemData.getString("category");
        String materialName = itemData.getString("material");
        int slot = itemData.getInt("slot");

        BigDecimal basePrice = getBigDecimal(itemData, "basePrice");
        BigDecimal priceVariation = getBigDecimal(itemData, "priceVariationPerOperation");
        BigDecimal sellTax = getBigDecimal(itemData, "sellTax");

        int amount = itemData.getInt("amount");
        long lastActivity = itemData.getLong("lastActivity");
        List<BigDecimal> priceHistory = getPriceHistory(itemData);

        if (category == null || materialName == null) {
            logInvalidItem(itemKey);
            return;
        }

        Material material = validateMaterial(materialName, itemKey);
        if (material != null) {
            marketItems.put(itemKey, new MarketItem(
                    category,
                    material,
                    slot,
                    basePrice,
                    priceVariation,
                    sellTax,
                    amount,
                    lastActivity,
                    priceHistory
            ));
        }
    }

    private ConfigurationSection getConfigurationSection(FileConfiguration config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            plugin.getLogger().warning("Missing configuration section: " + path);
        }
        return section;
    }

    private BigDecimal getBigDecimal(ConfigurationSection section, String path) {
        return BigDecimal.valueOf(section.getDouble(path));
    }

    private List<BigDecimal> getPriceHistory(ConfigurationSection itemData) {
        return itemData.getDoubleList("price_history").stream()
                .map(BigDecimal::valueOf)
                .collect(Collectors.toList());
    }

    private void logInvalidItem(String itemKey) {
        plugin.getLogger().warning("Skipping invalid item configuration: " + itemKey);
    }

    private Material validateMaterial(String materialName, String itemKey) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Invalid material '" + materialName + "' for item: " + itemKey);
        }
        return material;
    }

    public Map<String, Category> getMarketCategories() {
        return Collections.unmodifiableMap(marketCategories);
    }

    public Map<String, MarketItem> getMarketItems() {
        return Collections.unmodifiableMap(marketItems);
    }
}