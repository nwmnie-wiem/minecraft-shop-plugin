package com.minecraft.shop.pricing;

import com.minecraft.shop.ShopPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class PricingManager {

    private final ShopPlugin plugin;
    private Map<String, PriceData> priceCache;
    private static final double DEFAULT_BUY_MULTIPLIER = 2.0;
    private static final double DEFAULT_SELL_MULTIPLIER = 0.25;

    public PricingManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.priceCache = new HashMap<>();
        loadPrices();
    }

    /**
     * Load prices from configuration
     */
    public void loadPrices() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        FileConfiguration items = plugin.getConfigManager().getItems();

        boolean autoLoadItems = config.getBoolean("economy.auto-load-items", true);
        double sellMultiplier = config.getDouble("economy.sell-multiplier", DEFAULT_SELL_MULTIPLIER);
        double buyMultiplier = config.getDouble("economy.buy-multiplier", DEFAULT_BUY_MULTIPLIER);

        Map<String, Object> itemsConfig = items.getConfigurationSection("items").getValues(false);

        for (String itemKey : itemsConfig.keySet()) {
            Map<String, Object> itemData = (Map<String, Object>) itemsConfig.get(itemKey);
            double buyPrice = ((Number) itemData.get("buy-price")).doubleValue();
            double sellPrice = ((Number) itemData.get("sell-price")).doubleValue();
            double minSellPrice = ((Number) itemData.getOrDefault("min-sell-price", sellPrice * 0.5)).doubleValue();
            double maxSellPrice = ((Number) itemData.getOrDefault("max-sell-price", sellPrice * 2.0)).doubleValue();
            boolean dynamicPricing = (boolean) itemData.getOrDefault("dynamic-pricing", true);

            PriceData priceData = new PriceData();
            priceData.baseBuyPrice = buyPrice;
            priceData.baseSellPrice = sellPrice;
            priceData.currentBuyPrice = buyPrice;
            priceData.currentSellPrice = sellPrice;
            priceData.minSellPrice = minSellPrice;
            priceData.maxSellPrice = maxSellPrice;
            priceData.dynamicPricing = dynamicPricing;
            priceData.lastUpdated = System.currentTimeMillis();

            priceCache.put(itemKey, priceData);
        }

        plugin.getLogger().info("§a✓ Załadowano " + priceCache.size() + " przedmiotów");
    }

    /**
     * Get current buy price for an item
     */
    public double getCurrentBuyPrice(String itemKey) {
        PriceData data = priceCache.get(itemKey);
        return data != null ? data.currentBuyPrice : 0;
    }

    /**
     * Get current sell price for an item
     */
    public double getCurrentSellPrice(String itemKey) {
        PriceData data = priceCache.get(itemKey);
        return data != null ? data.currentSellPrice : 0;
    }

    /**
     * Apply price change due to transaction
     */
    public void applyPriceChange(String itemKey, boolean isSell, int amount) {
        PriceData data = priceCache.get(itemKey);
        if (data == null || !data.dynamicPricing) {
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getConfig();
        double sellImpact = config.getDouble("dynamic-pricing.sell-impact", 1.0);
        double buyImpact = config.getDouble("dynamic-pricing.buy-impact", 0.5);
        double maxChangePercent = config.getDouble("dynamic-pricing.max-change-per-transaction", 2);

        double impact = isSell ? sellImpact : buyImpact;
        double changePercent = Math.min(maxChangePercent, (amount / 64.0) * impact);

        if (isSell) {
            // Selling decreases price
            data.currentSellPrice *= (1 - (changePercent / 100));
            data.currentSellPrice = Math.max(data.minSellPrice, data.currentSellPrice);
        } else {
            // Buying increases price
            data.currentBuyPrice *= (1 + (changePercent / 100));
            data.currentBuyPrice = Math.min(data.maxSellPrice, data.currentBuyPrice);
        }

        data.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Get price change percentage from base price
     */
    public double getPriceChangePercent(String itemKey) {
        PriceData data = priceCache.get(itemKey);
        if (data == null) {
            return 0;
        }
        return ((data.currentSellPrice - data.baseSellPrice) / data.baseSellPrice) * 100;
    }

    /**
     * Reset prices for an item
     */
    public void resetPrice(String itemKey) {
        PriceData data = priceCache.get(itemKey);
        if (data != null) {
            data.currentBuyPrice = data.baseBuyPrice;
            data.currentSellPrice = data.baseSellPrice;
            data.lastUpdated = System.currentTimeMillis();
        }
    }

    /**
     * Reset all prices
     */
    public void resetAllPrices() {
        for (PriceData data : priceCache.values()) {
            data.currentBuyPrice = data.baseBuyPrice;
            data.currentSellPrice = data.baseSellPrice;
            data.lastUpdated = System.currentTimeMillis();
        }
    }

    /**
     * Gradual price reset towards base price
     */
    public void applyGradualPriceReset() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        double gradualSpeed = config.getDouble("dynamic-pricing.price-reset.gradual-speed", 5);
        double speedPercent = gradualSpeed / 100;

        for (PriceData data : priceCache.values()) {
            if (!data.dynamicPricing) continue;

            // Move towards base price
            data.currentSellPrice += (data.baseSellPrice - data.currentSellPrice) * speedPercent;
            data.currentBuyPrice += (data.baseBuyPrice - data.currentBuyPrice) * speedPercent;
        }
    }

    /**
     * Inner class for storing price data
     */
    public static class PriceData {
        public double baseBuyPrice;
        public double baseSellPrice;
        public double currentBuyPrice;
        public double currentSellPrice;
        public double minSellPrice;
        public double maxSellPrice;
        public boolean dynamicPricing;
        public long lastUpdated;
    }
}
