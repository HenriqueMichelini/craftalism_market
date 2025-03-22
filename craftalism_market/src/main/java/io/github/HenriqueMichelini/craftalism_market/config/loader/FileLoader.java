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

    public FileLoader(File configFolder) {
        this.configFolder = configFolder;
    }

    public void loadFiles() {
        loadCategories();
        loadItems();
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
                            title: Dyes
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
                            base_price: 0.37
                            price_variation: 0.0015
                            tax_rate: 0.15
                            stock: 2000
                            last_activity: 0
                            price_history:
                              - 0.37
                          dirt:
                            material: DIRT
                            category: natural_resources
                            slot: 1
                            base_price: 0.1
                            price_variation: 0.0005
                            tax_rate: 0.1
                            stock: 5000
                            last_activity: 0
                            price_history:
                              - 0.1
                          sand:
                            material: SAND
                            category: natural_resources
                            slot: 2
                            base_price: 0.25
                            price_variation: 0.001
                            tax_rate: 0.12
                            stock: 3000
                            last_activity: 0
                            price_history:
                              - 0.25
                          gravel:
                            material: GRAVEL
                            category: natural_resources
                            slot: 3
                            base_price: 0.2
                            price_variation: 0.0008
                            tax_rate: 0.12
                            stock: 2500
                            last_activity: 0
                            price_history:
                              - 0.2
                          clay_ball:
                            material: CLAY_BALL
                            category: natural_resources
                            slot: 4
                            base_price: 0.5
                            price_variation: 0.002
                            tax_rate: 0.15
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 0.5
                          coal:
                            material: COAL
                            category: ores
                            slot: 0
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1500
                            last_activity: 0
                            price_history:
                              - 1.00
                          iron_ingot:
                            material: IRON_INGOT
                            category: ores
                            slot: 1
                            base_price: 5.00
                            price_variation: 0.0100
                            tax_rate: 0.25
                            stock: 800
                            last_activity: 0
                            price_history:
                              - 5.00
                          gold_ingot:
                            material: GOLD_INGOT
                            category: ores
                            slot: 2
                            base_price: 10.00
                            price_variation: 0.0200
                            tax_rate: 0.30
                            stock: 500
                            last_activity: 0
                            price_history:
                              - 10.00
                          diamond:
                            material: DIAMOND
                            category: ores
                            slot: 3
                            base_price: 50.00
                            price_variation: 0.0500
                            tax_rate: 0.35
                            stock: 200
                            last_activity: 0
                            price_history:
                              - 50.00
                          emerald:
                            material: EMERALD
                            category: ores
                            slot: 4
                            base_price: 100.00
                            price_variation: 0.1000
                            tax_rate: 0.40
                            stock: 100
                            last_activity: 0
                            price_history:
                              - 100.00
                          string:
                            material: STRING
                            category: mob_drops
                            slot: 0
                            base_price: 0.50
                            price_variation: 0.0020
                            tax_rate: 0.15
                            stock: 1500
                            last_activity: 0
                            price_history:
                              - 0.50
                          spider_eye:
                            material: SPIDER_EYE
                            category: mob_drops
                            slot: 1
                            base_price: 1.50
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 1.50
                          bone:
                            material: BONE
                            category: mob_drops
                            slot: 2
                            base_price: 0.75
                            price_variation: 0.0030
                            tax_rate: 0.18
                            stock: 1200
                            last_activity: 0
                            price_history:
                              - 0.75
                          gunpowder:
                            material: GUNPOWDER
                            category: mob_drops
                            slot: 3
                            base_price: 2.00
                            price_variation: 0.0075
                            tax_rate: 0.22
                            stock: 800
                            last_activity: 0
                            price_history:
                              - 2.00
                          ender_pearl:
                            material: ENDER_PEARL
                            category: mob_drops
                            slot: 4
                            base_price: 10.00
                            price_variation: 0.0200
                            tax_rate: 0.30
                            stock: 300
                            last_activity: 0
                            price_history:
                              - 10.00
                          oak_log:
                            material: OAK_LOG
                            category: woods
                            slot: 0
                            base_price: 0.50
                            price_variation: 0.0020
                            tax_rate: 0.15
                            stock: 2000
                            last_activity: 0
                            price_history:
                              - 0.50
                          spruce_log:
                            material: SPRUCE_LOG
                            category: woods
                            slot: 1
                            base_price: 0.55
                            price_variation: 0.0022
                            tax_rate: 0.15
                            stock: 1800
                            last_activity: 0
                            price_history:
                              - 0.55
                          birch_log:
                            material: BIRCH_LOG
                            category: woods
                            slot: 2
                            base_price: 0.60
                            price_variation: 0.0025
                            tax_rate: 0.15
                            stock: 1600
                            last_activity: 0
                            price_history:
                              - 0.60
                          jungle_log:
                            material: JUNGLE_LOG
                            category: woods
                            slot: 3
                            base_price: 0.65
                            price_variation: 0.0028
                            tax_rate: 0.15
                            stock: 1400
                            last_activity: 0
                            price_history:
                              - 0.65
                          acacia_log:
                            material: ACACIA_LOG
                            category: woods
                            slot: 4
                            base_price: 0.70
                            price_variation: 0.0030
                            tax_rate: 0.15
                            stock: 1200
                            last_activity: 0
                            price_history:
                              - 0.70
                          red_dye:
                            material: RED_DYE
                            category: Dyes
                            slot: 0
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 1.00
                          blue_dye:
                            material: BLUE_DYE
                            category: Dyes
                            slot: 1
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 1.00
                          green_dye:
                            material: GREEN_DYE
                            category: Dyes
                            slot: 2
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 1.00
                          yellow_dye:
                            material: YELLOW_DYE
                            category: Dyes
                            slot: 3
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 1.00
                          purple_dye:
                            material: PURPLE_DYE
                            category: Dyes
                            slot: 4
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 1.00
                          wheat:
                            material: WHEAT
                            category: Livestock
                            slot: 0
                            base_price: 0.30
                            price_variation: 0.0010
                            tax_rate: 0.10
                            stock: 3000
                            last_activity: 0
                            price_history:
                              - 0.30
                          carrot:
                            material: CARROT
                            category: Livestock
                            slot: 1
                            base_price: 0.50
                            price_variation: 0.0020
                            tax_rate: 0.15
                            stock: 2000
                            last_activity: 0
                            price_history:
                              - 0.50
                          potato:
                            material: POTATO
                            category: Livestock
                            slot: 2
                            base_price: 0.40
                            price_variation: 0.0015
                            tax_rate: 0.12
                            stock: 2500
                            last_activity: 0
                            price_history:
                              - 0.40
                          beef:
                            material: BEEF
                            category: Livestock
                            slot: 3
                            base_price: 2.00
                            price_variation: 0.0075
                            tax_rate: 0.20
                            stock: 1000
                            last_activity: 0
                            price_history:
                              - 2.00
                          chicken:
                            material: CHICKEN
                            category: Livestock
                            slot: 4
                            base_price: 1.50
                            price_variation: 0.0050
                            tax_rate: 0.18
                            stock: 1200
                            last_activity: 0
                            price_history:
                              - 1.50
                          white_wool:
                            material: WHITE_WOOL
                            category: wools
                            slot: 0
                            base_price: 0.75
                            price_variation: 0.0030
                            tax_rate: 0.15
                            stock: 1500
                            last_activity: 0
                            price_history:
                              - 0.75
                          black_wool:
                            material: BLACK_WOOL
                            category: wools
                            slot: 1
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1200
                            last_activity: 0
                            price_history:
                              - 1.00
                          red_wool:
                            material: RED_WOOL
                            category: wools
                            slot: 2
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1200
                            last_activity: 0
                            price_history:
                              - 1.00
                          blue_wool:
                            material: BLUE_WOOL
                            category: wools
                            slot: 3
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1200
                            last_activity: 0
                            price_history:
                              - 1.00
                          green_wool:
                            material: GREEN_WOOL
                            category: wools
                            slot: 4
                            base_price: 1.00
                            price_variation: 0.0050
                            tax_rate: 0.20
                            stock: 1200
                            last_activity: 0
                            price_history:
                              - 1.00
                        """;

        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(defaultConfig);
        config.save(file);
    }

    public YamlConfiguration getCategoriesConfig() {
        return categoriesConfig;
    }

    public YamlConfiguration getItemsConfig() {
        return itemsConfig;
    }
}