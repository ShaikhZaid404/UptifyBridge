package site.uptify.bridge.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import site.uptify.bridge.UptifyBridge;
import site.uptify.bridge.util.ChatUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            ChatUtils.sendMessage(sender, "&8---------------- [&a UptifyBridge Help &8] ----------------");
            ChatUtils.sendMessage(sender, "&a/link &8- &7Generate a 0-click browser link to verify your Uptify account");
            ChatUtils.sendMessage(sender, "&a/suggest <idea> &8- &7Post player feedback to your server's Uptify board");
            ChatUtils.sendMessage(sender, "&a/status &8- &7Check live node latency, uptime, and incident alerts");
            ChatUtils.sendMessage(sender, "&e/uptify test &8- &7Test cloud API connection & latency ping");
            ChatUtils.sendMessage(sender, "&e/uptify reload &8- &7Reload config.yml settings");
            ChatUtils.sendMessage(sender, "&e/uptify version &8- &7Display plugin and server platform information");
            ChatUtils.sendMessage(sender, "&8--------------------------------------------------------");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.reloadConfig();
            String reloaded = plugin.getConfig().getString("messages.config-reloaded", "&aUptifyBridge configuration reloaded successfully!");
            ChatUtils.sendMessage(sender, prefix + reloaded);
            return true;
        }

        if (sub.equals("test")) {
            String apiUrl = plugin.getConfig().getString("api-base-url", "https://uptify.site/api");
            ChatUtils.sendMessage(sender, prefix + "&7Testing connection to Uptify Cloud API (&f" + apiUrl + "&7)...");

            plugin.getApiClient().testConnection().thenAccept(health -> {
                if (health.isReachable()) {
                    ChatUtils.sendMessage(sender, prefix + "&a✔ Cloud API is reachable! Latency: &e" + health.getLatencyMs() + "ms");

                    String statusId = plugin.getConfig().getString("status-project-id", "").trim();
                    String feedbackId = plugin.getConfig().getString("feedback-project-id", "").trim();

                    boolean statusOk = !statusId.isEmpty() && !statusId.equalsIgnoreCase("YOUR_STATUS_PROJECT_ID_HERE");
                    boolean feedbackOk = !feedbackId.isEmpty() && !feedbackId.equalsIgnoreCase("YOUR_FEEDBACK_PROJECT_ID_HERE");

                    ChatUtils.sendMessage(sender, prefix + "&7Status Project:   " + (statusOk ? "&aConfigured (" + statusId + ")" : "&cNot set in config.yml"));
                    ChatUtils.sendMessage(sender, prefix + "&7Feedback Project: " + (feedbackOk ? "&aConfigured (" + feedbackId + ")" : "&cNot set in config.yml"));
                    ChatUtils.sendMessage(sender, prefix + "&a✔ UptifyBridge is fully operational.");
                } else {
                    ChatUtils.sendMessage(sender, prefix + "&c✖ API check failed with HTTP " + health.getStatusCode() + ". Check network connectivity.");
                }
            }).exceptionally(ex -> {
                ChatUtils.sendMessage(sender, prefix + "&c✖ Could not reach Uptify API: " + ex.getMessage());
                return null;
            });
            return true;
        }

        if (sub.equals("version")) {
            String ver = plugin.getDescription().getVersion();
            String serverName = Bukkit.getName();
            String mcVer = Bukkit.getBukkitVersion();
            String javaVer = System.getProperty("java.version");

            ChatUtils.sendMessage(sender, "&8---------------- [&a UptifyBridge Info &8] ----------------");
            ChatUtils.sendMessage(sender, "&7Plugin Version:  &a" + ver);
            ChatUtils.sendMessage(sender, "&7Server Engine:   &b" + serverName + " (" + mcVer + ")");
            ChatUtils.sendMessage(sender, "&7Java Runtime:    &f" + javaVer);
            ChatUtils.sendMessage(sender, "&7Official Site:   &nhttps://uptify.site");
            ChatUtils.sendMessage(sender, "&8--------------------------------------------------------");
            return true;
        }

        ChatUtils.sendMessage(sender, prefix + "&cUnknown subcommand. Type &e/uptify help &cfor available commands.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("uptify.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "test", "reload", "version");
            List<String> matches = new ArrayList<>();
            for (String sub : subcommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    matches.add(sub);
                }
            }
            return matches;
        }

        return Collections.emptyList();
    }
}
