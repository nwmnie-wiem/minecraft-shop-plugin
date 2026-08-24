package com.minecraft.shop.listeners;

import com.minecraft.shop.ShopPlugin;
import com.minecraft.shop.items.CurrencyItem;
import com.minecraft.shop.gui.ShopGUI;
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
    private ShopGUI shopGUI;

    public PlayerListener(ShopPlugin plugin) {
        this.plugin = plugin;
        this.zdzisiekTrader = new ZdzisiekTrader(plugin);
        this.shopGUI = new ShopGUI(plugin, plugin.getPricingManager());
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
     * Handle inventory clicks in shop GUI
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        // Prevent clicking with currency paper
        if (clicked != null && clicked.getType() == Material.PAPER && CurrencyItem.isCurrencyPaper(clicked)) {
            if (event.isShiftClick()) {
                return; // Allow shift-click to move
            }
            event.setCancelled(true);
            return;
        }

        // Check if this is a shop GUI
        String invTitle = event.getView().getTitle();
        if (!invTitle.contains("Sklep")) {
            return;
        }

        event.setCancelled(true);

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Handle category click
        String category = ShopGUI.getCategory(clicked);
        if (category != null) {
            shopGUI.openCategoryItems(player, category);
            return;
        }

        // Handle action click
        String action = ShopGUI.getAction(clicked);
        if (action != null) {
            if (action.equals("back")) {
                shopGUI.openMainMenu(player);
            } else if (action.equals("search")) {
                player.sendMessage("§6[Shop] Funkcja wyszukiwania - wkrótce");
            } else if (action.equals("favorites")) {
                player.sendMessage("§6[Shop] Ulubione - wkrótce");
            }
            return;
        }

        // Handle item click
        String itemType = ShopGUI.getItemType(clicked);
        if (itemType != null) {
            handleItemClick(player, itemType, event);
        }
    }

    /**
     * Handle item click in shop
     */
    private void handleItemClick(Player player, String itemType, InventoryClickEvent event) {
        double buyPrice = plugin.getPricingManager().getCurrentBuyPrice(itemType);
        double sellPrice = plugin.getPricingManager().getCurrentSellPrice(itemType);

        if (event.isLeftClick()) {
            if (event.isShiftClick()) {
                // Buy stack (64)
                plugin.getTransactionManager().buyItem(player, itemType, 64, buyPrice);
                plugin.getPricingManager().applyPriceChange(itemType, false, 64);
            } else {
                // Buy 1
                plugin.getTransactionManager().buyItem(player, itemType, 1, buyPrice);
                plugin.getPricingManager().applyPriceChange(itemType, false, 1);
            }
        } else if (event.isRightClick()) {
            if (event.isShiftClick()) {
                // Sell stack (64)
                plugin.getTransactionManager().sellItem(player, itemType, 64, sellPrice);
                plugin.getPricingManager().applyPriceChange(itemType, true, 64);
            } else {
                // Sell 1
                plugin.getTransactionManager().sellItem(player, itemType, 1, sellPrice);
                plugin.getPricingManager().applyPriceChange(itemType, true, 1);
            }
        }
    }

    /**
     * Handle Zdzisiek's death
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof WanderingTrader) {
            WanderingTrader trader = (WanderingTrader) event.getEntity();
            if (ZdzisiekTrader.isZdzisiek(trader)) {
                event.getDrops().clear();
                trader.getWorld().getPlayers().forEach(p -> {
                    if (p.getLocation().distance(trader.getLocation()) < 100) {
                        p.sendMessage("§c[Zdzisiek] Zdzisiek zniknął w pyle...");
                    }
                });
            }
        }
    }

    public ZdzisiekTrader getZdzisiekTrader() {
        return zdzisiekTrader;
    }

    public ShopGUI getShopGUI() {
        return shopGUI;
    }
}
