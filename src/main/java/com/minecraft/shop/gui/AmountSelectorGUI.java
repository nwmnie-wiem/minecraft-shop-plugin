package com.minecraft.shop.gui;

import com.minecraft.shop.ShopPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class AmountSelectorGUI {

    private final ShopPlugin plugin;

    public AmountSelectorGUI(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Open amount selector GUI
     */
    public void openAmountSelector(Player player, String itemType, String action) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§6Wybierz ilość"));

        // Quick amount buttons
        addAmountButton(inv, 10, 1, itemType, action);
        addAmountButton(inv, 11, 8, itemType, action);
        addAmountButton(inv, 12, 16, itemType, action);
        addAmountButton(inv, 13, 32, itemType, action);
        addAmountButton(inv, 14, 64, itemType, action);

        player.openInventory(inv);
    }

    /**
     * Add amount button to inventory
     */
    private void addAmountButton(Inventory inv, int slot, int amount, String itemType, String action) {
        ItemStack button = new ItemStack(Material.PAPER, Math.min(amount, 64));
        ItemMeta meta = button.getItemMeta();

        meta.displayName(Component.text("§6[" + amount + "x]").color(NamedTextColor.YELLOW));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Kliknij aby wybrać").color(NamedTextColor.GRAY));
        meta.lore(lore);

        button.setItemMeta(meta);
        inv.setItem(slot, button);
    }
}
