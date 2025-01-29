package io.github.HenriqueMichelini.craftalism_market.command;

import io.github.HenriqueMichelini.craftalism_market.gui.MarketCategories;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MarketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be run by a player.");
            return false;
        }

        MarketCategories marketCategories = new MarketCategories();
        marketCategories.getGui().open(player);

        return false;
    }
}
