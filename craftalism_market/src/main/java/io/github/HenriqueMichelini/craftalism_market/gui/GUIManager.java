package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.HenriqueMichelini.craftalism_economy.economy.EconomyManager;
import io.github.HenriqueMichelini.craftalism_market.logic.DataLoader;
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
import java.util.Map;

public class GUIManager {
    private final DataLoader dataLoader;
    private final EconomyManager economyManager;
    private Gui marketGui;
    private Gui marketGuiItemsByCategory;
    private Gui marketGuiItemNegotiation;

    private String itemName = "";

    private int amountOfItemsSelected = 1;

    // Constants for slot numbers
    private static final int CENTER_SLOT = 13;
    private static final int[] ADD_SLOTS = {0, 9, 18, 27, 36, 45};
    private static final int[] DEDUCT_SLOTS = {8, 17, 26, 35, 44, 53};
    private static final int[] AMOUNTS = {1, 8, 32, 64, 576, 2304};
    private static final int BUY_BUTTON_SLOT = 38;
    private static final int SELL_BUTTON_SLOT = 42;

    public GUIManager(DataLoader dataLoader, EconomyManager economyManager) {
        this.dataLoader = dataLoader;
        this.economyManager = economyManager;
    }

    public void openMarket(Player player) {
        createMarketGui();
        populateMarketGui();
        marketGui.open(player);
    }

