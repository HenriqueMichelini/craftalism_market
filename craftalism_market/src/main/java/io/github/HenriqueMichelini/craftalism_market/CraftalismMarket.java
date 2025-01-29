package io.github.HenriqueMichelini.craftalism_market;

import io.github.HenriqueMichelini.craftalism_market.command.MarketCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CraftalismMarket extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("CraftalismMarket Enabled!");
        Objects.requireNonNull(getCommand("market")).setExecutor(new MarketCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("CraftalismMarket Disabled!");
    }
}
