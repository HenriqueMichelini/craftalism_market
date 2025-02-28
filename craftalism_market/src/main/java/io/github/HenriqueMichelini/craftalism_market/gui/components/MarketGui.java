package io.github.HenriqueMichelini.craftalism_market.gui.components;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.logic.DataLoader;
import io.github.HenriqueMichelini.craftalism_market.model.MarketCategoryItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;

public class MarketGui extends BaseGui {
    private final DataLoader dataLoader;
    private final BiConsumer<Player, String> onCategorySelect;

    public MarketGui(
            CraftalismMarket plugin,
            DataLoader dataLoader,
            BiConsumer<Player, String> onCategorySelect
    ) {
        super("Market", 6, plugin);
        this.dataLoader = dataLoader;
        this.onCategorySelect = onCategorySelect;
        populateCategories();
    }

    private void populateCategories() {
        dataLoader.getMarketCategories().values().forEach(category -> {
            gui.setItem(category.getSlot(), createCategoryButton(category));
        });
    }

    private GuiItem createCategoryButton(MarketCategoryItem category) {
        return createButton(
                category.getMaterial(),
                Component.text(category.getTitle(), NamedTextColor.GREEN),
                List.of(),
                event -> {
                        Player player = event.getPlayer();
                        onCategorySelect.accept(player, category.getTitle());
                }
        );
    }
}