    private void createMarketGui() {
        marketGui = Gui.gui()
                .title(Component.text("Market", NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions()
                .create();
    }

    private void populateMarketGui() {
        Map<String, MarketCategoryItem> marketCategories = dataLoader.getMarketCategories();

        for (Map.Entry<String, MarketCategoryItem> entry : marketCategories.entrySet()) {
            MarketCategoryItem categoryItem = entry.getValue();

            ItemStack itemStack = new ItemStack(categoryItem.getMaterial());
            GuiItem guiItem = ItemBuilder.from(itemStack)
                    .name(Component.text(categoryItem.getTitle(), NamedTextColor.GREEN))
                    .asGuiItem(event -> openMarketItemsByCategory(categoryItem.getTitle(), (Player) event.getWhoClicked()));

            marketGui.setItem(categoryItem.getSlot(), guiItem);
        }
    }

    public void openMarketItemsByCategory(String category, Player player) {
        createMarketGuiItemsByCategory(category);
        populateMarketGuiItemsByCategory(category);
        marketGuiItemsByCategory.open(player);
    }

    private void createMarketGuiItemsByCategory(String categoryItem) {
        marketGuiItemsByCategory = Gui.gui()
                .title(Component.text(categoryItem, NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions()
                .create();
    }

    private GuiItem createBackToMarketGuiButton() {
        ItemStack itemStack = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(Component.text("Back to Market", NamedTextColor.RED));
        itemStack.setItemMeta(itemMeta);

        return new GuiItem(itemStack, event -> openMarket((Player) event.getWhoClicked()));
    }

    private void populateMarketGuiItemsByCategory(String category) {
        marketGuiItemsByCategory.setItem(49, createBackToMarketGuiButton());

        Map<String, MarketItem> marketItems = dataLoader.getMarketItems();

        for (Map.Entry<String, MarketItem> entry : marketItems.entrySet()) {
            String itemName = entry.getKey();
            MarketItem item = entry.getValue();

            if (!item.getCategory().equalsIgnoreCase(category)) continue;

            ItemStack itemStack = new ItemStack(item.getMaterial());
            GuiItem guiItem = ItemBuilder.from(itemStack)
                    .name(Component.text(itemName, NamedTextColor.GREEN))
                    .lore(
                            Component.text("Price: $" + item.getPrice()),
                            Component.text("Stock: " + item.getAmount())
                    )
                    .asGuiItem(event -> openItemNegotiation((Player) event.getWhoClicked(), itemName));

            marketGuiItemsByCategory.setItem(item.getSlot(), guiItem);
        }
    }

    public void openItemNegotiation(Player player, String itemName) {
        setItemName(itemName);
        createMarketGuiItemNegotiation(itemName);
        populateMarketGuiItemNegotiation(itemName);
        marketGuiItemNegotiation.open(player);
    }

    private void createMarketGuiItemNegotiation(String itemName) {
        marketGuiItemNegotiation = Gui.gui()
                .title(Component.text("Market", NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions()
                .create();

        // Add a close event listener to reset amountOfItemsSelected
        marketGuiItemNegotiation.setDefaultClickAction(event -> event.setCancelled(true)); // Prevent item movement
        marketGuiItemNegotiation.setCloseGuiAction(event -> {
            // Reset the amountOfItemsSelected when the GUI is closed
            setAmountOfItemsSelected(1);
        });
    }

    private GuiItem createBackToMarketGuiItemsByCategoryButton(String category) {
        ItemStack itemStack = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(Component.text("Back to " + category, NamedTextColor.RED));
        itemStack.setItemMeta(itemMeta);

        return new GuiItem(itemStack, event -> openMarketItemsByCategory(category, (Player) event.getWhoClicked()));
    }

    private void populateMarketGuiItemNegotiation(String itemName) {
        Map<String, MarketItem> marketItems = dataLoader.getMarketItems();
        MarketItem item = marketItems.get(itemName);
        String category = item.getCategory();

        marketGuiItemNegotiation.setItem(49, createBackToMarketGuiItemsByCategoryButton(category));

        marketGuiItemNegotiation.setItem(BUY_BUTTON_SLOT, createBuyButton(itemName));
        marketGuiItemNegotiation.setItem(SELL_BUTTON_SLOT, createSellButton(itemName));

        if (item == null) {
            System.out.println("Item not found: " + itemName); // Replace with proper logging
            return;
        }

        // Display item details
        ItemStack itemStack = new ItemStack(item.getMaterial());
        GuiItem displayItem = ItemBuilder.from(itemStack)
                .name(Component.text(itemName, NamedTextColor.GREEN))
                .lore(
                        Component.text("Price: $" + item.getPrice()),
                        Component.text("Stock: " + item.getAmount())
                )
                .asGuiItem();

        marketGuiItemNegotiation.setItem(CENTER_SLOT, displayItem);

        // Add amount buttons
        populateAmountButtons();
    }

    private void populateAmountButtons() {
        for (int i = 0; i < AMOUNTS.length; i++) {
            int amount = AMOUNTS[i];
            marketGuiItemNegotiation.setItem(ADD_SLOTS[i], createAmountButton(Material.GREEN_STAINED_GLASS_PANE, amount, NamedTextColor.GREEN, true));
            marketGuiItemNegotiation.setItem(DEDUCT_SLOTS[i], createAmountButton(Material.RED_STAINED_GLASS_PANE, amount, NamedTextColor.RED, false));
        }
    }

    private GuiItem createAmountButton(Material material, int amount, NamedTextColor color, boolean isAdding) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        String action = isAdding ? "ADD" : "DEDUCT";
        String title = String.format("%s %d", action, amount);

        itemMeta.displayName(Component.text(title, color));
        itemStack.setItemMeta(itemMeta);

        return ItemBuilder.from(itemStack).asGuiItem(event -> {
            adjustSelectedItemAmount(amount, isAdding);
            sendMessageToPlayerOfAdjustSelectedItemAmount((Player) event.getWhoClicked(), amountOfItemsSelected, isAdding);
            marketGuiItemNegotiation.updateItem(BUY_BUTTON_SLOT, createBuyButton(itemName));
            marketGuiItemNegotiation.updateItem(SELL_BUTTON_SLOT, createSellButton(itemName));
        });
    }

    private void adjustSelectedItemAmount(int buttonAmount, boolean isAddition) {
        int newAmount = isAddition
                ? amountOfItemsSelected + buttonAmount
                : amountOfItemsSelected - buttonAmount;

        int clampedAmount = Math.max(1, Math.min(2304, newAmount));
        setAmountOfItemsSelected(clampedAmount);
    }

    private void sendMessageToPlayerOfAdjustSelectedItemAmount(Player player, int amountOfItemsSelected, boolean isAdding) {
        Component message = isAdding ? Component.text("Amount selected: " + amountOfItemsSelected, NamedTextColor.GREEN) : Component.text("Amount selected: " + amountOfItemsSelected, NamedTextColor.RED);
        player.sendMessage(message);
    }

    private GuiItem createBuyButton(String itemName) {
        ItemStack itemStack = new ItemStack(Material.SLIME_BLOCK);
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set the display name
        itemMeta.displayName(Component.text("Buy", NamedTextColor.GREEN));

        // Get the item data
        Map<String, MarketItem> marketItems = dataLoader.getMarketItems();
        MarketItem item = marketItems.get(itemName);

        // Calculate the total price
        BigDecimal pricePerAmount = item.getPrice();
        int totalAmount = getAmountOfItemsSelected();
        BigDecimal totalPrice = pricePerAmount.multiply(BigDecimal.valueOf(totalAmount));

        // Initialize and set the lore
        itemMeta.lore(java.util.Arrays.asList(
                Component.text("Price per amount: $" + pricePerAmount, NamedTextColor.WHITE),
                Component.text("Amount selected: " + totalAmount, NamedTextColor.WHITE),
                Component.text("Total price: $" + totalPrice, NamedTextColor.YELLOW)
        ));

        itemStack.setItemMeta(itemMeta);

        return new GuiItem(itemStack, event -> {
            Player player = (Player) event.getWhoClicked();
            Transaction transaction = new Transaction(player, economyManager, dataLoader);
            transaction.performBuyTransaction(itemName, getAmountOfItemsSelected());
            marketGuiItemNegotiation.close(player);
        });
    }

    private GuiItem createSellButton(String itemName) {
        ItemStack itemStack = new ItemStack(Material.HONEY_BLOCK);
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set the display name
        itemMeta.displayName(Component.text("Sell", NamedTextColor.GOLD));

        // Get the item data
        Map<String, MarketItem> marketItems = dataLoader.getMarketItems();
        MarketItem item = marketItems.get(itemName);

        // Calculate the total price
        BigDecimal pricePerAmount = item.getPrice();
        int totalAmount = getAmountOfItemsSelected();
        BigDecimal totalPrice = pricePerAmount.multiply(BigDecimal.valueOf(totalAmount));

        // Initialize and set the lore
        itemMeta.lore(java.util.Arrays.asList(
                Component.text("Price per amount: $" + pricePerAmount, NamedTextColor.WHITE),
                Component.text("Amount selected: " + totalAmount, NamedTextColor.WHITE),
                Component.text("Total price: $" + totalPrice, NamedTextColor.YELLOW)
        ));

        itemStack.setItemMeta(itemMeta);

        return new GuiItem(itemStack, event -> {
            Player player = (Player) event.getWhoClicked();
            Transaction transaction = new Transaction(player, economyManager, dataLoader);
            transaction.performSellTransaction(itemName, getAmountOfItemsSelected());
            marketGuiItemNegotiation.close(player);
        });
    }

//    private Sound playSoundWhenAdd() {
//        return //sound
//    }
//
//    private Sound playSoundWhenDeduct() {
//        return //sound
//    }
//
//    private Sound playSoundWhenBuyOrSell() {
//        return //sound
//    }

    private int getAmountOfItemsSelected() {
        return amountOfItemsSelected;
    }

    private void setAmountOfItemsSelected(int amountOfItemsSelected) {
        this.amountOfItemsSelected = amountOfItemsSelected;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}