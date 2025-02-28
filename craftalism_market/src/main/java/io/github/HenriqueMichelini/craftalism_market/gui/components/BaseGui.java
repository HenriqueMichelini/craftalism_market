package io.github.HenriqueMichelini.craftalism_market.gui.components;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public abstract class BaseGui {
    protected final Gui gui;
    protected final CraftalismMarket plugin;

    protected static final int BACK_BUTTON_SLOT = 49;

    public BaseGui(String title, int rows, CraftalismMarket plugin) {
        this.gui = Gui.gui()
                .title(Component.text(title, NamedTextColor.GREEN))
                .rows(rows)
                .disableAllInteractions()
                .create();
        this.plugin = plugin;
    }

    public void open(Player player) {
        gui.open(player);
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
        gui.setItem(BACK_BUTTON_SLOT, createButton(
                Material.BARRIER,
                Component.text("Back", NamedTextColor.RED),
                List.of(),
                onBack
        ));
    }

    protected String formatPrice(BigDecimal price) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMANY);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return "$" + formatter.format(price.doubleValue());
    }

    protected String formatPercentage(BigDecimal sellTax) {
        return sellTax.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) + "%";
    }
}
