package io.github.HenriqueMichelini.craftalism_market.gui.components;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_market.CraftalismMarket;
import io.github.HenriqueMichelini.craftalism_market.config.ConfigManager;
import io.github.HenriqueMichelini.craftalism_market.gui.manager.GuiManager;
import io.github.HenriqueMichelini.craftalism_market.logic.MarketUtils;
import io.github.HenriqueMichelini.craftalism_market.core.TransactionHandler;
import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;
import io.github.HenriqueMichelini.craftalism_market.stock.StockHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TradeGUI extends BaseGUI {
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
    private final ConfigManager configManager;
    private final MarketUtils marketUtils;
    private final GuiManager guiManager;
    private final StockHandler stockHandler;
    private int selectedAmount = MIN_AMOUNT;

    private int lastRefreshAmount = -1;

    // Region: Constructor
    public TradeGUI(
            String itemName,
            CraftalismMarket plugin,
            ConfigManager configManager,
            MarketUtils marketUtils,
            Consumer<Player> onBack, GuiManager guiManager,
            StockHandler stockHandler
    ) {
        super("Trading: " + itemName, 6, plugin);
        this.itemName = itemName;
        this.configManager = configManager;
        this.marketUtils = marketUtils;
        this.item = validateItem(itemName);
        this.guiManager = guiManager;
        this.stockHandler = stockHandler;
        initialize(onBack);
    }

    // In TradeGUI.java
    @Override
    public void open(Player player) {
        super.open(player);
        guiManager.registerTradeGui(itemName, this);
    }

    @Override
    protected void onClose(Player player) {
        guiManager.unregisterTradeGui(itemName, this);
    }

    public void refresh() {
        try {
            // Update all dynamic components
            updateItemDisplay();
            refreshTransactionButtons();
            refreshAmountControls();

            // Update back button reference
            gui.updateItem(BACK_BUTTON_SLOT, createButton(
                    Material.BARRIER,
                    Component.text("Back", NamedTextColor.RED),
                    List.of(),
                    p -> guiManager.returnToCategory(p, item.getCategory())
            ));

            // Force GUI redraw
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!gui.getInventory().getViewers().isEmpty()) {
                    gui.update();
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to refresh TradeGUI: " + e.getMessage());
            Component errorMsg = Component.text("GUI Error - Please reopen", NamedTextColor.RED);
            gui.getInventory().getViewers().forEach(viewer -> {
                if (viewer instanceof Player player) {
                    player.sendMessage(errorMsg);
                    gui.close(player);
                }
            });
        }
    }

    // Region: Initialization
    private void initialize(Consumer<Player> onBack) {
        if (item == null) return;
        addItemDisplay();
        addTransactionButtons();
        addAmountControls();
        addBackButton(onBack);
    }

    private MarketItem validateItem(String itemName) {
        MarketItem item = configManager.getItems().get(itemName);
        if (item == null) {
            plugin.getLogger().severe("Invalid item: " + itemName);
        }
        return item;
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
        lore.add(Component.text("Buy Price: " + formatPrice(item.getCurrentPrice()), NamedTextColor.GREEN));
        lore.add(Component.text("Sell Tax: " + formatPercentage(item.getTaxRate()), NamedTextColor.RED));
        lore.add(Component.text("Stock: " + item.getCurrentStock(), NamedTextColor.AQUA));
        lore.add(Component.empty());
        addPriceHistory(lore);
        return lore;
    }

    private void addPriceHistory(List<Component> lore) {
        List<BigDecimal> history = item.getPriceHistory();
        int maxEntries = 5; // Show last 5 prices

        if (history.isEmpty()) {
            lore.add(Component.text("Price History: No data", NamedTextColor.GRAY));
            return;
        }

        lore.add(Component.text("Price History:", NamedTextColor.DARK_AQUA));
        history.stream()
                .skip(Math.max(0, history.size() - maxEntries))
                .forEach(price ->
                        lore.add(Component.text("⏺ ", NamedTextColor.DARK_GRAY)
                                .append(Component.text(formatPrice(price), NamedTextColor.GRAY)))
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
        return List.of(
                Component.text("➤ Unit Price: ", NamedTextColor.GRAY)
                        .append(Component.text(formatPrice(item.getCurrentPrice()), NamedTextColor.WHITE)),
                Component.text("➤ Quantity: ", NamedTextColor.GRAY)
                        .append(Component.text(selectedAmount, NamedTextColor.WHITE)),
                createTotalComponent(action)
        );
    }

    private Component createTotalComponent(String action) {
        BigDecimal total = calculateTotalPrice(action);
        NamedTextColor color = "buy".equalsIgnoreCase(action) ? NamedTextColor.GREEN : NamedTextColor.GOLD;

        return Component.text("➤ Total: ", NamedTextColor.GRAY)
                .append(Component.text(formatPrice(total), color));
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

    private void updateItemDisplay() {
        ItemStack displayItem = new ItemStack(item.getMaterial());
        ItemMeta meta = displayItem.getItemMeta();
        meta.lore(createDetailedLore());
        displayItem.setItemMeta(meta);
        gui.updateItem(CENTER_SLOT, ItemBuilder.from(displayItem).asGuiItem());
    }

    private void refreshAmountControls() {
        for (int i = 0; i < AMOUNTS.length; i++) {
            gui.updateItem(ADD_SLOTS[i], createAmountControl(AMOUNTS[i], true));
            gui.updateItem(DEDUCT_SLOTS[i], createAmountControl(AMOUNTS[i], false));
        }
    }

    private void handleAmountChange(int delta, boolean isAddition, Player player) {
        selectedAmount = clampAmount(isAddition ? selectedAmount + delta : selectedAmount - delta);
        provideAmountFeedback(player, isAddition);
        refreshTransactionButtons();
    }

    // Region: Core Logic
    private BigDecimal calculateTotalPrice(String action) {
        return marketUtils.getTotalPriceOfItem(
                item,
                selectedAmount,
                "buy".equalsIgnoreCase(action)
        );
    }

    private void handleBuy(Player player) {
        TransactionHandler transactionHandler = new TransactionHandler(
                player,
                plugin.getEconomyManager(),
                configManager,
                marketUtils,
                stockHandler
        );

        if (transactionHandler.performBuyTransaction(itemName, selectedAmount)) {
            playUiSound(player, "success");
            guiManager.refreshCategoryItem(item.getCategory(), itemName);
            gui.close(player); // Close the GUI
        } else {
            playUiSound(player, "error");
            player.sendMessage(Component.text("Failed to complete purchase!", NamedTextColor.RED));
        }
    }

    private void handleSell(Player player) {
        TransactionHandler transactionHandler = new TransactionHandler(
                player,
                plugin.getEconomyManager(),
                configManager,
                marketUtils,
                stockHandler
        );

        if (transactionHandler.performSellTransaction(itemName, selectedAmount)) {
            playUiSound(player, "success");
            guiManager.refreshCategoryItem(item.getCategory(), itemName);
            gui.close(player); // Close the GUI
        } else {
            playUiSound(player, "error");
            player.sendMessage(Component.text("Failed to complete sale!", NamedTextColor.RED));
        }
    }

    // Region: Utility Methods
    private void refreshTransactionButtons() {
        if (lastRefreshAmount != selectedAmount) {
            lastRefreshAmount = selectedAmount;
            gui.updateItem(BUY_BUTTON_SLOT, createTransactionButton(
                    Material.SLIME_BLOCK, "Buy", NamedTextColor.GREEN, this::handleBuy));
            gui.updateItem(SELL_BUTTON_SLOT, createTransactionButton(
                    Material.HONEY_BLOCK, "Sell", NamedTextColor.GOLD, this::handleSell));
        }
    }

    protected void resetAmount() {
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

}