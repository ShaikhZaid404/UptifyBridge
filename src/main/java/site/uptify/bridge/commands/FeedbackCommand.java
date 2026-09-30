package site.uptify.bridge.commands;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import site.uptify.bridge.UptifyBridge;
import site.uptify.bridge.api.ApiClient;
import site.uptify.bridge.util.ChatUtils;

import java.util.Collections;
import java.util.List;

public class FeedbackCommand implements CommandExecutor, TabCompleter {

    private final UptifyBridge plugin;

    public FeedbackCommand(UptifyBridge plugin) {
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

        if (args.length == 0) {
            String emptyUsage = plugin.getConfig().getString("messages.feedback-empty", "&cUsage: /suggest <your idea or suggestion>");
            ChatUtils.sendMessage(player, prefix + emptyUsage);
            return true;
        }

        String projectId = plugin.getConfig().getString("feedback-project-id", "").trim();
        if (projectId.isEmpty() || projectId.equalsIgnoreCase("YOUR_FEEDBACK_PROJECT_ID_HERE")) {
            ChatUtils.sendMessage(player, prefix + "&cFeedback system is not configured yet. Please set 'feedback-project-id' in config.yml.");
            return true;
        }

        String content = String.join(" ", args).trim();
        if (content.length() < 3) {
            ChatUtils.sendMessage(player, prefix + "&cSuggestion content is too short (minimum 3 characters).");
            return true;
        }

        String submittingMsg = plugin.getConfig().getString("messages.feedback-submitting", "&7Submitting your suggestion to Uptify...");
        ChatUtils.sendMessage(player, prefix + submittingMsg);

        plugin.getApiClient().submitFeedback(player.getUniqueId(), player.getName(), projectId, content)
                .thenAccept(response -> {
                    JsonObject data = response.getData();

                    // Check if player is not linked
                    if (response.getStatusCode() == 403 || (data.has("notLinked") && data.get("notLinked").getAsBoolean())) {
                        String notLinkedMsg = plugin.getConfig().getString("messages.feedback-not-linked", 
                                "&cYou must link your Uptify account before submitting suggestions!\n&7Click below or type &a/link &7to link your account.");
                        ChatUtils.sendMessage(player, prefix + notLinkedMsg);

                        TextComponent linkBtn = ChatUtils.createClickableCommand("&a&l[CLICK HERE TO LINK]", "/link", "&7Run /link to connect your account");
                        player.spigot().sendMessage(linkBtn);
                        return;
                    }

                    if (!response.isSuccess()) {
                        String reason = data.has("error") ? data.get("error").getAsString() : "Internal server error";
                        String errorTemplate = plugin.getConfig().getString("messages.feedback-error", "&cFailed to submit suggestion. &7{reason}")
                                .replace("{reason}", reason);
                        ChatUtils.sendMessage(player, prefix + errorTemplate);
                        return;
                    }

                    // Success
                    String idStr = "N/A";
                    if (data.has("feedback") && data.getAsJsonObject("feedback").has("id")) {
                        idStr = String.valueOf(data.getAsJsonObject("feedback").get("id").getAsInt());
                    }

                    String successTemplate = plugin.getConfig().getString("messages.feedback-success", "&a✔ Your suggestion has been posted to our Uptify board! &8(ID: #{id})")
                            .replace("{id}", idStr);
                    ChatUtils.sendMessage(player, prefix + successTemplate);

                    // Optional button to view on web
                    String viewBtnText = plugin.getConfig().getString("messages.feedback-view-button", "&b&l[VIEW ON WEB]");
                    String viewHover = plugin.getConfig().getString("messages.feedback-view-hover", "&7Click to open your suggestion in your browser");
                    String webUrl = "https://uptify.site/feedback";

                    TextComponent viewBtn = ChatUtils.createClickableUrl("  " + viewBtnText, webUrl, viewHover);
                    player.spigot().sendMessage(viewBtn);
                })
                .exceptionally(ex -> {
                    String errorTemplate = plugin.getConfig().getString("messages.feedback-error", "&cFailed to submit suggestion. &7{reason}")
                            .replace("{reason}", "Could not connect to Uptify servers.");
                    ChatUtils.sendMessage(player, prefix + errorTemplate);
                    plugin.getLogger().warning("Error submitting feedback: " + ex.getMessage());
                    return null;
                });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
