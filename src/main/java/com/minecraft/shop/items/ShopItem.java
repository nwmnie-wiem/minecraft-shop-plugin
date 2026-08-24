package com.minecraft.shop.items;

public class ShopItem {
    private String type;
    private String category;
    private double buyPrice;
    private double sellPrice;
    private double minSellPrice;
    private double maxSellPrice;
    private String stock;
    private boolean dynamicPricing;

    public ShopItem(String type, String category, double buyPrice, double sellPrice, 
                    double minSellPrice, double maxSellPrice, String stock, boolean dynamicPricing) {
        this.type = type;
        this.category = category;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.minSellPrice = minSellPrice;
        this.maxSellPrice = maxSellPrice;
        this.stock = stock;
        this.dynamicPricing = dynamicPricing;
    }

    // Getters
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public double getMinSellPrice() { return minSellPrice; }
    public double getMaxSellPrice() { return maxSellPrice; }
    public String getStock() { return stock; }
    public boolean isDynamicPricing() { return dynamicPricing; }

    // Setters
    public void setBuyPrice(double price) { this.buyPrice = price; }
    public void setSellPrice(double price) { this.sellPrice = price; }
}
