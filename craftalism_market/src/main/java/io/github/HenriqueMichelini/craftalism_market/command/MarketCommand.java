package io.github.HenriqueMichelini.craftalism_market.command;
import io.github.HenriqueMichelini.craftalism_market.gui.manager.GuiManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MarketCommand implements CommandExecutor {
    private final GuiManager guiManager;

    public MarketCommand(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        // Validate sender
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        // Open main market GUI
        guiManager.openMarket(player);
        return true;
    }
}