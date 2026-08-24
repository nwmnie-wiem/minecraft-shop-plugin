package com.minecraft.shop.trader;

import com.minecraft.shop.ShopPlugin;
import com.minecraft.shop.items.CurrencyItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ZdzisiekTrader {

    private final ShopPlugin plugin;
    private WanderingTrader trader;
    private Location currentLocation;
    private BukkitTask spawnTask;
    private static final int SPAWN_RADIUS = 100;
    private static final int SPAWN_INTERVAL = 300; // 15 minutes
    private static final double SPAWN_CHANCE = 0.15; // 15% chance to spawn each interval
    private static final Map<String, TraderPrices> TRADER_PRICES = new HashMap<>();

    public ZdzisiekTrader(ShopPlugin plugin) {
        this.plugin = plugin;
        initializeTraderPrices();
    }

    /**
     * Initialize Zdzisiek's trading prices (with variance)
     */
    private void initializeTraderPrices() {
        // Food items
        TRADER_PRICES.put("APPLE", new TraderPrices(20, 4, 35, 7));
        TRADER_PRICES.put("BREAD", new TraderPrices(15, 3, 25, 5));
        TRADER_PRICES.put("COOKED_BEEF", new TraderPrices(30, 6, 50, 10));

        // Rare materials
        TRADER_PRICES.put("EMERALD", new TraderPrices(400, 80, 600, 120));
        TRADER_PRICES.put("DIAMOND", new TraderPrices(700, 140, 1000, 200));
        TRADER_PRICES.put("NETHERITE_INGOT", new TraderPrices(1500, 300, 2500, 500));

        // Potions & enchanted items
        TRADER_PRICES.put("POTION", new TraderPrices(100, 20, 200, 40));
        TRADER_PRICES.put("ENCHANTED_GOLDEN_APPLE", new TraderPrices(500, 100, 800, 160));

        // Building blocks
        TRADER_PRICES.put("GLOWSTONE", new TraderPrices(50, 10, 100, 20));
        TRADER_PRICES.put("PURPUR_BLOCK", new TraderPrices(30, 6, 60, 12));
    }

    /**
     * Start the automatic spawn task
     */
    public void startAutoSpawn() {
        spawnTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (trader == null || trader.isDead()) {
                if (Math.random() < SPAWN_CHANCE) {
                    spawnRandomTrader();
                }
            }
        }, SPAWN_INTERVAL, SPAWN_INTERVAL);

        plugin.getLogger().info("✓ Zdzisiek auto-spawn system started");
    }

    /**
     * Stop the automatic spawn task
     */
    public void stopAutoSpawn() {
        if (spawnTask != null) {
            spawnTask.cancel();
        }
    }

    /**
     * Spawn Zdzisiek at a random player's location
     */
    private void spawnRandomTrader() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) {
            return;
        }

        Player randomPlayer = onlinePlayers.get(new Random().nextInt(onlinePlayers.size()));
        Location spawnLoc = randomPlayer.getLocation().clone();
        spawnLoc.add(
                (Math.random() - 0.5) * SPAWN_RADIUS,
                5,
                (Math.random() - 0.5) * SPAWN_RADIUS
        );

        spawnZdzisiek(spawnLoc, randomPlayer);
    }

    /**
     * Spawn Zdzisiek at a specific location
     */
    public void spawnZdzisiek(Location location, Player nearPlayer) {
        if (trader != null && !trader.isDead()) {
            trader.remove();
        }

        currentLocation = location.clone();
        trader = location.getWorld().spawn(location, WanderingTrader.class);

        // Customize trader appearance
        trader.setCustomName("§6§l✦ Zdzisiek ✦");
        trader.setCustomNameVisible(true);
        trader.setNoGravity(false);

        // Generate random prices for this spawn
        generateTraderOffers();

        // Announce to nearby players
        for (Player p : location.getWorld().getPlayers()) {
            if (p.getLocation().distance(location) < 100) {
                p.sendMessage("§6[Zdzisiek] §eZdzisiek pojawił się w pobliżu!");
            }
        }

        plugin.getLogger().info("§6Zdzisiek spawned at: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }

    /**
     * Generate trading offers with price variance
     */
    private void generateTraderOffers() {
        List<MerchantRecipe> recipes = new ArrayList<>();

        for (Map.Entry<String, TraderPrices> entry : TRADER_PRICES.entrySet()) {
            Material material = Material.matchMaterial(entry.getKey());
            if (material == null) continue;

            TraderPrices prices = entry.getValue();

            // Buy offer (player gives paper, gets item)
            double buyPrice = prices.getRandomBuyPrice();
            ItemStack paperCost = CurrencyItem.createCurrencyPaper(buyPrice);
            ItemStack itemReward = new ItemStack(material, 1);

            MerchantRecipe buyRecipe = new MerchantRecipe(itemReward, 0);
            buyRecipe.addIngredient(paperCost);
            buyRecipe.setVillagerExperience(0);
            recipes.add(buyRecipe);

            // Sell offer (player gives item, gets paper)
            double sellPrice = prices.getRandomSellPrice();
            ItemStack itemCost = new ItemStack(material, 1);
            ItemStack paperReward = CurrencyItem.createCurrencyPaper(sellPrice);

            MerchantRecipe sellRecipe = new MerchantRecipe(paperReward, 0);
            sellRecipe.addIngredient(itemCost);
            sellRecipe.setVillagerExperience(0);
            recipes.add(sellRecipe);
        }

        trader.setRecipes(recipes);
    }

    /**
     * Check if a trader is the special Zdzisiek
     */
    public static boolean isZdzisiek(WanderingTrader trader) {
        return trader.getCustomName() != null && trader.getCustomName().contains("Zdzisiek");
    }

    public WanderingTrader getTrader() {
        return trader;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Inner class for storing price ranges
     */
    private static class TraderPrices {
        private final double minBuy;
        private final double maxBuy;
        private final double minSell;
        private final double maxSell;

        TraderPrices(double minBuy, double maxBuy, double minSell, double maxSell) {
            this.minBuy = minBuy;
            this.maxBuy = maxBuy;
            this.minSell = minSell;
            this.maxSell = maxSell;
        }

        double getRandomBuyPrice() {
            return minBuy + (Math.random() * (maxBuy - minBuy));
        }

        double getRandomSellPrice() {
            return minSell + (Math.random() * (maxSell - minSell));
        }
    }
}
