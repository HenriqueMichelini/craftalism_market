package io.github.HenriqueMichelini.craftalism_market.gui.components;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ButtonFactory {
    private static final Map<String, GuiItem> CACHE = new HashMap<>();

    public static GuiItem createCachedButton(String key, Material material, Component name,
                                             List<Component> lore, Consumer<Player> action) {
        return CACHE.computeIfAbsent(key, k ->
                new GuiItem(
                        ItemBuilder.from(material)
                                .name(name)
                                .lore(lore)
                                .build(),
                        event -> action.accept((Player) event.getWhoClicked())
                )
        );
    }
}
