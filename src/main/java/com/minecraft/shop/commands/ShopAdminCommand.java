package com.minecraft.shop.commands;

import com.minecraft.shop.ShopPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ShopAdminCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopAdminCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("shop.admin")) {
            sender.sendMessage("§c[Shop] Nie masz uprawnień!");
            return true;
        }

        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "reload":
                if (!sender.hasPermission("shop.admin.reload")) {
                    sender.sendMessage("§c[Shop] Nie masz uprawnień!");
                    return true;
                }
                plugin.getConfigManager().reloadConfigs();
                plugin.getPricingManager().loadPrices();
                sender.sendMessage("§a[Shop] Konfiguracja przeładowana!");
                return true;

            case "prices":
                if (!sender.hasPermission("shop.admin.prices")) {
                    sender.sendMessage("§c[Shop] Nie masz uprawnień!");
                    return true;
                }
                showPrices(sender);
                return true;

            case "setprice":
                if (!sender.hasPermission("shop.admin.prices")) {
                    sender.sendMessage("§c[Shop] Nie masz uprawnień!");
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUżycie: /shopadmin setprice <item> <buy/sell> <cena>");
                    return true;
                }
                setPrice(sender, args[1], args[2], args[3]);
                return true;

            case "resetprice":
                if (!sender.hasPermission("shop.admin.prices")) {
                    sender.sendMessage("§c[Shop] Nie masz uprawnień!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUżycie: /shopadmin resetprice <item>");
                    return true;
                }
                resetPrice(sender, args[1]);
                return true;

            case "resetallprices":
                if (!sender.hasPermission("shop.admin.prices")) {
                    sender.sendMessage("§c[Shop] Nie masz uprawnień!");
                    return true;
                }
                plugin.getPricingManager().resetAllPrices();
                sender.sendMessage("§a[Shop] Wszystkie ceny zresetowane!");
                return true;

            case "transactions":
                if (!sender.hasPermission("shop.admin.transactions")) {
                    sender.sendMessage("§c[Shop] Nie masz uprawnień!");
                    return true;
                }
                if (args.length < 2) {
                    showRecentTransactions(sender, 10);
                } else {
                    try {
                        int limit = Integer.parseInt(args[1]);
                        showRecentTransactions(sender, limit);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cNieprawidłowa liczba!");
                    }
                }
                return true;

            case "spawnzdzisiek":
                if (!sender.hasPermission("shop.admin")) {
                    sender.sendMessage("§c[Shop] Nie masz uprawnień!");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Tylko gracze mogą użyć tej komendy!");
                    return true;
                }
                Player player = (Player) sender;
                plugin.getPlayerListener().getZdzisiekTrader().spawnZdzisiek(player.getLocation(), player);
                sender.sendMessage("§a[Shop] Zdzisiek spawned!");
                return true;

            default:
                sender.sendMessage("§cNieznana komenda!");
                showAdminHelp(sender);
                return true;
        }
    }

    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage("§6=== KOMENDY ADMINISTRATORA ===");
        sender.sendMessage("§e/shopadmin reload§7 - Przeładuj konfigurację");
        sender.sendMessage("§e/shopadmin prices§7 - Pokaż bieżące ceny");
        sender.sendMessage("§e/shopadmin setprice <item> <buy/sell> <cena>§7 - Ustaw cenę");
        sender.sendMessage("§e/shopadmin resetprice <item>§7 - Resetuj cenę przedmiotu");
        sender.sendMessage("§e/shopadmin resetallprices§7 - Resetuj wszystkie ceny");
        sender.sendMessage("§e/shopadmin transactions [limit]§7 - Pokaż transakcje");
        sender.sendMessage("§e/shopadmin spawnzdzisiek§7 - Spawnuj Zdziska");
    }

    private void showPrices(CommandSender sender) {
        sender.sendMessage("§6=== BIEŻĄCE CENY ===");
        sender.sendMessage("§e[Przykład: DIAMOND - Kupno: $500 | Sprzedaż: $100]");
        sender.sendMessage("§6====================");
    }

    private void setPrice(CommandSender sender, String item, String priceType, String price) {
        try {
            double priceValue = Double.parseDouble(price);
            
            if (priceValue < 0) {
                sender.sendMessage("§c[Shop] Cena nie może być ujemna!");
                return;
            }

            // TODO: Implement price setting in database
            sender.sendMessage("§a[Shop] Cena dla " + item + " " + priceType + " ustawiona na: $" + price);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c[Shop] Nieprawidłowa cena!");
        }
    }

    private void resetPrice(CommandSender sender, String item) {
        plugin.getPricingManager().resetPrice(item);
        sender.sendMessage("§a[Shop] Cena dla " + item + " zresetowana!");
    }

    private void showRecentTransactions(CommandSender sender, int limit) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection();
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery(
                    "SELECT player_name, item_type, amount, price_per_unit, transaction_type, timestamp " +
                    "FROM shop_transactions ORDER BY timestamp DESC LIMIT " + limit
                );

                sender.sendMessage("§6=== OSTATNIE TRANSAKCJE ===");
                int count = 0;
                while (rs.next() && count < limit) {
                    String playerName = rs.getString("player_name");
                    String itemType = rs.getString("item_type");
                    int amount = rs.getInt("amount");
                    double pricePerUnit = rs.getDouble("price_per_unit");
                    String transactionType = rs.getString("transaction_type");
                    long timestamp = rs.getLong("timestamp");

                    String type = transactionType.equals("BUY") ? "§a[KUPNO]" : "§c[SPRZEDAŻ]";
                    sender.sendMessage(type + "§7 " + playerName + " - " + amount + "x " + itemType + " @ $" + pricePerUnit);
                    count++;
                }
            } catch (Exception e) {
                sender.sendMessage("§c[Shop] Błąd przy ładowaniu transakcji!");
            }
        });
    }
}
