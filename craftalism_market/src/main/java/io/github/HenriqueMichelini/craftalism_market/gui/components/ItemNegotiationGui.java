package io.github.HenriqueMichelini.craftalism_market.gui.components;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.gui.GuiManager;
import io.github.HenriqueMichelini.craftalism_market.logic.DataLoader;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketManager;
import io.github.HenriqueMichelini.craftalism_market.core.Transaction;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemNegotiationGui extends BaseGui {
    // Region: Constants
    private static final int
            CENTER_SLOT = 13,
            BUY_BUTTON_SLOT = 38,
            SELL_BUTTON_SLOT = 42,
            MIN_AMOUNT = 1,
            MAX_AMOUNT = 2304;

    private static final int[]
            ADD_SLOTS = {0, 9, 18, 27, 36, 45},
            DEDUCT_SLOTS = {8, 17, 26, 35, 44, 53},
            AMOUNTS = {1, 8, 32, 64, 576, 2304};

    // Region: Fields
    private final MarketItem item;
    private final String itemName;
    private final DataLoader dataLoader;
    private final MarketManager marketManager;
    private final GuiManager guiManager;
    private int selectedAmount = MIN_AMOUNT;

    // Region: Constructor
    public ItemNegotiationGui(
            String itemName,
            CraftalismMarket plugin,
            DataLoader dataLoader,
            MarketManager marketManager,
            Consumer<Player> onBack, GuiManager guiManager
    ) {
        super("Market", 6, plugin);
        this.itemName = itemName;
        this.dataLoader = dataLoader;
        this.marketManager = marketManager;
        this.item = validateItem(itemName);
        this.guiManager = guiManager;
        initialize(onBack);
    }

    // Region: Initialization
    private void initialize(Consumer<Player> onBack) {
        if (item == null) return;

        configureCloseHandler();
        addItemDisplay();
        addTransactionButtons();
        addAmountControls();
        addBackButton(onBack);
    }

    private MarketItem validateItem(String itemName) {
        MarketItem item = dataLoader.getMarketItems().get(itemName);
        if (item == null) {
            plugin.getLogger().severe("Invalid item: " + itemName);
        }
        return item;
    }

    private void configureCloseHandler() {
        gui.setCloseGuiAction(event -> resetAmount());
    }

    // Region: Item Display
    private void addItemDisplay() {
        ItemStack displayItem = new ItemStack(item.getMaterial());
        ItemMeta meta = displayItem.getItemMeta();
        meta.lore(createDetailedLore());
        displayItem.setItemMeta(meta);
        gui.setItem(CENTER_SLOT, ItemBuilder.from(displayItem).asGuiItem());
    }

    private List<Component> createDetailedLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Buy Price: " + formatPrice(item.getBasePrice()), NamedTextColor.GREEN));
        lore.add(Component.text("Sell Tax: " + formatPercentage(item.getSellTax()), NamedTextColor.RED));
        lore.add(Component.text("Stock: " + item.getAmount(), NamedTextColor.AQUA));
        lore.add(Component.empty());
        addPriceHistory(lore);
        return lore;
    }

    private void addPriceHistory(List<Component> lore) {
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

    // Region: Transaction Handling
    private void addTransactionButtons() {
        gui.setItem(BUY_BUTTON_SLOT, createTransactionButton(
                Material.SLIME_BLOCK, "Buy", NamedTextColor.GREEN, this::handleBuy));
        gui.setItem(SELL_BUTTON_SLOT, createTransactionButton(
                Material.HONEY_BLOCK, "Sell", NamedTextColor.GOLD, this::handleSell));
    }

    private GuiItem createTransactionButton(Material material, String action, NamedTextColor color, Consumer<Player> handler) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text(action, color));
        meta.lore(createTransactionLore(action));
        button.setItemMeta(meta);
        return new GuiItem(button, event -> {
            handler.accept((Player) event.getWhoClicked());
            refreshTransactionButtons();
        });
    }

    private List<Component> createTransactionLore(String action) {
        BigDecimal totalPrice = calculateTotalPrice(action);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Price/Unit: " + formatPrice(item.getBasePrice()), NamedTextColor.WHITE));
        lore.add(Component.text("Quantity: " + selectedAmount, NamedTextColor.WHITE));

        if ("sell".equalsIgnoreCase(action)) {
            BigDecimal tax = calculateTaxAmount(totalPrice);
            lore.add(Component.text("Before tax: " + formatPrice(totalPrice), NamedTextColor.WHITE));
            lore.add(Component.text("Tax: " + formatPrice(tax), NamedTextColor.RED));
            lore.add(Component.text("Total: " + formatPrice(totalPrice.subtract(tax)), NamedTextColor.YELLOW));
        } else {
            lore.add(Component.text("Total: " + formatPrice(totalPrice), NamedTextColor.YELLOW));
        }
        return lore;
    }

    // Region: Amount Controls
    private void addAmountControls() {
        for (int i = 0; i < AMOUNTS.length; i++) {
            gui.setItem(ADD_SLOTS[i], createAmountControl(AMOUNTS[i], true));
            gui.setItem(DEDUCT_SLOTS[i], createAmountControl(AMOUNTS[i], false));
        }
    }

    private GuiItem createAmountControl(int amount, boolean isAddition) {
        Material material = isAddition ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        NamedTextColor color = isAddition ? NamedTextColor.GREEN : NamedTextColor.RED;
        String action = isAddition ? "Add" : "Deduct";

        return createButton(
                material,
                Component.text(action + " " + amount, color),
                List.of(),
                player -> { // 'player' is already the correct type (Player)
                    handleAmountChange(amount, isAddition, player);
                }
        );
    }

    private void handleAmountChange(int delta, boolean isAddition, Player player) {
        selectedAmount = clampAmount(isAddition ? selectedAmount + delta : selectedAmount - delta);
        provideAmountFeedback(player, isAddition);
        refreshTransactionButtons();
    }

    // Region: Core Logic
    private BigDecimal calculateTotalPrice(String action) {
        return marketManager.getTotalPriceOfItem(
                item,
                selectedAmount,
                "buy".equalsIgnoreCase(action)
        );
    }

    private BigDecimal calculateTaxAmount(BigDecimal totalPrice) {
        return totalPrice.multiply(item.getSellTax()).setScale(2, RoundingMode.HALF_UP);
    }

    private void handleBuy(Player player) {
        Transaction transaction = new Transaction(
                player,
                plugin.getEconomyManager(),
                dataLoader,
                marketManager
        );

        if (transaction.performBuyTransaction(itemName, selectedAmount)) {
            playTransactionSound(player, "buy");
            guiManager.refreshCategoryItem(item.getCategory(), itemName);
            gui.close(player); // Close the GUI
        } else {
            player.sendMessage(Component.text("Failed to complete purchase!", NamedTextColor.RED));
        }
    }

    private void handleSell(Player player) {
        Transaction transaction = new Transaction(
                player,
                plugin.getEconomyManager(),
                dataLoader,
                marketManager
        );

        if (transaction.performSellTransaction(itemName, selectedAmount)) {
            playTransactionSound(player, "sell");
            guiManager.refreshCategoryItem(item.getCategory(), itemName);
            gui.close(player); // Close the GUI
        } else {
            player.sendMessage(Component.text("Failed to complete sale!", NamedTextColor.RED));
        }
    }

    // Region: Utility Methods
    private void refreshTransactionButtons() {
        gui.updateItem(BUY_BUTTON_SLOT, createTransactionButton(
                Material.SLIME_BLOCK, "Buy", NamedTextColor.GREEN, this::handleBuy));
        gui.updateItem(SELL_BUTTON_SLOT, createTransactionButton(
                Material.HONEY_BLOCK, "Sell", NamedTextColor.GOLD, this::handleSell));
    }

    private void resetAmount() {
        selectedAmount = MIN_AMOUNT;
        refreshTransactionButtons();
    }

    private int clampAmount(int amount) {
        return Math.max(MIN_AMOUNT, Math.min(amount, MAX_AMOUNT));
    }

    private void provideAmountFeedback(Player player, boolean isAddition) {
        player.sendMessage(Component.text("Selected: " + selectedAmount,
                isAddition ? NamedTextColor.GREEN : NamedTextColor.RED));
        player.playSound(player.getLocation(),
                isAddition ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.BLOCK_NOTE_BLOCK_BASS,
                1.0f, 1.0f);
    }

    private void playTransactionSound(Player player, String action) {
        Sound sound = "buy".equals(action)
                ? Sound.BLOCK_NOTE_BLOCK_BELL
                : Sound.ENTITY_VILLAGER_YES;
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
}