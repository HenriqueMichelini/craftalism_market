package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.logic.DataLoader;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketManager;
import io.github.HenriqueMichelini.craftalism_market.logic.Transaction;
import io.github.HenriqueMichelini.craftalism_market.model.MarketCategoryItem;
import io.github.HenriqueMichelini.craftalism_market.model.MarketItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUIManager {
    // Constants
    private static final int CENTER_SLOT = 13;
    private static final int BACK_BUTTON_SLOT = 49;
    private static final int BUY_BUTTON_SLOT = 38;
    private static final int SELL_BUTTON_SLOT = 42;
    private static final int[] ADD_SLOTS = {0, 9, 18, 27, 36, 45};
    private static final int[] DEDUCT_SLOTS = {8, 17, 26, 35, 44, 53};
    private static final int[] AMOUNTS = {1, 8, 32, 64, 576, 2304};

    // GUI instances
    private Gui marketGui;
    private Gui marketGuiItemsByCategory;
    private Gui marketGuiItemNegotiation;

    // State
    private String currentItemName = "";
    private int amountOfItemsSelected = 1;

    // Dependencies
    private final DataLoader dataLoader;
    private final CraftalismMarket marketPlugin;
    private final MarketManager marketManager;

    public GUIManager(DataLoader dataLoader, CraftalismMarket marketPlugin, MarketManager marketManager) {
        this.dataLoader = dataLoader;
        this.marketPlugin = marketPlugin;
        this.marketManager = marketManager;
    }

    // Public GUI access methods
    public void openMarket(Player player) {
        initializeMarketGui();
        marketGui.open(player);
    }

    public void openMarketItemsByCategory(String category, Player player) {
        initializeCategoryGui(category);
        marketGuiItemsByCategory.open(player);
    }

    public void openItemNegotiation(Player player, String itemName) {
        this.currentItemName = itemName;
        initializeNegotiationGui(itemName);
        marketGuiItemNegotiation.open(player);
    }

    // GUI initialization methods
    private void initializeMarketGui() {
        marketGui = createBaseGui("Market", 6);
        populateMarketCategories();
    }

    private void initializeCategoryGui(String category) {
        marketGuiItemsByCategory = createBaseGui(category, 6);
        populateCategoryItems(category);
        addBackButton(marketGuiItemsByCategory, this::openMarket);
    }

    private void initializeNegotiationGui(String itemName) {
        marketGuiItemNegotiation = createBaseGui("Market", 6);
        marketGuiItemNegotiation.setCloseGuiAction(event -> {
            setAmountOfItemsSelected(1); // Reset to 1 when GUI closes
        });
        configureNegotiationGui(itemName);
        addBackButton(marketGuiItemNegotiation, p -> openMarketItemsByCategory(getItemCategory(itemName), p));
    }

    // GUI population methods
    private void populateMarketCategories() {
        dataLoader.getMarketCategories().values().forEach(this::addCategoryItem);
    }

    private void populateCategoryItems(String category) {
        dataLoader.getMarketItems().entrySet().stream()
                .filter(entry -> entry.getValue().getCategory().equalsIgnoreCase(category))
                .forEach(entry -> addItemToCategory(entry.getKey(), entry.getValue()));
    }

    private void configureNegotiationGui(String itemName) {
        MarketItem item = getValidMarketItem(itemName);
        if (item == null) return;

        addItemDisplay(item);
        addTransactionButtons(itemName);
        addAmountControls();
    }

    // Helper methods
    private Gui createBaseGui(String title, int rows) {
        return Gui.gui()
                .title(Component.text(title, NamedTextColor.GREEN))
                .rows(rows)
                .disableAllInteractions()
                .create();
    }

    private void addBackButton(Gui gui, java.util.function.Consumer<Player> backAction) {
        gui.setItem(BACK_BUTTON_SLOT, createNavigationButton(
                Material.BARRIER,
                "Back",
                NamedTextColor.RED,
                backAction
        ));
    }

    private GuiItem createNavigationButton(Material material, String text, NamedTextColor color, java.util.function.Consumer<Player> action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(text, color));
        item.setItemMeta(meta);

        return new GuiItem(item, event ->
                action.accept((Player) event.getWhoClicked())
        );
    }

    private void addCategoryItem(MarketCategoryItem categoryItem) {
        GuiItem guiItem = ItemBuilder.from(categoryItem.getMaterial())
                .name(Component.text(categoryItem.getTitle(), NamedTextColor.GREEN))
                .asGuiItem(event ->
                        openMarketItemsByCategory(categoryItem.getTitle(), (Player) event.getWhoClicked())
                );
        marketGui.setItem(categoryItem.getSlot(), guiItem);
    }

    private void addItemToCategory(String itemName, MarketItem item) {
        GuiItem guiItem = ItemBuilder.from(item.getMaterial())
                .name(Component.text(itemName, NamedTextColor.GREEN))
                .lore(createItemLore(item))
                .asGuiItem(event ->
                        openItemNegotiation((Player) event.getWhoClicked(), itemName)
                );
        marketGuiItemsByCategory.setItem(item.getSlot(), guiItem);
    }

    private List<Component> createItemLore(MarketItem item) {
        return List.of(
                Component.text("Price: " + formatPrice(item.getBasePrice()), NamedTextColor.WHITE),
                Component.text("Stock: " + item.getAmount(), NamedTextColor.AQUA)
        );
    }

    private void addItemDisplay(MarketItem item) {
        ItemStack displayItem = new ItemStack(item.getMaterial());
        ItemMeta meta = displayItem.getItemMeta();
        meta.lore(createDetailedLore(item));
        displayItem.setItemMeta(meta);

        marketGuiItemNegotiation.setItem(CENTER_SLOT, ItemBuilder.from(displayItem).asGuiItem());
    }

    private List<Component> createDetailedLore(MarketItem item) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Buy Price: " + formatPrice(item.getBasePrice()), NamedTextColor.GREEN));
        lore.add(Component.text("Sell Price: " + formatPrice(item.getBasePrice()), NamedTextColor.RED));
        lore.add(Component.text("Stock: " + item.getAmount(), NamedTextColor.AQUA));
        lore.add(Component.empty());
        addPriceHistory(lore, item);
        return lore;
    }

    private void addPriceHistory(List<Component> lore, MarketItem item) {
        List<BigDecimal> history = item.getPriceHistory();
        if (history.isEmpty()) {
            lore.add(Component.text("Price History: No data", NamedTextColor.GRAY));
            return;
        }

        lore.add(Component.text("Price History:", NamedTextColor.GOLD));
        history.forEach(price ->
                lore.add(Component.text("  - " + formatPrice(price), NamedTextColor.GRAY))
        );
    }

    private void addTransactionButtons(String itemName) {
        marketGuiItemNegotiation.setItem(BUY_BUTTON_SLOT, createTransactionButton(
                Material.SLIME_BLOCK,
                "Buy",
                NamedTextColor.GREEN,
                itemName
        ));

        marketGuiItemNegotiation.setItem(SELL_BUTTON_SLOT, createTransactionButton(
                Material.HONEY_BLOCK,
                "Sell",
                NamedTextColor.GOLD,
                itemName
        ));
    }

    private GuiItem createTransactionButton(Material material, String action, NamedTextColor color, String itemName) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text(action, color));
        meta.lore(createTransactionLore(itemName, action));
        button.setItemMeta(meta);

        return new GuiItem(button, event -> handleTransaction(action, (Player) event.getWhoClicked(), itemName));
    }

    private List<Component> createTransactionLore(String itemName, String action) {
        MarketItem item = getValidMarketItem(itemName);
        BigDecimal price = action.equalsIgnoreCase("buy") ? item.getBasePrice() : item.getBasePrice();
        BigDecimal total = marketManager.getTotalPriceOfItem(item, amountOfItemsSelected, true);

        return List.of(
                Component.text("Price/item: " + formatPrice(price), NamedTextColor.WHITE),
                Component.text("Quantity: " + amountOfItemsSelected, NamedTextColor.WHITE),
                Component.text("Total: " + formatPrice(total), NamedTextColor.YELLOW)
        );
    }

    private void handleTransaction(String action, Player player, String itemName) {
        EconomyManager economy = marketPlugin.getEconomyManager();
        if (economy == null) {
            player.sendMessage(Component.text("Economy system unavailable!", NamedTextColor.RED));
            return;
        }

        Transaction transaction = new Transaction(player, economy, dataLoader, marketManager);
        boolean success = action.equalsIgnoreCase("buy")
                ? transaction.performBuyTransaction(itemName, amountOfItemsSelected)
                : transaction.performSellTransaction(itemName, amountOfItemsSelected);

        if (success) {
            player.playSound(player.getLocation(), getTransactionSound(action), 1.0f, 1.0f);
            marketGuiItemNegotiation.close(player);
        }
    }

    // Amount control methods
    private void addAmountControls() {
        for (int i = 0; i < AMOUNTS.length; i++) {
            int amount = AMOUNTS[i];
            marketGuiItemNegotiation.setItem(ADD_SLOTS[i], createAmountControl(
                    Material.GREEN_STAINED_GLASS_PANE,
                    amount,
                    true
            ));
            marketGuiItemNegotiation.setItem(DEDUCT_SLOTS[i], createAmountControl(
                    Material.RED_STAINED_GLASS_PANE,
                    amount,
                    false
            ));
        }
    }

    private GuiItem createAmountControl(Material material, int amount, boolean isAddition) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        String action = isAddition ? "Add" : "Deduct";

        meta.displayName(Component.text(action + " " + amount,
                isAddition ? NamedTextColor.GREEN : NamedTextColor.RED));
        button.setItemMeta(meta);

        return new GuiItem(button, event -> {
            updateSelectedAmount(amount, isAddition);
            Player player = (Player) event.getWhoClicked();
            provideAmountFeedback(player, isAddition);
            refreshTransactionButtons();
        });
    }

    private void updateSelectedAmount(int delta, boolean isAddition) {
        int newAmount = isAddition
                ? amountOfItemsSelected + delta
                : amountOfItemsSelected - delta;

        amountOfItemsSelected = Math.clamp(newAmount, 1, 2304);
    }

    private void provideAmountFeedback(Player player, boolean isAddition) {
        player.sendMessage(Component.text("Selected: " + amountOfItemsSelected,
                isAddition ? NamedTextColor.GREEN : NamedTextColor.RED));
        player.playSound(player.getLocation(),
                isAddition ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.BLOCK_NOTE_BLOCK_BASS,
                1.0f, 1.0f);
    }

    private void refreshTransactionButtons() {
        marketGuiItemNegotiation.updateItem(BUY_BUTTON_SLOT,
                createTransactionButton(Material.SLIME_BLOCK, "Buy", NamedTextColor.GREEN, currentItemName));
        marketGuiItemNegotiation.updateItem(SELL_BUTTON_SLOT,
                createTransactionButton(Material.HONEY_BLOCK, "Sell", NamedTextColor.GOLD, currentItemName));
    }

    // Utility methods
    private MarketItem getValidMarketItem(String itemName) {
        MarketItem item = dataLoader.getMarketItems().get(itemName);
        if (item == null) {
            marketPlugin.getLogger().severe("Invalid item: " + itemName);
        }
        return item;
    }

    private String getItemCategory(String itemName) {
        MarketItem item = getValidMarketItem(itemName);
        return item != null ? item.getCategory() : "";
    }

    private Sound getTransactionSound(String action) {
        return action.equalsIgnoreCase("buy")
                ? Sound.BLOCK_NOTE_BLOCK_BELL
                : Sound.ENTITY_VILLAGER_YES;
    }

    // State management
    private int getAmountOfItemsSelected() {
        return amountOfItemsSelected;
    }

    private void setAmountOfItemsSelected(int amount) {
        this.amountOfItemsSelected = Math.clamp(amount, 1, 2304);
    }

    public void resetAmountOfItemsSelected() {
        setAmountOfItemsSelected(1);
    }

    private String formatPrice(BigDecimal price) {
        // Create a locale-specific formatter (e.g., for European-style formatting)
        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMANY); // Uses . for thousands and , for decimals
        formatter.setMinimumFractionDigits(2); // Always show 2 decimal places
        formatter.setMaximumFractionDigits(2); // Never show more than 2 decimal places
        return "$" + formatter.format(price.doubleValue()); // Use € symbol (or $ if preferred)
    }
}