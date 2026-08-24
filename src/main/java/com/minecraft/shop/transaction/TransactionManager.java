package com.minecraft.shop.transaction;

import com.minecraft.shop.ShopPlugin;
import com.minecraft.shop.items.CurrencyItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

public class TransactionManager {

    private final ShopPlugin plugin;

    public TransactionManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Execute a buy transaction
     */
    public boolean buyItem(Player player, String itemKey, int amount, double pricePerUnit) {
        double totalPrice = pricePerUnit * amount;

        // Check if player has enough money
        if (!plugin.getEconomyManager().hasBalance(player, totalPrice)) {
            player.sendMessage("§c[Shop] Nie masz wystarczająco pieniędzy! Potrzebujesz " + 
                plugin.getEconomyManager().formatMoney(totalPrice) + 
                ", a masz " + plugin.getEconomyManager().formatMoney(
                plugin.getEconomyManager().getBalance(player)));
            return false;
        }

        // Give items to player
        try {
            Material material = Material.valueOf(itemKey);
            ItemStack item = new ItemStack(material, amount);
            
            // Add to inventory
            if (player.getInventory().firstEmpty() == -1 && amount > player.getInventory().getMaxStackSize()) {
                player.sendMessage("§c[Shop] Twój ekwipunek jest pełny!");
                return false;
            }
            
            player.getInventory().addItem(item);
            
            // Withdraw money
            plugin.getEconomyManager().withdrawMoney(player, totalPrice);
            
            // Log transaction
            logTransaction(player, itemKey, amount, pricePerUnit, totalPrice, "BUY");
            
            player.sendMessage("§a[Shop] Kupiłeś §f" + amount + "x " + itemKey + 
                "§a za §f" + plugin.getEconomyManager().formatMoney(totalPrice));
            
            return true;
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c[Shop] Nieznany typ przedmiotu!");
            return false;
        }
    }

    /**
     * Execute a sell transaction
     */
    public boolean sellItem(Player player, String itemKey, int amount, double pricePerUnit) {
        double totalPrice = pricePerUnit * amount;

        // Check if player has enough items
        try {
            Material material = Material.valueOf(itemKey);
            
            if (!hasEnoughItems(player, material, amount)) {
                player.sendMessage("§c[Shop] Nie masz wystarczająco tego przedmiotu!");
                return false;
            }
            
            // Remove items from inventory
            removeItems(player, material, amount);
            
            // Give money (as currency paper)
            ItemStack currencyPaper = CurrencyItem.createCurrencyPaper(totalPrice);
            player.getInventory().addItem(currencyPaper);
            
            // Log transaction
            logTransaction(player, itemKey, amount, pricePerUnit, totalPrice, "SELL");
            
            player.sendMessage("§a[Shop] Sprzedałeś §f" + amount + "x " + itemKey + 
                "§a za §f" + plugin.getEconomyManager().formatMoney(totalPrice));
            
            return true;
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c[Shop] Nieznany typ przedmiotu!");
            return false;
        }
    }

    /**
     * Check if player has enough items
     */
    private boolean hasEnoughItems(Player player, Material material, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    /**
     * Remove items from player inventory
     */
    private void removeItems(Player player, Material material, int amount) {
        int toRemove = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= toRemove) {
                    toRemove -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                    toRemove = 0;
                }
                if (toRemove == 0) break;
            }
        }
    }

    /**
     * Log transaction to database
     */
    private void logTransaction(Player player, String itemType, int amount, double pricePerUnit, double totalPrice, String type) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO shop_transactions (player_uuid, player_name, item_type, amount, price_per_unit, total_price, transaction_type, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, player.getName());
                stmt.setString(3, itemType);
                stmt.setInt(4, amount);
                stmt.setDouble(5, pricePerUnit);
                stmt.setDouble(6, totalPrice);
                stmt.setString(7, type);
                stmt.setLong(8, System.currentTimeMillis());
                stmt.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to log transaction: " + e.getMessage());
            }
        });
    }
}
