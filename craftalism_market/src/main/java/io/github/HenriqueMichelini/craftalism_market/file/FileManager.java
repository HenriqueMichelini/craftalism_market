package io.github.HenriqueMichelini.craftalism_market.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {
    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());
    private static final String MARKET_CATEGORY_FILE_NAME = "categories.yml";
    private static final String ITEMS_DATA_FILE_NAME = "items.yml";

    private final File marketCategoryFile;
    private FileConfiguration marketCategoryConfig;

    private final File itemsDataFile;
    private FileConfiguration itemsDataConfig;

    public FileManager(File dataFolder) {
        this.marketCategoryFile = new File(dataFolder, MARKET_CATEGORY_FILE_NAME);
        this.itemsDataFile = new File(dataFolder, ITEMS_DATA_FILE_NAME);

        loadMarketCategoryFile();
        loadItemsDataFile();
    }

    /**
     * Loads the market category file. If the file doesn't exist or is empty, creates it with default values.
     */
    private void loadMarketCategoryFile() {
        if (!marketCategoryFile.exists() || marketCategoryFile.length() == 0) {
            createDefaultMarketCategoryFile();
        } else {
            marketCategoryConfig = YamlConfiguration.loadConfiguration(marketCategoryFile);
            LOGGER.info("Loaded market category file: " + MARKET_CATEGORY_FILE_NAME);
        }
    }

    /**
     * Creates the market category file with default values.
     */
    private void createDefaultMarketCategoryFile() {
        try {
            if (marketCategoryFile.createNewFile()) {
                marketCategoryConfig = YamlConfiguration.loadConfiguration(marketCategoryFile);
                setDefaultMarketCategoryValues();
                saveMarketCategoryFile();
                LOGGER.info("Created default market category file: " + MARKET_CATEGORY_FILE_NAME);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create market category file: " + MARKET_CATEGORY_FILE_NAME, e);
        }
    }

    /**
     * Sets default values for the market category configuration.
     */
    private void setDefaultMarketCategoryValues() {
        addItemToMarketCategoryConfig("stone", "STONE", "Natural Resources", 19);
        addItemToMarketCategoryConfig("cyan_dye", "CYAN_DYE", "Dyes", 29);
        addItemToMarketCategoryConfig("oak_log", "OAK_LOG", "Woods", 21);
        addItemToMarketCategoryConfig("wheat", "WHEAT", "Livestock", 31);
        addItemToMarketCategoryConfig("purple_wool", "PURPLE_WOOL", "Wools", 23);
        addItemToMarketCategoryConfig("string", "STRING", "Mob Drops", 33);
        addItemToMarketCategoryConfig("emerald", "EMERALD", "Ores", 25);
    }

    /**
     * Adds an item to the market category configuration.
     *
     * @param itemKey  The key for the item (e.g., "stone").
     * @param material The material name (e.g., "STONE").
     * @param category The category name (e.g., "Natural Resources").
     * @param slot     The slot number in the GUI.
     */
    private void addItemToMarketCategoryConfig(String itemKey, String material, String category, int slot) {
        String basePath = "items." + itemKey + ".";
        marketCategoryConfig.set(basePath + "material", material);
        marketCategoryConfig.set(basePath + "category", category);
        marketCategoryConfig.set(basePath + "title", category);
        marketCategoryConfig.set(basePath + "slot", slot);
    }

    /**
     * Saves the market category file.
     */
    private void saveMarketCategoryFile() {
        try {
            marketCategoryConfig.save(marketCategoryFile);
            LOGGER.info("Saved market category file: " + MARKET_CATEGORY_FILE_NAME);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save market category file: " + MARKET_CATEGORY_FILE_NAME, e);
        }
    }

    /**
     * Loads the items data file. If the file doesn't exist or is empty, creates it with default values.
     */
    private void loadItemsDataFile() {
        if (!itemsDataFile.exists() || itemsDataFile.length() == 0) {
            createDefaultItemsDataFile();
        } else {
            itemsDataConfig = YamlConfiguration.loadConfiguration(itemsDataFile);
            LOGGER.info("Loaded items data file: " + ITEMS_DATA_FILE_NAME);
        }
    }

    /**
     * Creates the items data file with default values.
     */
    private void createDefaultItemsDataFile() {
        try {
            if (itemsDataFile.createNewFile()) {
                itemsDataConfig = YamlConfiguration.loadConfiguration(itemsDataFile);
                setDefaultItemsDataValues();
                saveItemsDataFile();
                LOGGER.info("Created default items data file: " + ITEMS_DATA_FILE_NAME);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create items data file: " + ITEMS_DATA_FILE_NAME, e);
        }
    }

    /**
     * Sets default values for the items data configuration.
     */
    private void setDefaultItemsDataValues() {
        // Natural Resources
        addItemToItemsDataConfig("cobblestone", "COBBLESTONE", "Natural Resources", 0, 0.37, 0.0015, 0.15, 2000, 0, new double[]{0.37});
        addItemToItemsDataConfig("dirt", "DIRT", "Natural Resources", 1, 0.10, 0.0005, 0.10, 5000, 0, new double[]{0.10});
        addItemToItemsDataConfig("sand", "SAND", "Natural Resources", 2, 0.25, 0.0010, 0.12, 3000, 0, new double[]{0.25});
        addItemToItemsDataConfig("gravel", "GRAVEL", "Natural Resources", 3, 0.20, 0.0008, 0.12, 2500, 0, new double[]{0.20});
        addItemToItemsDataConfig("clay_ball", "CLAY_BALL", "Natural Resources", 4, 0.50, 0.0020, 0.15, 1000, 0, new double[]{0.50});

        // Ores
        addItemToItemsDataConfig("coal", "COAL", "Ores", 0, 1.00, 0.0050, 0.20, 1500, 0, new double[]{1.00});
        addItemToItemsDataConfig("iron_ingot", "IRON_INGOT", "Ores", 1, 5.00, 0.0100, 0.25, 800, 0, new double[]{5.00});
        addItemToItemsDataConfig("gold_ingot", "GOLD_INGOT", "Ores", 2, 10.00, 0.0200, 0.30, 500, 0, new double[]{10.00});
        addItemToItemsDataConfig("diamond", "DIAMOND", "Ores", 3, 50.00, 0.0500, 0.35, 200, 0, new double[]{50.00});
        addItemToItemsDataConfig("emerald", "EMERALD", "Ores", 4, 100.00, 0.1000, 0.40, 100, 0, new double[]{100.00});

        // Mob Drops
        addItemToItemsDataConfig("string", "STRING", "Mob Drops", 0, 0.50, 0.0020, 0.15, 1500, 0, new double[]{0.50});
        addItemToItemsDataConfig("spider_eye", "SPIDER_EYE", "Mob Drops", 1, 1.50, 0.0050, 0.20, 1000, 0, new double[]{1.50});
        addItemToItemsDataConfig("bone", "BONE", "Mob Drops", 2, 0.75, 0.0030, 0.18, 1200, 0, new double[]{0.75});
        addItemToItemsDataConfig("gunpowder", "GUNPOWDER", "Mob Drops", 3, 2.00, 0.0075, 0.22, 800, 0, new double[]{2.00});
        addItemToItemsDataConfig("ender_pearl", "ENDER_PEARL", "Mob Drops", 4, 10.00, 0.0200, 0.30, 300, 0, new double[]{10.00});

        // Woods
        addItemToItemsDataConfig("oak_log", "OAK_LOG", "Woods", 0, 0.50, 0.0020, 0.15, 2000, 0, new double[]{0.50});
        addItemToItemsDataConfig("spruce_log", "SPRUCE_LOG", "Woods", 1, 0.55, 0.0022, 0.15, 1800, 0, new double[]{0.55});
        addItemToItemsDataConfig("birch_log", "BIRCH_LOG", "Woods", 2, 0.60, 0.0025, 0.15, 1600, 0, new double[]{0.60});
        addItemToItemsDataConfig("jungle_log", "JUNGLE_LOG", "Woods", 3, 0.65, 0.0028, 0.15, 1400, 0, new double[]{0.65});
        addItemToItemsDataConfig("acacia_log", "ACACIA_LOG", "Woods", 4, 0.70, 0.0030, 0.15, 1200, 0, new double[]{0.70});

        // Dyes
        addItemToItemsDataConfig("red_dye", "RED_DYE", "Dyes", 0, 1.00, 0.0050, 0.20, 1000, 0, new double[]{1.00});
        addItemToItemsDataConfig("blue_dye", "BLUE_DYE", "Dyes", 1, 1.00, 0.0050, 0.20, 1000, 0, new double[]{1.00});
        addItemToItemsDataConfig("green_dye", "GREEN_DYE", "Dyes", 2, 1.00, 0.0050, 0.20, 1000, 0, new double[]{1.00});
        addItemToItemsDataConfig("yellow_dye", "YELLOW_DYE", "Dyes", 3, 1.00, 0.0050, 0.20, 1000, 0, new double[]{1.00});
        addItemToItemsDataConfig("purple_dye", "PURPLE_DYE", "Dyes", 4, 1.00, 0.0050, 0.20, 1000, 0, new double[]{1.00});

        // Livestock
        addItemToItemsDataConfig("wheat", "WHEAT", "Livestock", 0, 0.30, 0.0010, 0.10, 3000, 0, new double[]{0.30});
        addItemToItemsDataConfig("carrot", "CARROT", "Livestock", 1, 0.50, 0.0020, 0.15, 2000, 0, new double[]{0.50});
        addItemToItemsDataConfig("potato", "POTATO", "Livestock", 2, 0.40, 0.0015, 0.12, 2500, 0, new double[]{0.40});
        addItemToItemsDataConfig("beef", "BEEF", "Livestock", 3, 2.00, 0.0075, 0.20, 1000, 0, new double[]{2.00});
        addItemToItemsDataConfig("chicken", "CHICKEN", "Livestock", 4, 1.50, 0.0050, 0.18, 1200, 0, new double[]{1.50});

        // Wools
        addItemToItemsDataConfig("white_wool", "WHITE_WOOL", "Wools", 0, 0.75, 0.0030, 0.15, 1500, 0, new double[]{0.75});
        addItemToItemsDataConfig("black_wool", "BLACK_WOOL", "Wools", 1, 1.00, 0.0050, 0.20, 1200, 0, new double[]{1.00});
        addItemToItemsDataConfig("red_wool", "RED_WOOL", "Wools", 2, 1.00, 0.0050, 0.20, 1200, 0, new double[]{1.00});
        addItemToItemsDataConfig("blue_wool", "BLUE_WOOL", "Wools", 3, 1.00, 0.0050, 0.20, 1200, 0, new double[]{1.00});
        addItemToItemsDataConfig("green_wool", "GREEN_WOOL", "Wools", 4, 1.00, 0.0050, 0.20, 1200, 0, new double[]{1.00});
    }

    /**
     * Adds an item to the items data configuration.
     *
     * @param itemKey                  The key for the item (e.g., "cobblestone").
     * @param material                 The material name (e.g., "COBBLESTONE").
     * @param category                 The category name (e.g., "Natural Resources").
     * @param slot                     The slot number in the GUI.
     * @param basePrice                The base price of the item.
     * @param priceVariationPerOperation The price variation per transaction.
     * @param sellTax                  The tax rate for selling the item.
     * @param amount                   The available stock of the item.
     * @param lastActivity             The timestamp of the last activity.
     * @param priceHistory             The price history of the item.
     */
    private void addItemToItemsDataConfig(String itemKey, String material, String category, int slot, double basePrice,
                                          double priceVariationPerOperation, double sellTax, int amount, long lastActivity, double[] priceHistory) {
        String basePath = "items." + itemKey + ".";
        itemsDataConfig.set(basePath + "material", material);
        itemsDataConfig.set(basePath + "category", category);
        itemsDataConfig.set(basePath + "slot", slot);
        itemsDataConfig.set(basePath + "basePrice", basePrice);
        itemsDataConfig.set(basePath + "priceVariationPerOperation", priceVariationPerOperation);
        itemsDataConfig.set(basePath + "sellTax", sellTax);
        itemsDataConfig.set(basePath + "amount", amount);
        itemsDataConfig.set(basePath + "lastActivity", lastActivity);
        itemsDataConfig.set(basePath + "price_history", priceHistory);
    }

    /**
     * Saves the items data file.
     */
    private void saveItemsDataFile() {
        try {
            itemsDataConfig.save(itemsDataFile);
            LOGGER.info("Saved items data file: " + ITEMS_DATA_FILE_NAME);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save items data file: " + ITEMS_DATA_FILE_NAME, e);
        }
    }

    /**
     * Gets the market category configuration.
     *
     * @return The FileConfiguration object for the market category file.
     */
    public FileConfiguration getMarketCategoryConfig() {
        return marketCategoryConfig;
    }

    /**
     * Gets the items data configuration.
     *
     * @return The FileConfiguration object for the items data file.
     */
    public FileConfiguration getItemsDataConfig() {
        return itemsDataConfig;
    }
}