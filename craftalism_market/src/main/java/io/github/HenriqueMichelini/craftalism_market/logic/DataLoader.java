package io.github.HenriqueMichelini.craftalism_market.logic;

import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.model.MarketCategoryItem;
import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

            String category = itemData.getString("category");
            String materialName = itemData.getString("material");
            int slot = itemData.getInt("slot");
            BigDecimal price = BigDecimal.valueOf(itemData.getDouble("price"));
            int amount = itemData.getInt("amount");

            if (category == null || materialName == null) {
                plugin.getLogger().warning("Invalid data for item: " + itemKey);
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Invalid material: " + materialName);
                continue;
            }

            marketItems.put(itemKey, new MarketItem(category, material, slot, price, amount));
        }
    }

    public Map<String, MarketCategoryItem> getMarketCategories() { return marketCategories; }
    public Map<String, MarketItem> getMarketItems() { return marketItems; }
}
