package site.uptify.bridge.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import site.uptify.bridge.UptifyBridge;
import site.uptify.bridge.util.ChatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UptifyAdminCommand implements CommandExecutor, TabCompleter {

    private final UptifyBridge plugin;

    public UptifyAdminCommand(UptifyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&aUptify&8]&r ");

        if (!sender.hasPermission("uptify.admin")) {
            String noPerm = plugin.getConfig().getString("messages.no-permission", "&cYou do not have permission to execute this command.");
            ChatUtils.sendMessage(sender, prefix + noPerm);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            String reloaded = plugin.getConfig().getString("messages.config-reloaded", "&aUptifyBridge configuration reloaded successfully!");
            ChatUtils.sendMessage(sender, prefix + reloaded);
            return true;
        }

        ChatUtils.sendMessage(sender, prefix + "&7UptifyBridge v" + plugin.getDescription().getVersion());
        ChatUtils.sendMessage(sender, prefix + "&7Usage: &f/uptify reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("uptify.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
