package io.github.HenriqueMichelini.craftalism_market.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemNegotiation {
    private final Gui gui;
    private final Material itemTarget;
    private int itemAmount = 1;
    private final Player player;
    private final String subCategoryTitle;

    private static final int ROW_INVENTORY_SIZE = 576;
    private static final int FULL_INVENTORY_SIZE = 2304;
    private static final int MIN_AMOUNT = 1;

    public ItemNegotiation(Material itemTarget, Player player, String subCategoryTitle) {
        this.itemTarget = itemTarget;
        this.player = player;
        this.subCategoryTitle = subCategoryTitle;
        gui = Gui.gui()
                .title(Component.text("Item", NamedTextColor.GREEN))
                .rows(6)
                .disableAllInteractions() // Disable all interactions by default
                .create();
        populateGui();
    }

    private void populateGui() {
        GuiItem add1 = createAmountButton(Material.LIME_STAINED_GLASS_PANE, 1, true);
        GuiItem add8 = createAmountButton(Material.LIME_STAINED_GLASS_PANE, 8, true);
        GuiItem add32 = createAmountButton(Material.LIME_STAINED_GLASS_PANE, 32, true);
        GuiItem add64 = createAmountButton(Material.LIME_STAINED_GLASS_PANE, 64, true);

        GuiItem addInventoryRow = createAmountButton(Material.GREEN_STAINED_GLASS_PANE, ROW_INVENTORY_SIZE, true);
        GuiItem addFullInventory = createAmountButton(Material.GREEN_STAINED_GLASS_PANE, FULL_INVENTORY_SIZE, true);

        gui.setItem(0, add1);
        gui.setItem(9, add8);
        gui.setItem(18, add32);
        gui.setItem(27, add64);

        gui.setItem(36, addInventoryRow);
        gui.setItem(45, addFullInventory);

        GuiItem rem1 = createAmountButton(Material.RED_STAINED_GLASS_PANE, 1, false);
        GuiItem rem8 = createAmountButton(Material.RED_STAINED_GLASS_PANE, 8, false);
        GuiItem rem32 = createAmountButton(Material.RED_STAINED_GLASS_PANE, 32, false);
        GuiItem rem64 = createAmountButton(Material.RED_STAINED_GLASS_PANE, 64, false);

        GuiItem remInventoryRow = createAmountButton(Material.PURPLE_STAINED_GLASS_PANE, ROW_INVENTORY_SIZE, false);
        GuiItem remFullInventory = createAmountButton(Material.PURPLE_STAINED_GLASS_PANE, FULL_INVENTORY_SIZE, false);

        gui.setItem(8, rem1);
        gui.setItem(17, rem8);
        gui.setItem(26, rem32);
        gui.setItem(35, rem64);

        gui.setItem(44, remInventoryRow);
        gui.setItem(53, remFullInventory);

        GuiItem targetItem = ItemBuilder.from(itemTarget).asGuiItem();

        gui.setItem(13, targetItem);

        gui.setItem(38, buyButton());

        gui.setItem(42, sellButton());

        addBackButton();
    }

    private GuiItem createAmountButton(Material materialItem, int amount, boolean isAdding) {
        ItemStack item = new ItemStack(materialItem);
        ItemMeta meta = item.getItemMeta();

        // Get localized item name
        Component itemName = Component.translatable(materialItem.translationKey());

        // Determine amount description
        String amountAux = switch (amount) {
            case ROW_INVENTORY_SIZE -> "a row (" + amount + ")";
            case FULL_INVENTORY_SIZE -> "a full inventory (" + amount + ")";
            default -> String.valueOf(amount);
        };

        // Set display name
        meta.displayName(Component.text((isAdding ? "ADD " : "REMOVE ") + amountAux + " of ")
                .append(Component.translatable(itemTarget.translationKey()))
                .color(isAdding ? NamedTextColor.GREEN : NamedTextColor.RED));

        if (amount == FULL_INVENTORY_SIZE) {
            meta.addEnchant(Enchantment.LOYALTY, 1, false);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);

        return ItemBuilder.from(item).amount(1).asGuiItem(event -> {
            if (player == null) return; // Avoid NullPointerException

            if (isAdding) {
                itemAmount = Math.min(itemAmount + amount, FULL_INVENTORY_SIZE);
                playSoundAddAmount(player);
            } else {
                itemAmount = Math.max(itemAmount - amount, MIN_AMOUNT);
                playSoundRemoveAmount(player);
            }

            player.sendMessage(Component.text("Amount of ")
                    .append(Component.translatable(itemTarget.translationKey()))
                    .append(Component.text(": " + itemAmount, NamedTextColor.WHITE)));
        });
    }

    private void updateTargetItem() {
        ItemStack targetItem = new ItemStack(itemTarget);
        ItemMeta meta = targetItem.getItemMeta();

        // Initialize lore as a list with one element if it's null or empty
        List<Component> lore = (meta.lore() == null || Objects.requireNonNull(meta.lore()).isEmpty())
                ? new ArrayList<>() : meta.lore();

        // Ensure that only one lore item exists
        assert lore != null;
        lore.clear();  // Clear any existing lore
        lore.add(Component.text("Item amount: ".concat(String.valueOf(itemAmount)), NamedTextColor.GREEN));

        // Set the updated lore back to the item meta
        meta.lore(lore);

        targetItem.setItemMeta(meta);
        gui.updateItem(13, targetItem);
    }

    public void playSoundAddAmount(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1.0f, 1.0f);
    }

    public void playSoundRemoveAmount(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }

    private GuiItem sellButton() {
        ItemStack item = new ItemStack(Material.HONEY_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Sell " + itemAmount + " of ")
                .append(Component.translatable(itemTarget.translationKey()))
                .append(Component.text(" ($)", NamedTextColor.WHITE)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).amount(1).asGuiItem(event -> {
            player.sendMessage(Component.text("sell").color(NamedTextColor.GREEN));
        });
    }

    private GuiItem buyButton() {
        ItemStack item = new ItemStack(Material.SLIME_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Buy " + itemAmount + " of ")
                .append(Component.translatable(itemTarget.translationKey()))
                .append(Component.text(" ($)", NamedTextColor.WHITE)));
        item.setItemMeta(meta);
        return ItemBuilder.from(item).amount(1).asGuiItem(event -> {
            player.sendMessage(Component.text("buy").color(NamedTextColor.GREEN));
        });
    }

    private void addBackButton() {
        GuiItem backButton = ItemBuilder.from(Material.BARRIER)
                .name(Component.text("Back to " + subCategoryTitle, NamedTextColor.RED))
                .asGuiItem(this::goBackToSubCategories);

        gui.setItem(49, backButton);
    }

    private void goBackToSubCategories(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            MarketItems subCategoryGui = new MarketItems(subCategoryTitle);
            subCategoryGui.getGui().open(player);
        }
    }

    private void performBuyAction() {}

    private void performSellAction() {}

    public Gui getGui() {
        return gui;
    }
}
