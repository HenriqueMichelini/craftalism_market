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

public class MarketItems {
    private final Gui gui;
    private final String subCategoryTitle;

    public MarketItems(String subCategoryTitle) {
        this.subCategoryTitle = subCategoryTitle;

        gui = Gui.gui()
                .title(Component.text(subCategoryTitle, NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions()
                .create();

        populateGui();
        addBackButton();
    }

    private void populateGui() {
        FileConfiguration config = CraftalismMarket.getInstance().getItemsDataConfig();
        String path = "items";

        for (String key : Objects.requireNonNull(config.getConfigurationSection(path)).getKeys(false)) {
            String category = config.getString(path + "." + key + ".category");

            if (category != null && category.equalsIgnoreCase(subCategoryTitle)) {
                String materialString = config.getString(path + "." + key + ".material");
                int amount = config.getInt(path + "." + key + ".amount");
                double price = config.getDouble(path + "." + key + ".price");
                int slot = config.getInt(path + "." + key + ".slot", -1); // Default to -1 if missing

                if (slot < 0 || slot >= gui.getRows() * 9) {
                    CraftalismMarket.getInstance().getLogger().warning("Invalid slot for item: " + key + " slot: " + slot);
                    continue;
                }

                assert materialString != null;
                Material material = Material.matchMaterial(materialString);
                if (material != null) {
                    GuiItem item = createGuiItem(material, amount, price);
                    gui.setItem(slot, item); // Set item in the correct slot
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
                    new ItemNegotiation(material, (Player) event.getWhoClicked(), subCategoryTitle).getGui().open(event.getWhoClicked());
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
