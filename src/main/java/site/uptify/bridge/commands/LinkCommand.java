package site.uptify.bridge.commands;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import site.uptify.bridge.UptifyBridge;
import site.uptify.bridge.api.ApiClient;
import site.uptify.bridge.util.ChatUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkCommand implements CommandExecutor, TabCompleter {

    private final UptifyBridge plugin;
    private final Map<UUID, BukkitTask> activePollTasks = new ConcurrentHashMap<>();

    public LinkCommand(UptifyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by in-game players.");
            return true;
        }

        Player player = (Player) sender;
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&aUptify&8]&r ");
        String generatingMsg = plugin.getConfig().getString("messages.link-generating", "&7Generating your secure account link session...");

        ChatUtils.sendMessage(player, prefix + generatingMsg);

        // Call backend async
        plugin.getApiClient().createLinkSession(player.getUniqueId(), player.getName())
                .thenAccept(response -> {
                    if (!response.isSuccess() || !response.getData().has("linkUrl")) {
                        String errorMsg = plugin.getConfig().getString("messages.link-error", "&cFailed to initiate link session. Please contact an administrator.");
                        ChatUtils.sendMessage(player, prefix + errorMsg);
                        return;
                    }

                    JsonObject data = response.getData();
                    String linkUrl = data.get("linkUrl").getAsString();
                    String token = data.get("token").getAsString();

                    String header = plugin.getConfig().getString("messages.link-header", "&8------------------ [&a Uptify Link &8] ------------------");
                    String instructions = plugin.getConfig().getString("messages.link-instructions", "&eClick the button below to link your Minecraft profile to Uptify:");
                    String buttonText = plugin.getConfig().getString("messages.link-button", "&a&l[CLICK HERE TO LINK ACCOUNT]");
                    String hoverText = plugin.getConfig().getString("messages.link-hover", "&7Opens &nhttps://uptify.site/mc/link&7 in your browser\n&8(Token expires in 15 minutes)");
                    String footer = plugin.getConfig().getString("messages.link-footer", "&8----------------------------------------------------");

                    ChatUtils.sendMessage(player, header);
                    ChatUtils.sendMessage(player, instructions);

                    // Send clickable text component
                    TextComponent clickableBtn = ChatUtils.createClickableUrl("  " + buttonText, linkUrl, hoverText);
                    player.spigot().sendMessage(clickableBtn);

                    ChatUtils.sendMessage(player, footer);

                    // Start background polling if enabled
                    if (plugin.getConfig().getBoolean("enable-link-polling", true)) {
                        startLinkPolling(player, token);
                    }
                })
                .exceptionally(ex -> {
                    String errorMsg = plugin.getConfig().getString("messages.link-error", "&cFailed to initiate link session. Please contact an administrator.");
                    ChatUtils.sendMessage(player, prefix + errorMsg);
                    plugin.getLogger().warning("Error generating link session: " + ex.getMessage());
                    return null;
                });

        return true;
    }

    private void startLinkPolling(Player player, String token) {
        UUID uuid = player.getUniqueId();

        // Cancel previous polling task for this player if any
        BukkitTask existing = activePollTasks.remove(uuid);
        if (existing != null) {
            existing.cancel();
        }

        int intervalSeconds = Math.max(2, plugin.getConfig().getInt("polling-interval-seconds", 3));
        int maxSeconds = Math.max(30, plugin.getConfig().getInt("polling-timeout-seconds", 180));
        long intervalTicks = intervalSeconds * 20L;
        AtomicInteger elapsedSeconds = new AtomicInteger(0);

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                cancelPolling(uuid);
                return;
            }

            if (elapsedSeconds.addAndGet(intervalSeconds) >= maxSeconds) {
                cancelPolling(uuid);
                String prefix = plugin.getConfig().getString("messages.prefix", "&8[&aUptify&8]&r ");
                String timeoutMsg = plugin.getConfig().getString("messages.link-timeout", "&cLink session timed out. Type &e/link&c to generate a new one.");
                ChatUtils.sendMessage(p, prefix + timeoutMsg);
                return;
            }

            plugin.getApiClient().checkLinkStatus(token)
                    .thenAccept(res -> {
                        if (!res.isSuccess()) return;

                        JsonObject obj = res.getData();
                        String status = obj.has("status") ? obj.get("status").getAsString() : "";

                        if ("LINKED".equalsIgnoreCase(status)) {
                            cancelPolling(uuid);

                            String username = "your account";
                            if (obj.has("linkedUser") && !obj.get("linkedUser").isJsonNull()) {
                                JsonObject u = obj.getAsJsonObject("linkedUser");
                                if (u.has("username")) {
                                    username = u.get("username").getAsString();
                                }
                            }

                            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&aUptify&8]&r ");
                            String successMsg = plugin.getConfig().getString("messages.link-success", "&a✔ Successfully linked to Uptify account &e@{username}&a!")
                                    .replace("{username}", username);

                            ChatUtils.sendMessage(p, prefix + successMsg);
                        }
                    })
                    .exceptionally(t -> null);

        }, intervalTicks, intervalTicks);

        activePollTasks.put(uuid, task);
    }

    private void cancelPolling(UUID uuid) {
        BukkitTask task = activePollTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public void cancelAllPollers() {
        for (BukkitTask task : activePollTasks.values()) {
            task.cancel();
        }
        activePollTasks.clear();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
