package io.github.HenriqueMichelini.craftalism_market.command;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.model.MarketCategoryItem;
import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MarketCommand implements CommandExecutor {
    private Gui MarketGui;
    private Gui MarketGuiItemsByCategory;
    private final CraftalismMarket plugin;

    private final Map<String, MarketCategoryItem> marketCategories = new HashMap<>();
    private final Map<String, MarketItem> marketItems = new HashMap<>();

    public MarketCommand(CraftalismMarket plugin) {
        this.plugin = plugin;
        loadMarketCategories();
        loadItemsData();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be run by a player.");
            return false;
        }

        openMarket(player);

        return true;
    }

    private void openMarket(Player player) {
        createMarketGui();
        populateMarketGui();

        getMarketGui().open(player);
    }

    private void createMarketGui() {
        MarketGui = Gui.gui()
                .title(Component.text("Market", NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions()
                .create();
    }

    private void loadMarketCategories() {
        File file = new File(plugin.getDataFolder(), "market_category_items.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("market_category_items.yml does not exist!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        var itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            plugin.getLogger().warning("Invalid items section in market_category_items.yml");
            return;
        }

        for (String itemKey : itemsSection.getKeys(false)) {
            var itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            String materialName = itemData.getString("material");
            String title = itemData.getString("title");
            int slot = itemData.getInt("slot");

            if (materialName == null || title == null) {
                plugin.getLogger().warning("Invalid data for item: " + itemKey);
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Invalid material: " + materialName);
                continue;
            }

            marketCategories.put(itemKey, new MarketCategoryItem(material, title, slot));
        }
    }

    private void populateMarketGui() {
        for (Map.Entry<String, MarketCategoryItem> entry : marketCategories.entrySet()) {
            String itemKey = entry.getKey();
            MarketCategoryItem categoryItem = entry.getValue();

            ItemStack itemStack = new ItemStack(categoryItem.getMaterial());
            GuiItem guiItem = ItemBuilder.from(itemStack)
                    .name(Component.text(categoryItem.getTitle(), NamedTextColor.GREEN))
                    .asGuiItem(event -> {
                        openMarketItemsByCategory(categoryItem.getTitle(), (Player) event.getWhoClicked());
                    });

            MarketGui.setItem(categoryItem.getSlot(), guiItem);
        }
    }

    public Gui getMarketGui() {
        return MarketGui;
    }

    private void openMarketItemsByCategory(String category, Player player) {
        createMarketGuiItemsByCategory(category);
        populateMarketGuiItemsByCategory(category);

        getMarketGuiItemsByCategory().open(player);
    }

    private void createMarketGuiItemsByCategory(String categoryItem) {
        MarketGuiItemsByCategory = Gui.gui()
                .title(Component.text(categoryItem, NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions()
                .create();
    }

    private void loadItemsData() {
        File file = new File(plugin.getDataFolder(), "items_data.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("items_data.yml does not exist!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        var itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            plugin.getLogger().warning("Invalid items section in items_data.yml");
            return;
        }

        for (String itemKey : itemsSection.getKeys(false)) {
            var itemData = itemsSection.getConfigurationSection(itemKey);
            if (itemData == null) continue;

            String category = itemData.getString("category");
            String materialName = itemData.getString("material");
            int slot = itemData.getInt("slot");
            BigDecimal price = BigDecimal.valueOf(itemData.getDouble("price"));
            int amount = itemData.getInt("amount");

            if (category == null || materialName == null) {
                plugin.getLogger().warning("Invalid data for item: " + itemKey);
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Invalid material: " + materialName);
                continue;
            }

            marketItems.put(itemKey, new MarketItem(category, material, slot, price, amount));
        }
    }

    private void populateMarketGuiItemsByCategory(String category) {
        for (Map.Entry<String, MarketItem> entry : marketItems.entrySet()) {
            String itemKey = entry.getKey();
            MarketItem item = entry.getValue();

            if (!item.getCategory().equalsIgnoreCase(category)) continue;

            ItemStack itemStack = new ItemStack(item.getMaterial());
            GuiItem guiItem = ItemBuilder.from(itemStack)
                    .name(Component.text(itemKey, NamedTextColor.GREEN))
                    .lore(
                            Component.text("Price: $" + item.getPrice()),
                            Component.text("Stock: " + item.getAmount())
                    )
                    .asGuiItem();

            MarketGuiItemsByCategory.setItem(item.getSlot(), guiItem);
        }
    }

    public Gui getMarketGuiItemsByCategory() {
        return MarketGuiItemsByCategory;
    }
}