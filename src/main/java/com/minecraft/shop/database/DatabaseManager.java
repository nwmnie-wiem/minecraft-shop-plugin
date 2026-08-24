package com.minecraft.shop.database;

import org.bukkit.plugin.Plugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final Plugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        try {
            // Setup HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/shop.db");
            config.setConnectionTestQuery("SELECT 1");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);

            dataSource = new HikariDataSource(config);

            // Create tables
            createTables();

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "player_uuid TEXT NOT NULL,"
                    + "player_name TEXT NOT NULL,"
                    + "item_type TEXT NOT NULL,"
                    + "amount INTEGER NOT NULL,"
                    + "price_per_unit DOUBLE NOT NULL,"
                    + "total_price DOUBLE NOT NULL,"
                    + "transaction_type TEXT NOT NULL,"
                    + "timestamp LONG NOT NULL"
                    + ")");

            // Dynamic prices table
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_prices ("
                    + "item_type TEXT PRIMARY KEY,"
                    + "current_buy_price DOUBLE NOT NULL,"
                    + "current_sell_price DOUBLE NOT NULL,"
                    + "base_buy_price DOUBLE NOT NULL,"
                    + "base_sell_price DOUBLE NOT NULL,"
                    + "min_sell_price DOUBLE NOT NULL,"
                    + "max_sell_price DOUBLE NOT NULL,"
                    + "last_updated LONG NOT NULL"
                    + ")");

            // Favorites table
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_favorites ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "player_uuid TEXT NOT NULL,"
                    + "item_type TEXT NOT NULL,"
                    + "UNIQUE(player_uuid, item_type)"
                    + ")");

            plugin.getLogger().info("✓ Database tables created/verified");
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection closed");
        }
    }
}
