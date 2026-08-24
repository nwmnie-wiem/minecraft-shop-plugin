package com.minecraft.shop.listeners;

import com.minecraft.shop.ShopPlugin;
import com.minecraft.shop.items.CurrencyItem;
import com.minecraft.shop.trader.ZdzisiekTrader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final ShopPlugin plugin;
    private ZdzisiekTrader zdzisiekTrader;

    public PlayerListener(ShopPlugin plugin) {
        this.plugin = plugin;
        this.zdzisiekTrader = new ZdzisiekTrader(plugin);
        zdzisiekTrader.startAutoSpawn();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize player data if needed
    }

    /**
     * Handle currency paper usage
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.PAPER) {
            return;
        }

        // Check if it's a valid currency paper
        if (!CurrencyItem.isCurrencyPaper(item)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        double amount = CurrencyItem.getCurrencyAmount(item);

        // Deposit money into player account
        plugin.getEconomyManager().depositMoney(player, amount);

        // Remove the paper from inventory
        item.setAmount(item.getAmount() - 1);

        player.sendMessage("§a[Shop] Wymieniono banknot na " + plugin.getEconomyManager().formatMoney(amount));
    }

    /**
     * Handle Zdzisiek's death
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof WanderingTrader) {
            WanderingTrader trader = (WanderingTrader) event.getEntity();
            if (ZdzisiekTrader.isZdzisiek(trader)) {
                // Remove all drops (optional - keep trader drops)
                event.getDrops().clear();
                trader.getWorld().getPlayers().forEach(p -> {
                    if (p.getLocation().distance(trader.getLocation()) < 100) {
                        p.sendMessage("§c[Zdzisiek] Zdzisiek zniknął w pyle...");
                    }
                });
            }
        }
    }

    /**
     * Prevent items from being removed from currency paper
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (current != null && current.getType() == Material.PAPER && CurrencyItem.isCurrencyPaper(current)) {
            // Prevent certain operations on currency paper
            if (event.isShiftClick()) {
                // Allow shift-click to move currency paper
                return;
            }
        }
    }

    public ZdzisiekTrader getZdzisiekTrader() {
        return zdzisiekTrader;
    }
}
