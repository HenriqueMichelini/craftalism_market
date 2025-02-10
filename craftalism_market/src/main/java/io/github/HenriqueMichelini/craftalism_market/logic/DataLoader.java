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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataLoader {
    private final CraftalismMarket plugin;
    private final Map<String, MarketCategoryItem> marketCategories = new HashMap<>();
    private final Map<String, MarketItem> marketItems = new HashMap<>();

    public DataLoader(CraftalismMarket plugin) {
        this.plugin = plugin;
    }

    public void loadMarketCategories() {
        File file = new File(plugin.getDataFolder(), "market_category_items.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("market_category_items.yml does not exist!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        var itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            plugin.getLogger().warning("Invalid items section in market_category_items.yml");
            return;
        }

        for (String itemKey : itemsSection.getKeys(false)) {
            var itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            String materialName = itemData.getString("material");
            String title = itemData.getString("title");
            int slot = itemData.getInt("slot");

            if (materialName == null || title == null) {
                plugin.getLogger().warning("Invalid data for item: " + itemKey);
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Invalid material: " + materialName);
                continue;
            }

            marketCategories.put(itemKey, new MarketCategoryItem(material, title, slot));
        }
    }

    public void loadItemsData() {
        File file = new File(plugin.getDataFolder(), "items_data.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("items_data.yml does not exist!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        var itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            plugin.getLogger().warning("Invalid items section in items_data.yml");
            return;
        }

        for (String itemKey : itemsSection.getKeys(false)) {
            var itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            // Load all fields from YAML
            String category = itemData.getString("category");
            String materialName = itemData.getString("material");
            int slot = itemData.getInt("slot");
            BigDecimal currentBuyPrice = BigDecimal.valueOf(itemData.getDouble("currentBuyPrice"));
            BigDecimal currentSellPrice = BigDecimal.valueOf(itemData.getDouble("currentSellPrice"));
            BigDecimal buySellPriceRatio = BigDecimal.valueOf(itemData.getDouble("buySellPriceRatio"));
            int currentAmount = itemData.getInt("currentAmount");
            long lastActivity = itemData.getLong("lastActivity");

            // Load price_history
            List<Double> priceHistoryDouble = itemData.getDoubleList("price_history");
            List<BigDecimal> priceHistory = priceHistoryDouble.stream()
                    .map(BigDecimal::valueOf)
                    .collect(Collectors.toList());

            if (category == null || materialName == null) {
                plugin.getLogger().warning("Invalid data for item: " + itemKey);
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Invalid material: " + materialName);
                continue;
            }

            // Create MarketItem with all parameters
            marketItems.put(itemKey, new MarketItem(
                    category,
                    material,
                    slot,
                    currentBuyPrice,
                    currentSellPrice,
                    buySellPriceRatio,
                    currentAmount,
                    lastActivity,
                    priceHistory
            ));
        }
    }

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
            itemSection.set("currentBuyPrice", item.getCurrentBuyPrice().doubleValue());
            itemSection.set("currentSellPrice", item.getCurrentSellPrice().doubleValue());
            itemSection.set("buySellPriceRatio", item.getBuySellPriceRatio());
            itemSection.set("currentAmount", item.getCurrentAmount());
            itemSection.set("lastActivity", item.getLastActivity());

            // Convert BigDecimal price history to Double list
            List<Double> priceHistory = item.getPriceHistory().stream()
                    .map(BigDecimal::doubleValue)
                    .collect(Collectors.toList());
            itemSection.set("price_history", priceHistory);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save items_data.yml: " + e.getMessage());
        }
    }

    public Map<String, MarketCategoryItem> getMarketCategories() { return marketCategories; }
    public Map<String, MarketItem> getMarketItems() { return marketItems; }
}
