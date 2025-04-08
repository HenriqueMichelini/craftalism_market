package io.github.HenriqueMichelini.craftalism_market.stock.listener;

import io.github.HenriqueMichelini.craftalism_market.models.MarketItem;

public interface StockUpdateListener {
    void onStockUpdated(MarketItem item);
}
