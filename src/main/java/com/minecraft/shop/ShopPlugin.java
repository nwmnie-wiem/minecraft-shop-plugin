package com.minecraft.shop;

import com.minecraft.shop.commands.ShopCommand;
import com.minecraft.shop.commands.ShopAdminCommand;
import com.minecraft.shop.config.ConfigManager;
import com.minecraft.shop.database.DatabaseManager;
import com.minecraft.shop.economy.EconomyManager;
import com.minecraft.shop.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private static ShopPlugin instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;

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

        // Register commands
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("shopadmin").setExecutor(new ShopAdminCommand(this));
        getLogger().info("✓ Commands registered");

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
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
}
