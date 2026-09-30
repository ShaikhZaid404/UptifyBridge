package site.uptify.bridge.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.uptify.bridge.UptifyBridge;

public class UptifyExpansion extends PlaceholderExpansion {

    private final UptifyBridge plugin;

    public UptifyExpansion(UptifyBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "uptify";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ShaikhZaid404";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String lower = params.toLowerCase();

        switch (lower) {
            case "status":
                return plugin.getCachedStatus();

            case "status_colored":
                String s = plugin.getCachedStatus();
                if ("DEGRADED".equalsIgnoreCase(s)) {
                    return "§eDegraded";
                } else if ("OUTAGE".equalsIgnoreCase(s) || "DOWN".equalsIgnoreCase(s)) {
                    return "§cOutage";
                }
                return "§aOperational";

            case "status_project":
                return plugin.getConfig().getString("status-project-id", "None");

            case "feedback_project":
                return plugin.getConfig().getString("feedback-project-id", "None");

            case "api":
                return plugin.getConfig().getString("api-base-url", "https://uptify.site/api");

            case "version":
                return plugin.getDescription().getVersion();

            default:
                return null;
        }
    }
}
