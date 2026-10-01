package site.uptify.bridge.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import site.uptify.bridge.UptifyBridge;
import site.uptify.bridge.util.ChatUtils;

import java.util.Collections;
import java.util.List;

public class StatusCommand implements CommandExecutor, TabCompleter {

    private final UptifyBridge plugin;

    public StatusCommand(UptifyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&aUptify&8]&r ");
        String projectId = plugin.getConfig().getString("status-project-id", "").trim();

        if (projectId.isEmpty() || projectId.equalsIgnoreCase("YOUR_STATUS_PROJECT_ID_HERE")) {
            String notConfigured = plugin.getConfig().getString("messages.status-not-configured", 
                    "&cStatus monitoring is not configured. Please set 'status-project-id' in config.yml.");
            ChatUtils.sendMessage(sender, prefix + notConfigured);
            return true;
        }

        String loadingMsg = plugin.getConfig().getString("messages.status-loading", "&7Fetching live server status from Uptify...");
        ChatUtils.sendMessage(sender, prefix + loadingMsg);

        plugin.getApiClient().getStatus(projectId)
                .thenAccept(response -> {
                    if (!response.isSuccess()) {
                        ChatUtils.sendMessage(sender, prefix + "&cUnable to retrieve network status. Please try again later.");
                        return;
                    }

                    JsonObject data = response.getData();
                    String overall = data.has("overallStatus") ? data.get("overallStatus").getAsString().toUpperCase() : "OPERATIONAL";

                    String header = plugin.getConfig().getString("messages.status-header", "&8============= [&a Live Network Status &8] =============");
                    String footer = plugin.getConfig().getString("messages.status-footer", "&8====================================================");

                    ChatUtils.sendMessage(sender, header);

                    // Overall status badge
                    String overallBadge;
                    if ("OUTAGE".equals(overall) || "DOWN".equals(overall)) {
                        overallBadge = plugin.getConfig().getString("messages.status-overall-outage", "&c● Major System Outage");
                    } else if ("DEGRADED".equals(overall)) {
                        overallBadge = plugin.getConfig().getString("messages.status-overall-degraded", "&e● Minor Service Degradation");
                    } else {
                        overallBadge = plugin.getConfig().getString("messages.status-overall-operational", "&a● All Systems Operational");
                    }
                    ChatUtils.sendMessage(sender, "  " + overallBadge);

                    // Monitors list
                    if (data.has("monitors") && data.get("monitors").isJsonArray()) {
                        JsonArray monitors = data.getAsJsonArray("monitors");
                        if (monitors.size() > 0) {
                            ChatUtils.sendMessage(sender, "&7Monitors &amp; Services:".replace("&amp;", "&"));
                            for (JsonElement el : monitors) {
                                if (!el.isJsonObject()) continue;
                                JsonObject m = el.getAsJsonObject();

                                String name = m.has("name") ? m.get("name").getAsString() : "Node";
                                String status = m.has("status") ? m.get("status").getAsString().toUpperCase() : "UP";
                                int latency = m.has("latency") && !m.get("latency").isJsonNull() ? m.get("latency").getAsInt() : 0;

                                String statusColor = "&a";
                                String statusText = "Online";

                                if ("DOWN".equals(status)) {
                                    statusColor = "&c";
                                    statusText = "Offline";
                                } else if ("DEGRADED".equals(status)) {
                                    statusColor = "&e";
                                    statusText = "Slow";
                                }

                                String entryTemplate = plugin.getConfig().getString("messages.status-monitor-entry", 
                                        "&8• &f{name}: {status_color}● {status_text} &8(&7{latency}ms&8)")
                                        .replace("{name}", name)
                                        .replace("{status_color}", statusColor)
                                        .replace("{status_text}", statusText)
                                        .replace("{latency}", String.valueOf(latency));

                                ChatUtils.sendMessage(sender, "  " + entryTemplate);
                            }
                        }
                    }

                    // Active Incidents
                    if (data.has("incidents") && data.get("incidents").isJsonArray()) {
                        JsonArray incidents = data.getAsJsonArray("incidents");
                        for (JsonElement el : incidents) {
                            if (!el.isJsonObject()) continue;
                            JsonObject inc = el.getAsJsonObject();
                            String title = inc.has("title") ? inc.get("title").getAsString() : "Incident";
                            String severity = inc.has("severity") ? inc.get("severity").getAsString() : "MAJOR";

                            String incTemplate = plugin.getConfig().getString("messages.status-incident-alert", 
                                    "&e⚠ Active Incident: &f{title} &8(&e{severity}&8)")
                                    .replace("{title}", title)
                                    .replace("{severity}", severity);

                            ChatUtils.sendMessage(sender, "  " + incTemplate);
                        }
                    }

                    // Clickable web link if player
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        TextComponent webBtn = ChatUtils.createClickableUrl("  &b&nView full status page at uptify.site", 
                                "https://uptify.site/p/" + projectId, "&7Click to open full status breakdown in browser");
                        player.spigot().sendMessage(webBtn);
                    }

                    ChatUtils.sendMessage(sender, footer);
                })
                .exceptionally(ex -> {
                    ChatUtils.sendMessage(sender, prefix + "&cFailed to retrieve server status from Uptify.");
                    plugin.getLogger().warning("Error fetching status: " + ex.getMessage());
                    return null;
                });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
