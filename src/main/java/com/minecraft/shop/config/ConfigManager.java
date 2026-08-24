package com.minecraft.shop.config;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {

    private final Plugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration items;
    private FileConfiguration categories;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean loadConfiguration() {
        try {
            // Create plugin folder if not exists
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            // Load or create config.yml
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                saveDefaultConfig("config.yml");
            }
            config = YamlConfiguration.loadConfiguration(configFile);
            plugin.getLogger().info("✓ config.yml loaded");

            // Load or create messages.yml
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            if (!messagesFile.exists()) {
                saveDefaultConfig("messages.yml");
            }
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            plugin.getLogger().info("✓ messages.yml loaded");

            // Load or create items.yml
            File itemsFile = new File(plugin.getDataFolder(), "items.yml");
            if (!itemsFile.exists()) {
                saveDefaultConfig("items.yml");
            }
            items = YamlConfiguration.loadConfiguration(itemsFile);
            plugin.getLogger().info("✓ items.yml loaded");

            // Load or create categories.yml
            File categoriesFile = new File(plugin.getDataFolder(), "categories.yml");
            if (!categoriesFile.exists()) {
                saveDefaultConfig("categories.yml");
            }
            categories = YamlConfiguration.loadConfiguration(categoriesFile);
            plugin.getLogger().info("✓ categories.yml loaded");

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void saveDefaultConfig(String filename) throws IOException {
        InputStream resource = plugin.getResource(filename);
        if (resource != null) {
            Files.copy(resource, new File(plugin.getDataFolder(), filename).toPath());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getItems() {
        return items;
    }

    public FileConfiguration getCategories() {
        return categories;
    }

    public void reloadConfigs() {
        loadConfiguration();
    }
}
