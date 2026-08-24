package com.minecraft.shop.commands;

import com.minecraft.shop.ShopPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("shop.use")) {
            player.sendMessage("§cYou don't have permission to use the shop!");
            return true;
        }

        if (args.length == 0) {
            // Open main shop menu
            player.sendMessage("§a[Shop] Opening shop menu...");
            // TODO: Open GUI menu
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "reload":
                if (player.hasPermission("shop.admin.reload")) {
                    plugin.getConfigManager().reloadConfigs();
                    player.sendMessage("§a[Shop] Configuration reloaded!");
                } else {
                    player.sendMessage("§cYou don't have permission to reload the shop!");
                }
                return true;
            case "search":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /shop search <item>");
                    return true;
                }
                // TODO: Search functionality
                player.sendMessage("§a[Shop] Searching for: " + args[1]);
                return true;
            default:
                player.sendMessage("§cUnknown subcommand!");
                return true;
        }
    }
}
