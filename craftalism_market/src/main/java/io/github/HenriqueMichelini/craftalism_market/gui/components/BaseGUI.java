package io.github.HenriqueMichelini.craftalism_market.gui.components;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseGUI {
    protected final Gui gui;
    protected final CraftalismMarket plugin;

    protected static final int BACK_BUTTON_SLOT = 49;

    public BaseGUI(String title, int rows, CraftalismMarket plugin) {
        this.gui = Gui.gui()
                .title(Component.text(title, NamedTextColor.GREEN))
                .rows(rows)
                .disableAllInteractions()
                .create();
        this.plugin = plugin;

        // Add close handler here
        gui.setCloseGuiAction(event -> {
            Player player = (Player) event.getPlayer();
            onClose(player);
            if (this instanceof TradeGUI) {
                ((TradeGUI) this).resetAmount();
            }
        });
    }

    public void open(Player player) {
        plugin.getLogger().fine(player.getName() + " opened " + gui.title());
        gui.open(player);
    }

    protected void onClose(Player player) {
        plugin.getLogger().fine(player.getName() + " closed " + gui.title());
    }

    protected GuiItem createButton(
            Material material,
            Component name,
            List<Component> lore,
            Consumer<Player> onClick
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);

        return new GuiItem(item, e -> onClick.accept((Player) e.getWhoClicked()));
    }

    protected void addBackButton(Consumer<Player> onBack) {
        String cacheKey = "back_button";
        GuiItem backButton = ButtonFactory.createCachedButton(
                cacheKey,
                Material.BARRIER,
                Component.text("Back", NamedTextColor.RED),
                List.of(), // No lore
                onBack
        );
        gui.setItem(BACK_BUTTON_SLOT, backButton);
    }

    protected String formatPercentage(BigDecimal sellTax) {
        return sellTax.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%";
    }

    protected void playUiSound(Player player, String soundType) {
        Sound sound = switch(soundType.toLowerCase()) {
            case "click" -> Sound.UI_BUTTON_CLICK;
            case "success" -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            case "error" -> Sound.BLOCK_NOTE_BLOCK_BASS;
            default -> Sound.BLOCK_WOODEN_BUTTON_CLICK_ON;
        };

        player.playSound(player.getLocation(), sound, 0.8f, 1.0f);
    }
}
