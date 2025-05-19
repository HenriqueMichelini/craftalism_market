package io.github.HenriqueMichelini.craftalism_market.config.loader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileLoader {
    private static final Logger LOGGER = Logger.getLogger(FileLoader.class.getName());

    private final File configFolder;
    private YamlConfiguration categoriesConfig;
    private YamlConfiguration itemsConfig;
    private YamlConfiguration mainConfig;

    public FileLoader(File configFolder) {
        this.configFolder = configFolder;
    }

    public File getConfigFolder() {
        return configFolder;
    }

    public void loadFiles() {
        loadMainConfig();
        loadCategories();
        loadItems();
    }

    private void loadMainConfig() {
        File file = new File(configFolder, "config.yml");
        try {
            if (!file.exists()) createDefaultConfig(file);
            mainConfig = YamlConfiguration.loadConfiguration(file);
        } catch (IOException | InvalidConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Failed to load main config", e);
        }
    }

    private void createDefaultConfig(File file) throws IOException, InvalidConfigurationException {
        String defaultConfig = """
        # Stock update interval in minutes
        stock-update-interval: 10
        
        # Maximum stock overflow multiplier (2.0 = 200% of base stock)
        max-stock-overflow: 2.0
        
        # Number of decimal places for prices
        price-decimal-places: 2
       
        """;

        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(defaultConfig);
        config.save(file);
    }

    public YamlConfiguration getMainConfig() {
        return mainConfig;
    }

    private void loadCategories() {
        File file = new File(configFolder, "categories.yml");
        try {
            if (!file.exists()) createDefaultCategories(file);
            categoriesConfig = YamlConfiguration.loadConfiguration(file);
        } catch (IOException | InvalidConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Failed to load categories config", e);
        }
    }

    private void loadItems() {
        File file = new File(configFolder, "items.yml");
        try {
            if (!file.exists()) createDefaultItems(file);
            itemsConfig = YamlConfiguration.loadConfiguration(file);
        } catch (IOException | InvalidConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Failed to load items config", e);
        }
    }

    private void createDefaultCategories(File file) throws IOException, InvalidConfigurationException {
        String defaultConfig =
                """
                        categories:
                          stone:
                            material: STONE
                            category: natural_resources
                            title: Natural Resources
                            slot: 19
                          cyan_dye:
                            material: CYAN_DYE
                            category: dyes
                            title: dyes
                            slot: 29
                          oak_log:
                            material: OAK_LOG
                            category: woods
                            title: Woods
                            slot: 21
                          wheat:
                            material: WHEAT
                            category: livestock
                            title: Livestock
                            slot: 31
                          purple_wool:
                            material: PURPLE_WOOL
                            category: wools
                            title: Wools
                            slot: 23
                          string:
                            material: STRING
                            category: mob_drops
                            title: Mob Drops
                            slot: 33
                          emerald:
                            material: EMERALD
                            category: ores
                            title: Ores
                            slot: 25""";

        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(defaultConfig);
        config.save(file);
    }

    private void createDefaultItems(File file) throws IOException, InvalidConfigurationException {
        String defaultConfig =
                """
                        items:
                          cobblestone:
                            material: COBBLESTONE
                            category: natural_resources
                            slot: 0
                            base_price: 3700
                            current_price: 3700
                            price_variation: 15
                            tax_rate: 0.15
                            original_stock: 2000
                            base_stock: 2000
                            current_stock: 2000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 3700
                          dirt:
                            material: DIRT
                            category: natural_resources
                            slot: 1
                            base_price: 1000
                            current_price: 1000
                            price_variation: 5
                            tax_rate: 0.1
                            original_stock: 5000
                            base_stock: 5000
                            current_stock: 5000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 1000
                          sand:
                            material: SAND
                            category: natural_resources
                            slot: 2
                            base_price: 2500
                            current_price: 2500
                            price_variation: 10
                            tax_rate: 0.12
                            original_stock: 3000
                            base_stock: 3000
                            current_stock: 3000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 2500
                          gravel:
                            material: GRAVEL
                            category: natural_resources
                            slot: 3
                            base_price: 2000
                            current_price: 2000
                            price_variation: 8
                            tax_rate: 0.12
                            original_stock: 2500
                            base_stock: 2500
                            current_stock: 2500
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 2000
                          clay_ball:
                            material: CLAY_BALL
                            category: natural_resources
                            slot: 4
                            base_price: 5000
                            current_price: 5000
                            price_variation: 20
                            tax_rate: 0.15
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 5000
                          coal:
                            material: COAL
                            category: ores
                            slot: 0
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1500
                            base_stock: 1500
                            current_stock: 1500
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          iron_ingot:
                            material: IRON_INGOT
                            category: ores
                            slot: 1
                            base_price: 50000
                            current_price: 50000
                            price_variation: 10
                            tax_rate: 0.25
                            original_stock: 800
                            base_stock: 800
                            current_stock: 800
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 50000
                          gold_ingot:
                            material: GOLD_INGOT
                            category: ores
                            slot: 2
                            base_price: 100000
                            current_price: 100000
                            price_variation: 200
                            tax_rate: 0.30
                            original_stock: 500
                            base_stock: 500
                            current_stock: 500
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 100000
                          diamond:
                            material: DIAMOND
                            category: ores
                            slot: 3
                            base_price: 500000
                            current_price: 500000
                            price_variation: 500
                            tax_rate: 0.35
                            original_stock: 200
                            base_stock: 200
                            current_stock: 200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 500000
                          emerald:
                            material: EMERALD
                            category: ores
                            slot: 4
                            base_price: 1000000
                            current_price: 1000000
                            price_variation: 1000
                            tax_rate: 0.40
                            original_stock: 100
                            base_stock: 100
                            current_stock: 100
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 1000000
                          string:
                            material: STRING
                            category: mob_drops
                            slot: 0
                            base_price: 5000
                            current_price: 5000
                            price_variation: 20
                            tax_rate: 0.15
                            original_stock: 1500
                            base_stock: 1500
                            current_stock: 1500
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 5000
                          spider_eye:
                            material: SPIDER_EYE
                            category: mob_drops
                            slot: 1
                            base_price: 150000
                            current_price: 150000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 150000
                          bone:
                            material: BONE
                            category: mob_drops
                            slot: 2
                            base_price: 7500
                            current_price: 7500
                            price_variation: 30
                            tax_rate: 0.18
                            original_stock: 1200
                            base_stock: 1200
                            current_stock: 1200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 7500
                          gunpowder:
                            material: GUNPOWDER
                            category: mob_drops
                            slot: 3
                            base_price: 200000
                            current_price: 200000
                            price_variation: 75
                            tax_rate: 0.22
                            original_stock: 800
                            base_stock: 800
                            current_stock: 800
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 200000
                          ender_pearl:
                            material: ENDER_PEARL
                            category: mob_drops
                            slot: 4
                            base_price: 100000
                            current_price: 100000
                            price_variation: 200
                            tax_rate: 0.30
                            original_stock: 300
                            base_stock: 300
                            current_stock: 300
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 100000
                          oak_log:
                            material: OAK_LOG
                            category: woods
                            slot: 0
                            base_price: 5000
                            current_price: 5000
                            price_variation: 20
                            tax_rate: 0.15
                            original_stock: 2000
                            base_stock: 2000
                            current_stock: 2000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 5000
                          spruce_log:
                            material: SPRUCE_LOG
                            category: woods
                            slot: 1
                            base_price: 5500
                            current_price: 5500
                            price_variation: 22
                            tax_rate: 0.15
                            original_stock: 1800
                            base_stock: 1800
                            current_stock: 1800
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 5500
                          birch_log:
                            material: BIRCH_LOG
                            category: woods
                            slot: 2
                            base_price: 6000
                            current_price: 6000
                            price_variation: 25
                            tax_rate: 0.15
                            original_stock: 1600
                            base_stock: 1600
                            current_stock: 1600
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 6000
                          jungle_log:
                            material: JUNGLE_LOG
                            category: woods
                            slot: 3
                            base_price: 6500
                            current_price: 6500
                            price_variation: 28
                            tax_rate: 0.15
                            original_stock: 1400
                            base_stock: 1400
                            current_stock: 1400
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 6500
                          acacia_log:
                            material: ACACIA_LOG
                            category: woods
                            slot: 4
                            base_price: 7000
                            current_price: 7000
                            price_variation: 30
                            tax_rate: 0.15
                            original_stock: 1200
                            base_stock: 1200
                            current_stock: 1200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 7000
                          red_dye:
                            material: RED_DYE
                            category: dyes
                            slot: 0
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          blue_dye:
                            material: BLUE_DYE
                            category: dyes
                            slot: 1
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          green_dye:
                            material: GREEN_DYE
                            category: dyes
                            slot: 2
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          yellow_dye:
                            material: YELLOW_DYE
                            category: dyes
                            slot: 3
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          purple_dye:
                            material: PURPLE_DYE
                            category: dyes
                            slot: 4
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          wheat:
                            material: WHEAT
                            category: livestock
                            slot: 0
                            base_price: 3000
                            current_price: 3000
                            price_variation: 10
                            tax_rate: 0.10
                            original_stock: 3000
                            base_stock: 3000
                            current_stock: 3000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 3000
                          carrot:
                            material: CARROT
                            category: livestock
                            slot: 1
                            base_price: 5000
                            current_price: 5000
                            price_variation: 20
                            tax_rate: 0.15
                            original_stock: 2000
                            base_stock: 2000
                            current_stock: 2000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 5000
                          potato:
                            material: POTATO
                            category: livestock
                            slot: 2
                            base_price: 4000
                            current_price: 4000
                            price_variation: 15
                            tax_rate: 0.12
                            original_stock: 2500
                            base_stock: 2500
                            current_stock: 2500
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 4000
                          beef:
                            material: BEEF
                            category: livestock
                            slot: 3
                            base_price: 20000
                            current_price: 20000
                            price_variation: 75
                            tax_rate: 0.20
                            original_stock: 1000
                            base_stock: 1000
                            current_stock: 1000
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 20000
                          chicken:
                            material: CHICKEN
                            category: livestock
                            slot: 4
                            base_price: 150000
                            current_price: 150000
                            price_variation: 50
                            tax_rate: 0.18
                            original_stock: 1200
                            base_stock: 1200
                            current_stock: 1200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 150000
                          white_wool:
                            material: WHITE_WOOL
                            category: wools
                            slot: 0
                            base_price: 7500
                            current_price: 7500
                            price_variation: 30
                            tax_rate: 0.15
                            original_stock: 1500
                            base_stock: 1500
                            current_stock: 1500
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 7500
                          black_wool:
                            material: BLACK_WOOL
                            category: wools
                            slot: 1
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1200
                            base_stock: 1200
                            current_stock: 1200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          red_wool:
                            material: RED_WOOL
                            category: wools
                            slot: 2
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1200
                            base_stock: 1200
                            current_stock: 1200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          blue_wool:
                            material: BLUE_WOOL
                            category: wools
                            slot: 3
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1200
                            base_stock: 1200
                            current_stock: 1200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                          green_wool:
                            material: GREEN_WOOL
                            category: wools
                            slot: 4
                            base_price: 10000
                            current_price: 10000
                            price_variation: 50
                            tax_rate: 0.20
                            original_stock: 1200
                            base_stock: 1200
                            current_stock: 1200
                            stock_surplus: 0
                            stock_regeneration_multiplier: 1
                            stock_regeneration_rate: 0.05
                            next_update_time: 0
                            last_activity: 0
                            price_history:
                              - 10000
                        """;

        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(defaultConfig);
        config.save(file);
    }

    public void saveMainConfig() throws IOException {
        File configFile = new File(configFolder, "config.yml");
        try {
            mainConfig.save(configFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save main config", e);
            throw e;
        }
    }

    public YamlConfiguration getCategoriesConfig() {
        return categoriesConfig;
    }

    public YamlConfiguration getItemsConfig() {
        return itemsConfig;
    }
}