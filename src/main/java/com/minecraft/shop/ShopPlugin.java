package com.minecraft.shop;

import com.minecraft.shop.commands.ShopCommand;
import com.minecraft.shop.commands.ShopAdminCommand;
import com.minecraft.shop.config.ConfigManager;
import com.minecraft.shop.database.DatabaseManager;
import com.minecraft.shop.economy.EconomyManager;
import com.minecraft.shop.listeners.PlayerListener;
import com.minecraft.shop.pricing.PricingManager;
import com.minecraft.shop.transaction.TransactionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private static ShopPlugin instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private PricingManager pricingManager;
    private TransactionManager transactionManager;
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("======================================");
        getLogger().info("ShopPlugin v1.0.0 is loading...");
        getLogger().info("======================================");

        // Load configuration
        configManager = new ConfigManager(this);
        if (!configManager.loadConfiguration()) {
            getLogger().severe("Failed to load configuration!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("✓ Configuration loaded");

        // Initialize database
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("✓ Database initialized");

        // Initialize economy
        economyManager = new EconomyManager(this);
        if (!economyManager.setupEconomy()) {
            getLogger().warning("⚠ Vault/Economy plugin not found! The shop will not work without an economy plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("✓ Economy manager initialized");

        // Initialize pricing manager
        pricingManager = new PricingManager(this);
        getLogger().info("✓ Pricing manager initialized");

        // Initialize transaction manager
        transactionManager = new TransactionManager(this);
        getLogger().info("✓ Transaction manager initialized");

        // Register commands
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("shopadmin").setExecutor(new ShopAdminCommand(this));
        getLogger().info("✓ Commands registered");

        // Register listeners
        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getLogger().info("✓ Event listeners registered");

        getLogger().info("======================================");
        getLogger().info("ShopPlugin has been successfully enabled!");
        getLogger().info("======================================");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("ShopPlugin has been disabled.");
    }

    public static ShopPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PricingManager getPricingManager() {
        return pricingManager;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }
}
