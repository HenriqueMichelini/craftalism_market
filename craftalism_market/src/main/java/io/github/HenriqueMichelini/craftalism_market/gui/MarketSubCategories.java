package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class MarketSubCategories {
    private final Gui gui;

    public MarketSubCategories(String subCategoryName) {
        gui = Gui.gui()
                .title(Component.text(subCategoryName, NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions()
                .create();

        populateGui(subCategoryName);
        addBackButton();
    }

    private void populateGui(String subCategoryName) {
        FileConfiguration config = CraftalismMarket.getInstance().getItemsDataConfig();
        String path = "items";

        for (String key : Objects.requireNonNull(config.getConfigurationSection(path)).getKeys(false)) {
            String category = config.getString(path + "." + key + ".category");

            if (category != null && category.equalsIgnoreCase(subCategoryName)) {
                String materialString = config.getString(path + "." + key + ".material");
                int amount = config.getInt(path + "." + key + ".amount");
                double price = config.getDouble(path + "." + key + ".price");

                assert materialString != null;
                Material material = Material.matchMaterial(materialString);
                if (material != null) {
                    GuiItem item = createGuiItem(material, amount, price);
                    gui.addItem(item);
                }
            }
        }
    }

    private GuiItem createGuiItem(Material material, int amount, double price) {
        return ItemBuilder.from(material)
                .lore(List.of(
                        Component.text("Amount: " + amount, NamedTextColor.WHITE),
                        Component.text("Price: " + price + " coins", NamedTextColor.GOLD)
                ))
                .asGuiItem(event -> {
                    new ItemNegotiation(material, (Player) event.getWhoClicked()).getGui().open(event.getWhoClicked());
                });
    }

    private void addBackButton() {
        GuiItem backButton = ItemBuilder.from(Material.BARRIER)
                .name(Component.text("Back to Categories", NamedTextColor.RED))
                .asGuiItem(this::goBackToCategories);

        gui.setItem(49, backButton);
    }

    private void goBackToCategories(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            MarketCategories marketCategories = new MarketCategories();
            marketCategories.getGui().open(player);
        }
    }

    public Gui getGui() {
        return gui;
    }
}
