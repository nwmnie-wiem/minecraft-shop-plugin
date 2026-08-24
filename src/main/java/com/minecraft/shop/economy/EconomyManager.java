package com.minecraft.shop.economy;

import com.minecraft.shop.items.CurrencyItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final Plugin plugin;
    private Economy economy;

    public EconomyManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin not found!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No economy plugin found!");
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean hasBalance(Player player, double amount) {
        return economy.has(player, amount);
    }

    public void withdrawMoney(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
        
        // Give player currency paper
        ItemStack currencyPaper = CurrencyItem.createCurrencyPaper(amount);
        player.getInventory().addItem(currencyPaper);
    }

    public void depositMoney(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public String formatMoney(double amount) {
        return economy.format(amount);
    }

    public String getCurrencySymbol() {
        return "$";
    }
}
