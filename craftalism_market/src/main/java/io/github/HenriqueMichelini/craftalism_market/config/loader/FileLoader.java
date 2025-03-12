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
                        items:
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
                "items:\n" +
                        "  cobblestone:\n" +
                        "    material: COBBLESTONE\n" +
                        "    category: natural_resources\n" +
                        "    slot: 0\n" +
                        "    base_price: 0.37\n" +
                        "    price_variation: 0.0015\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 2000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.37\n" +

                        "  dirt:\n" +
                        "    material: DIRT\n" +
                        "    category: natural_resources\n" +
                        "    slot: 1\n" +
                        "    base_price: 0.1\n" +
                        "    price_variation: 0.0005\n" +
                        "    tax_rate: 0.1\n" +
                        "    stock: 5000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.1\n" +

                        "  sand:\n" +
                        "    material: SAND\n" +
                        "    category: natural_resources\n" +
                        "    slot: 2\n" +
                        "    base_price: 0.25\n" +
                        "    price_variation: 0.001\n" +
                        "    tax_rate: 0.12\n" +
                        "    stock: 3000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.25\n" +

                        "  gravel:\n" +
                        "    material: GRAVEL\n" +
                        "    category: natural_resources\n" +
                        "    slot: 3\n" +
                        "    base_price: 0.2\n" +
                        "    price_variation: 0.0008\n" +
                        "    tax_rate: 0.12\n" +
                        "    stock: 2500\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.2\n" +

                        "  clay_ball:\n" +
                        "    material: CLAY_BALL\n" +
                        "    category: natural_resources\n" +
                        "    slot: 4\n" +
                        "    base_price: 0.5\n" +
                        "    price_variation: 0.002\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.5\n" +

                        "  coal:\n" +
                        "    material: COAL\n" +
                        "    category: Ores\n" +
                        "    slot: 0\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1500\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  iron_ingot:\n" +
                        "    material: IRON_INGOT\n" +
                        "    category: Ores\n" +
                        "    slot: 1\n" +
                        "    base_price: 5.00\n" +
                        "    price_variation: 0.0100\n" +
                        "    tax_rate: 0.25\n" +
                        "    stock: 800\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 5.00\n" +

                        "  gold_ingot:\n" +
                        "    material: GOLD_INGOT\n" +
                        "    category: Ores\n" +
                        "    slot: 2\n" +
                        "    base_price: 10.00\n" +
                        "    price_variation: 0.0200\n" +
                        "    tax_rate: 0.30\n" +
                        "    stock: 500\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 10.00\n" +

                        "  diamond:\n" +
                        "    material: DIAMOND\n" +
                        "    category: Ores\n" +
                        "    slot: 3\n" +
                        "    base_price: 50.00\n" +
                        "    price_variation: 0.0500\n" +
                        "    tax_rate: 0.35\n" +
                        "    stock: 200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 50.00\n" +

                        "  emerald:\n" +
                        "    material: EMERALD\n" +
                        "    category: Ores\n" +
                        "    slot: 4\n" +
                        "    base_price: 100.00\n" +
                        "    price_variation: 0.1000\n" +
                        "    tax_rate: 0.40\n" +
                        "    stock: 100\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 100.00\n" +

                        "  string:\n" +
                        "    material: STRING\n" +
                        "    category: Mob Drops\n" +
                        "    slot: 0\n" +
                        "    base_price: 0.50\n" +
                        "    price_variation: 0.0020\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 1500\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.50\n" +

                        "  spider_eye:\n" +
                        "    material: SPIDER_EYE\n" +
                        "    category: Mob Drops\n" +
                        "    slot: 1\n" +
                        "    base_price: 1.50\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.50\n" +

                        "  bone:\n" +
                        "    material: BONE\n" +
                        "    category: Mob Drops\n" +
                        "    slot: 2\n" +
                        "    base_price: 0.75\n" +
                        "    price_variation: 0.0030\n" +
                        "    tax_rate: 0.18\n" +
                        "    stock: 1200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.75\n" +

                        "  gunpowder:\n" +
                        "    material: GUNPOWDER\n" +
                        "    category: Mob Drops\n" +
                        "    slot: 3\n" +
                        "    base_price: 2.00\n" +
                        "    price_variation: 0.0075\n" +
                        "    tax_rate: 0.22\n" +
                        "    stock: 800\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 2.00\n" +

                        "  ender_pearl:\n" +
                        "    material: ENDER_PEARL\n" +
                        "    category: Mob Drops\n" +
                        "    slot: 4\n" +
                        "    base_price: 10.00\n" +
                        "    price_variation: 0.0200\n" +
                        "    tax_rate: 0.30\n" +
                        "    stock: 300\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 10.00\n" +

                        "  oak_log:\n" +
                        "    material: OAK_LOG\n" +
                        "    category: Woods\n" +
                        "    slot: 0\n" +
                        "    base_price: 0.50\n" +
                        "    price_variation: 0.0020\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 2000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.50\n" +

                        "  spruce_log:\n" +
                        "    material: SPRUCE_LOG\n" +
                        "    category: Woods\n" +
                        "    slot: 1\n" +
                        "    base_price: 0.55\n" +
                        "    price_variation: 0.0022\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 1800\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.55\n" +

                        "  birch_log:\n" +
                        "    material: BIRCH_LOG\n" +
                        "    category: Woods\n" +
                        "    slot: 2\n" +
                        "    base_price: 0.60\n" +
                        "    price_variation: 0.0025\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 1600\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.60\n" +

                        "  jungle_log:\n" +
                        "    material: JUNGLE_LOG\n" +
                        "    category: Woods\n" +
                        "    slot: 3\n" +
                        "    base_price: 0.65\n" +
                        "    price_variation: 0.0028\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 1400\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.65\n" +

                        "  acacia_log:\n" +
                        "    material: ACACIA_LOG\n" +
                        "    category: Woods\n" +
                        "    slot: 4\n" +
                        "    base_price: 0.70\n" +
                        "    price_variation: 0.0030\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 1200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.70\n" +

                        "  red_dye:\n" +
                        "    material: RED_DYE\n" +
                        "    category: Dyes\n" +
                        "    slot: 0\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  blue_dye:\n" +
                        "    material: BLUE_DYE\n" +
                        "    category: Dyes\n" +
                        "    slot: 1\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  green_dye:\n" +
                        "    material: GREEN_DYE\n" +
                        "    category: Dyes\n" +
                        "    slot: 2\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  yellow_dye:\n" +
                        "    material: YELLOW_DYE\n" +
                        "    category: Dyes\n" +
                        "    slot: 3\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  purple_dye:\n" +
                        "    material: PURPLE_DYE\n" +
                        "    category: Dyes\n" +
                        "    slot: 4\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  wheat:\n" +
                        "    material: WHEAT\n" +
                        "    category: Livestock\n" +
                        "    slot: 0\n" +
                        "    base_price: 0.30\n" +
                        "    price_variation: 0.0010\n" +
                        "    tax_rate: 0.10\n" +
                        "    stock: 3000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.30\n" +

                        "  carrot:\n" +
                        "    material: CARROT\n" +
                        "    category: Livestock\n" +
                        "    slot: 1\n" +
                        "    base_price: 0.50\n" +
                        "    price_variation: 0.0020\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 2000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.50\n" +

                        "  potato:\n" +
                        "    material: POTATO\n" +
                        "    category: Livestock\n" +
                        "    slot: 2\n" +
                        "    base_price: 0.40\n" +
                        "    price_variation: 0.0015\n" +
                        "    tax_rate: 0.12\n" +
                        "    stock: 2500\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.40\n" +

                        "  beef:\n" +
                        "    material: BEEF\n" +
                        "    category: Livestock\n" +
                        "    slot: 3\n" +
                        "    base_price: 2.00\n" +
                        "    price_variation: 0.0075\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1000\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 2.00\n" +

                        "  chicken:\n" +
                        "    material: CHICKEN\n" +
                        "    category: Livestock\n" +
                        "    slot: 4\n" +
                        "    base_price: 1.50\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.18\n" +
                        "    stock: 1200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.50\n" +

                        "  white_wool:\n" +
                        "    material: WHITE_WOOL\n" +
                        "    category: Wools\n" +
                        "    slot: 0\n" +
                        "    base_price: 0.75\n" +
                        "    price_variation: 0.0030\n" +
                        "    tax_rate: 0.15\n" +
                        "    stock: 1500\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 0.75\n" +

                        "  black_wool:\n" +
                        "    material: BLACK_WOOL\n" +
                        "    category: Wools\n" +
                        "    slot: 1\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  red_wool:\n" +
                        "    material: RED_WOOL\n" +
                        "    category: Wools\n" +
                        "    slot: 2\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  blue_wool:\n" +
                        "    material: BLUE_WOOL\n" +
                        "    category: Wools\n" +
                        "    slot: 3\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n" +

                        "  green_wool:\n" +
                        "    material: GREEN_WOOL\n" +
                        "    category: Wools\n" +
                        "    slot: 4\n" +
                        "    base_price: 1.00\n" +
                        "    price_variation: 0.0050\n" +
                        "    tax_rate: 0.20\n" +
                        "    stock: 1200\n" +
                        "    last_activity: 0\n" +
                        "    price_history:\n" +
                        "      - 1.00\n";

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