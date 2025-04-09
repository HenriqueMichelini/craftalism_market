package io.github.HenriqueMichelini.craftalism_market.task;

import io.github.HenriqueMichelini.craftalism_market.stock.StockHandler;
import org.bukkit.scheduler.BukkitRunnable;

public class StockUpdateTask extends BukkitRunnable {
    private final StockHandler stockHandler;

    public StockUpdateTask(StockHandler stockHandler) {
        this.stockHandler = stockHandler;
    }

    @Override
    public void run() {
        System.out.println("Processing stock updates (Interval: " +
                stockHandler.getUpdateIntervalMinutes() + " minutes)");
        stockHandler.getUpdateIntervalMinutes(); // Force config reload
        stockHandler.processAllActiveItems();
    }
}