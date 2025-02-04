package io.github.HenriqueMichelini.craftalism_market.util;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketItemData {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private Material MaterialName;

    public static class ItemData {
        public Material material;
        public String category;
        public BigDecimal price;
        public int amount;
        public int maxAmount;
        public int regenerationRate;
        public double priceAdjustmentFactor;
        public double regenAdjustmentFactor;
        public double decayRate;
        public double productivity;
        public long lastActivity;
        public List<BigDecimal> priceHistory = new ArrayList<>();

        public ItemData(Map<String, Object> data) {
            this.material = Material.valueOf((String) data.get("material"));
            this.category = (String) data.get("category");
            this.price = (BigDecimal) data.get("price");
            this.amount = (int) data.get("amount");
            this.maxAmount = (int) data.get("maxAmount");
            this.regenerationRate = (int) data.get("regenerationRate");
            this.priceAdjustmentFactor = (double) data.get("priceAdjustmentFactor");
            this.regenAdjustmentFactor = (double) data.get("regenAdjustmentFactor");
            this.decayRate = (double) data.get("decayRate");
            this.productivity = (double) data.get("productivity");
            this.lastActivity = (long) data.get("lastActivity");
            this.priceHistory = (List<BigDecimal>) data.get("price_history");
        }
    }

    public MarketItemData(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "items_data.yml");
        if (!configFile.exists()) {
            plugin.saveResource("items_data.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Initialize default data if empty
        if (config.getKeys(false).isEmpty()) {
            setupDefaultData();
        }
    }

    private void setupDefaultData() {
        Map<String, Object> MaterialNameData = new LinkedHashMap<>();
        MaterialNameData.put("material", MaterialName.name());
        MaterialNameData.put("category", "Natural Resources");
        MaterialNameData.put("price", 0.37);
        MaterialNameData.put("amount", 2000);
        MaterialNameData.put("maxAmount", 2000);
        MaterialNameData.put("regenerationRate", 50);
        MaterialNameData.put("priceAdjustmentFactor", 0.1);
        MaterialNameData.put("regenAdjustmentFactor", 0.05);
        MaterialNameData.put("decayRate", 0.02);
        MaterialNameData.put("productivity", 1.00);
        MaterialNameData.put("lastActivity", System.currentTimeMillis());
        MaterialNameData.put("price_history", List.of(0.37));

        config.set(MaterialName.name().toLowerCase(), MaterialNameData);
        saveConfig();
    }

    public ItemData getMarketItemData(Material material) {
        String materialKey = material.name().toLowerCase();
        if (!config.contains(materialKey)) return null;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("material", config.getString(materialKey + ".material"));
        data.put("category", config.getString(materialKey + ".category"));
        data.put("price", config.getDouble(materialKey + ".price"));
        data.put("amount", config.getInt(materialKey + ".amount"));
        data.put("maxAmount", config.getInt(materialKey + ".maxAmount"));
        data.put("regenerationRate", config.getInt(materialKey + ".regenerationRate"));
        data.put("priceAdjustmentFactor", config.getDouble(materialKey + ".priceAdjustmentFactor"));
        data.put("regenAdjustmentFactor", config.getDouble(materialKey + ".regenAdjustmentFactor"));
        data.put("decayRate", config.getDouble(materialKey + ".decayRate"));
        data.put("productivity", config.getDouble(materialKey + ".productivity"));
        data.put("lastActivity", config.getLong(materialKey + ".lastActivity"));
        data.put("price_history", config.getDoubleList(materialKey + ".price_history"));

        return new ItemData(data);
    }

    public void saveItemData(Material material, ItemData itemData) {
        String materialKey = material.name().toLowerCase();

        config.set(materialKey + ".material", itemData.material.name());
        config.set(materialKey + ".category", itemData.category);
        config.set(materialKey + ".price", itemData.price);
        config.set(materialKey + ".amount", itemData.amount);
        config.set(materialKey + ".maxAmount", itemData.maxAmount);
        config.set(materialKey + ".regenerationRate", itemData.regenerationRate);
        config.set(materialKey + ".priceAdjustmentFactor", itemData.priceAdjustmentFactor);
        config.set(materialKey + ".regenAdjustmentFactor", itemData.regenAdjustmentFactor);
        config.set(materialKey + ".decayRate", itemData.decayRate);
        config.set(materialKey + ".productivity", itemData.productivity);
        config.set(materialKey + ".lastActivity", itemData.lastActivity);
        config.set(materialKey + ".price_history", itemData.priceHistory);

        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save items_data.yml: " + e.getMessage());
        }
    }

    // Additional utility methods
    public BigDecimal getCurrentPrice(Material material) {
        ItemData data = getMarketItemData(material);
        return data != null ? data.price : BigDecimal.ZERO;
    }

    public void updatePrice(Material material, BigDecimal newPrice) {
        ItemData data = getMarketItemData(material);
        if (data == null) return;

        data.priceHistory.add(newPrice);
        // Keep only last 30 price entries
        if (data.priceHistory.size() > 30) {
            data.priceHistory = data.priceHistory.subList(data.priceHistory.size() - 30, data.priceHistory.size());
        }
        data.price = newPrice;
        saveItemData(material, data);
    }

    public boolean itemExists(Material material) {
        return config.contains(material.name().toLowerCase());
    }
}