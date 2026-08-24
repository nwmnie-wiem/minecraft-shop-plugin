package com.minecraft.shop.gui;

import com.minecraft.shop.ShopPlugin;
import com.minecraft.shop.items.ShopItem;
import com.minecraft.shop.pricing.PricingManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;

public class ShopGUI {

    private final ShopPlugin plugin;
    private final PricingManager pricingManager;
    private static final String GUI_TYPE_KEY = "shop:gui_type";
    private static final String ITEM_TYPE_KEY = "shop:item_type";
    private static final String ACTION_KEY = "shop:action";

    public ShopGUI(ShopPlugin plugin, PricingManager pricingManager) {
        this.plugin = plugin;
        this.pricingManager = pricingManager;
    }

    /**
     * Open main shop menu (categories)
     */
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§6§lSklep"));

        // Get categories from config
        Map<String, Map<String, Object>> categories = (Map<String, Map<String, Object>>) 
            plugin.getConfigManager().getCategories().getConfigurationSection("categories").getValues(false);

        int slot = 0;
        for (String categoryKey : categories.keySet()) {
            Map<String, Object> catData = (Map<String, Object>) categories.get(categoryKey);
            String name = (String) catData.get("name");
            String displayMaterial = (String) catData.get("display-item");
            List<String> description = (List<String>) catData.get("description");

            ItemStack item = createCategoryItem(name, displayMaterial, description, categoryKey);
            inv.setItem(slot, item);
            slot++;
        }

        // Add search button
        ItemStack searchItem = createButton(Material.COMPASS, "§e§l🔎 SZUKAJ", "Szukaj przedmiotów");
        searchItem.getItemMeta().getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, ACTION_KEY),
            PersistentDataType.STRING,
            "search"
        );
        inv.setItem(22, searchItem);

        // Add favorites button
        ItemStack favoritesItem = createButton(Material.STAR, "§b§l⭐ ULUBIONE", "Twoje ulubione przedmioty");
        favoritesItem.getItemMeta().getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, ACTION_KEY),
            PersistentDataType.STRING,
            "favorites"
        );
        inv.setItem(24, favoritesItem);

        player.openInventory(inv);
    }

    /**
     * Open items list for a category
     */
    public void openCategoryItems(Player player, String categoryKey) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("§6Sklep - Kategoria"));

        // Get items from config
        Map<String, Map<String, Object>> allItems = (Map<String, Map<String, Object>>) 
            plugin.getConfigManager().getItems().getConfigurationSection("items").getValues(false);

        int slot = 0;
        for (String itemKey : allItems.keySet()) {
            Map<String, Object> itemData = (Map<String, Object>) allItems.get(itemKey);
            String itemCategory = (String) itemData.get("category");

            // Only show items from this category
            if (!itemCategory.equalsIgnoreCase(categoryKey)) {
                continue;
            }

            if (slot >= 45) break; // Leave room for navigation

            try {
                Material material = Material.valueOf(itemKey);
                double buyPrice = ((Number) itemData.get("buy-price")).doubleValue();
                double sellPrice = ((Number) itemData.get("sell-price")).doubleValue();
                double currentBuyPrice = pricingManager.getCurrentBuyPrice(itemKey);
                double currentSellPrice = pricingManager.getCurrentSellPrice(itemKey);
                String stock = (String) itemData.get("stock");

                ItemStack item = createShopItem(
                    material,
                    itemKey,
                    currentBuyPrice,
                    currentSellPrice,
                    stock
                );

                inv.setItem(slot, item);
                slot++;
            } catch (IllegalArgumentException e) {
                // Skip invalid materials
            }
        }

        // Add back button
        ItemStack backItem = createButton(Material.ARROW, "§c<- WRÓĆ", "Powrót do kategorii");
        backItem.getItemMeta().getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, ACTION_KEY),
            PersistentDataType.STRING,
            "back"
        );
        inv.setItem(45, backItem);

        player.openInventory(inv);
    }

    /**
     * Create a category item for the main menu
     */
    private ItemStack createCategoryItem(String name, String displayMaterial, List<String> description, String categoryKey) {
        try {
            Material material = Material.valueOf(displayMaterial);
            ItemStack item = new ItemStack(material, 1);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(name)
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD));

            List<Component> lore = new ArrayList<>();
            for (String desc : description) {
                lore.add(Component.text(desc).color(NamedTextColor.GRAY));
            }
            lore.add(Component.text(""));
            lore.add(Component.text("Kliknij aby otworzyć").color(NamedTextColor.AQUA));
            meta.lore(lore);

            // Store category key
            meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "shop:category"),
                PersistentDataType.STRING,
                categoryKey
            );

            item.setItemMeta(meta);
            return item;
        } catch (IllegalArgumentException e) {
            return new ItemStack(Material.BARRIER);
        }
    }

    /**
     * Create a shop item
     */
    private ItemStack createShopItem(Material material, String itemKey, double buyPrice, double sellPrice, String stock) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(itemKey)
            .color(NamedTextColor.WHITE)
            .decorate(TextDecoration.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("Cena kupna: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(plugin.getEconomyManager().formatMoney(buyPrice))
                .color(NamedTextColor.GREEN)));
        lore.add(Component.text("Cena sprzedaży: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(plugin.getEconomyManager().formatMoney(sellPrice))
                .color(NamedTextColor.GREEN)));

        // Calculate price change percentage
        double priceChangePercent = pricingManager.getPriceChangePercent(itemKey);
        NamedTextColor changeColor = priceChangePercent >= 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
        lore.add(Component.text("Zmiana dynamiczna: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(String.format("%+.1f%%", priceChangePercent))
                .color(changeColor)));

        lore.add(Component.text(""));
        if (stock.equalsIgnoreCase("infinite")) {
            lore.add(Component.text("Stock: §a∞"));
        } else {
            lore.add(Component.text("Stock: §a" + stock));
        }

        lore.add(Component.text(""));
        lore.add(Component.text("§fLPM§7 - Kup 1    §fPPM§7 - Sprzedaj 1").color(NamedTextColor.GRAY));
        lore.add(Component.text("§fSHIFT+LPM§7 - Kup stack    §fSHIFT+PPM§7 - Sprzedaj stack").color(NamedTextColor.GRAY));
        lore.add(Component.text("§fŚrodkowy klik§7 - Wybierz ilość").color(NamedTextColor.GRAY));

        meta.lore(lore);

        // Store item data
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, ITEM_TYPE_KEY),
            PersistentDataType.STRING,
            itemKey
        );

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create a generic button
     */
    private ItemStack createButton(Material material, String name, String... description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name));

        List<Component> lore = new ArrayList<>();
        for (String line : description) {
            lore.add(Component.text(line).color(NamedTextColor.GRAY));
        }
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get item type from clicked item
     */
    public static String getItemType(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(
            new org.bukkit.NamespacedKey(ShopPlugin.getInstance(), ITEM_TYPE_KEY),
            PersistentDataType.STRING
        );
    }

    /**
     * Get category from clicked item
     */
    public static String getCategory(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(
            new org.bukkit.NamespacedKey(ShopPlugin.getInstance(), "shop:category"),
            PersistentDataType.STRING
        );
    }

    /**
     * Get action from clicked item
     */
    public static String getAction(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(
            new org.bukkit.NamespacedKey(ShopPlugin.getInstance(), ACTION_KEY),
            PersistentDataType.STRING
        );
    }
}
