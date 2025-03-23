package io.github.HenriqueMichelini.craftalism_market.gui.components;

import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CategoryGUI extends BaseGUI {
    private final ConfigManager configManager;
    private final BiConsumer<Player, String> onItemSelect;

    public CategoryGUI(
            String category,
            CraftalismMarket plugin,
            ConfigManager configManager,
            BiConsumer<Player, String> onItemSelect,
            Consumer<Player> onBack
    ) {
        super(category, 6, plugin);
        this.configManager = configManager;
        this.onItemSelect = onItemSelect;
        populateItems(category);
        addBackButton(onBack);
    }

    private void populateItems(String category) {
        configManager.getItems().values().stream()
                .filter(item -> item.getCategory().equals(category))
                .forEach(this::addItemButton);
    }

    private List<Component> createItemLore(MarketItem item) {
        return List.of(
                Component.text("Price: " + formatPrice(item.getCurrentPrice()), NamedTextColor.WHITE),
                Component.text("Stock: " + item.getCurrentStock(), NamedTextColor.AQUA)
        );
    }

    private void addItemButton(MarketItem item) {
        String itemMaterialName = item.getMaterial().name().toLowerCase();
        gui.setItem(item.getSlot(), createButton(
                item.getMaterial(),
                Component.text(item.getName(), NamedTextColor.GREEN),
                createItemLore(item),
                event -> {
                        Player player = event.getPlayer();
                        onItemSelect.accept(player, itemMaterialName);
                }
        ));
    }

    public void refreshItem(String itemName) {
        MarketItem updatedItem = configManager.getItems().get(itemName);
        if (updatedItem != null) {
            gui.updateItem(updatedItem.getSlot(), createButton(
                    updatedItem.getMaterial(),
                    Component.text(updatedItem.getName(), NamedTextColor.GREEN),
                    createItemLore(updatedItem),
                    event -> onItemSelect.accept(event.getPlayer(), itemName)
            ));
        }
    }
}
