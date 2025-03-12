package io.github.HenriqueMichelini.craftalism_market.config.validation;

import org.bukkit.configuration.ConfigurationSection;
import java.util.logging.Logger;

public class SchemaValidator {
    private static final Logger LOGGER = Logger.getLogger(SchemaValidator.class.getName());

    public static void validateCategories(ConfigurationSection config) {
        if (!config.contains("categories")) {
            LOGGER.warning("Missing categories section in config");
        }
    }

    public static void validateItems(ConfigurationSection config) {
        if (!config.contains("items")) {
            LOGGER.warning("Missing items section in config");
        }
    }
}