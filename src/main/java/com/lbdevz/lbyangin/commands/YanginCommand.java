package com.lbdevz.lbyangin.commands;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class YanginCommand implements CommandExecutor, TabCompleter {

    private final LBYangin plugin;

    public YanginCommand(LBYangin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lbyangin.admin")) {
            sender.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Kullanım: /yangin <start|stop|reload|setwarp>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                if (plugin.getEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "Etkinlik zaten aktif!");
                    return true;
                }
                
                // Oyuncu komutu attıysa veya config'de warp tanımlıysa etkinliği başlat
                if (sender instanceof Player player) {
                    plugin.getEventManager().startEvent(player.getLocation());
                } else {
                    plugin.getEventManager().startEvent();
                }
                sender.sendMessage(ChatColor.GREEN + "Yangın etkinliği başlatıldı!");
                break;

            case "stop":
                if (!plugin.getEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "Aktif bir etkinlik yok.");
                    return true;
                }
                plugin.getEventManager().stopEvent();
                sender.sendMessage(ChatColor.YELLOW + "Yangın etkinliği durduruldu.");
                break;

            case "setwarp":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyun içerisindeki oyuncular kullanabilir.");
                    return true;
                }

                Location loc = player.getLocation();
                plugin.getConfig().set("warp-location.world", loc.getWorld().getName());
                plugin.getConfig().set("warp-location.x", loc.getX());
                plugin.getConfig().set("warp-location.y", loc.getY());
                plugin.getConfig().set("warp-location.z", loc.getZ());
                plugin.getConfig().set("warp-location.yaw", (double) loc.getYaw());
                plugin.getConfig().set("warp-location.pitch", (double) loc.getPitch());
                plugin.saveConfig();

                String prefix = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&aEtkinlik doğma konumu durduğunuz yer olarak kaydedildi!"));
                break;

            case "reload":
                plugin.reloadConfig();
                String pfx = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ");
                String reloadedMsg = plugin.getConfig().getString("messages.config-reloaded", "Konfigürasyon yüklendi.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', pfx + reloadedMsg));
                break;

            default:
                sender.sendMessage(ChatColor.YELLOW + "Kullanım: /yangin <start|stop|reload|setwarp>");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subCommands = List.of("start", "stop", "reload", "setwarp");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }
        return completions;
    }
}