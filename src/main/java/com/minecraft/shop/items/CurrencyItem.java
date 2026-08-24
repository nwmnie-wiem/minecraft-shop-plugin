package com.minecraft.shop.items;

import org.bukkit.Material;
import org.bukkit.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;

import com.minecraft.shop.ShopPlugin;

public class CurrencyItem {

    private static final String CURRENCY_KEY = "shop:currency_money";
    private static final String CURRENCY_AMOUNT_KEY = "shop:currency_amount";

    /**
     * Creates a paper currency item with the specified amount
     */
    public static ItemStack createCurrencyPaper(double amount) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();

        if (meta != null) {
            // Set display name with gold color (hard to fake)
            meta.displayName(Component.text("Banknot")
                    .color(NamedTextColor.GOLD)
                    .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));

            // Add lore showing amount
            meta.lore(java.util.Arrays.asList(
                    Component.text("Wartość: " + ShopPlugin.getInstance().getEconomyManager().formatMoney(amount))
                            .color(NamedTextColor.YELLOW),
                    Component.text("Użyj w sklepie aby otrzymać pieniądze")
                            .color(NamedTextColor.GRAY),
                    Component.text("ID: " + System.nanoTime())
                            .color(NamedTextColor.DARK_GRAY)
            ));

            // Add NBT data for anti-fake validation
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(ShopPlugin.getInstance(), CURRENCY_KEY),
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(ShopPlugin.getInstance(), CURRENCY_AMOUNT_KEY),
                    PersistentDataType.DOUBLE,
                    amount
            );

            paper.setItemMeta(meta);
        }

        return paper;
    }

    /**
     * Checks if an item is a valid currency paper
     */
    public static boolean isCurrencyPaper(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(ShopPlugin.getInstance(), CURRENCY_KEY),
                PersistentDataType.BYTE
        );
    }

    /**
     * Gets the amount stored in a currency paper
     */
    public static double getCurrencyAmount(ItemStack item) {
        if (!isCurrencyPaper(item)) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }

        Double amount = meta.getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(ShopPlugin.getInstance(), CURRENCY_AMOUNT_KEY),
                PersistentDataType.DOUBLE
        );

        return amount != null ? amount : 0;
    }
}
