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
            player.sendMessage("§c[Shop] Nie masz dostępu do sklepu!");
            return true;
        }

        if (args.length == 0) {
            // Open main shop menu
            plugin.getPlayerListener().getShopGUI().openMainMenu(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "reload":
                if (player.hasPermission("shop.admin.reload")) {
                    plugin.getConfigManager().reloadConfigs();
                    plugin.getPricingManager().loadPrices();
                    player.sendMessage("§a[Shop] Konfiguracja przeładowana!");
                } else {
                    player.sendMessage("§c[Shop] Nie masz uprawnień!");
                }
                return true;
            case "search":
                if (args.length < 2) {
                    player.sendMessage("§cUżycie: /shop search <przedmiot>");
                    return true;
                }
                player.sendMessage("§a[Shop] Szukanie: " + args[1] + " - wkrótce");
                return true;
            default:
                player.sendMessage("§cNieznana komenda! Użyj: /shop");
                return true;
        }
    }
}
