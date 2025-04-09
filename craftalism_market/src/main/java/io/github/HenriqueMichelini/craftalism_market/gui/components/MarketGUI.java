package io.github.HenriqueMichelini.craftalism_market.gui.components;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.models.Category;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

public class MarketGUI extends BaseGUI {
    private final ConfigManager configManager;
    private final BiConsumer<Player, String> onCategorySelect;

    public MarketGUI(
            CraftalismMarket plugin,
            ConfigManager configManager,
            BiConsumer<Player, String> onCategorySelect
    ) {
        super("Market", 6, plugin);
        this.configManager = configManager;
        this.onCategorySelect = onCategorySelect;
        populateCategories();
    }

    private void populateCategories() {
        configManager.getCategories().values().forEach(this::createCategoryButton);
    }

    private void createCategoryButton(Category category) {
        String cacheKey = "category_" + category.category();
        GuiItem button = ButtonFactory.createCachedButton(
                cacheKey,
                category.material(),
                Component.text(category.title(), NamedTextColor.GREEN),
                List.of(), // Static lore if needed
                event -> onCategorySelect.accept(event.getPlayer(), category.category())
        );
        gui.setItem(category.slot(), button);
    }
}
