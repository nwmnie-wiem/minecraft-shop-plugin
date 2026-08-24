package com.minecraft.shop.commands;

import com.minecraft.shop.ShopPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ShopAdminCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopAdminCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("shop.admin")) {
            sender.sendMessage("§cYou don't have permission to use admin commands!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§a[Shop Admin] Available commands:");
            sender.sendMessage("§e/shopadmin reload§7 - Reload configuration");
            sender.sendMessage("§e/shopadmin prices§7 - Manage prices");
            sender.sendMessage("§e/shopadmin transactions§7 - View transactions");
            sender.sendMessage("§e/shopadmin setprice <item> <buy/sell> <price>§7 - Set item price");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "reload":
                plugin.getConfigManager().reloadConfigs();
                sender.sendMessage("§a[Shop] Configuration reloaded!");
                return true;
            case "prices":
                // TODO: Prices management
                sender.sendMessage("§a[Shop] Prices management - TBD");
                return true;
            case "transactions":
                // TODO: Transactions history
                sender.sendMessage("§a[Shop] Transactions - TBD");
                return true;
            case "setprice":
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /shopadmin setprice <item> <buy/sell> <price>");
                    return true;
                }
                // TODO: Set price functionality
                sender.sendMessage("§a[Shop] Price set - TBD");
                return true;
            default:
                sender.sendMessage("§cUnknown subcommand!");
                return true;
        }
    }
}
