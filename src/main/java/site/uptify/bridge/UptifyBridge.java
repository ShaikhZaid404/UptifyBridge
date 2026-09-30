package site.uptify.bridge;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import site.uptify.bridge.api.ApiClient;
import site.uptify.bridge.commands.FeedbackCommand;
import site.uptify.bridge.commands.LinkCommand;
import site.uptify.bridge.commands.StatusCommand;
import site.uptify.bridge.commands.UptifyAdminCommand;

public class UptifyBridge extends JavaPlugin {

    private static UptifyBridge instance;
    private ApiClient apiClient;
    private LinkCommand linkCommand;
    private FeedbackCommand feedbackCommand;
    private StatusCommand statusCommand;
    private UptifyAdminCommand adminCommand;

    public static UptifyBridge getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Save default config.yml if not already present
        saveDefaultConfig();

        // Initialize REST API Client
        this.apiClient = new ApiClient(this);

        // Initialize Command Handlers
        this.linkCommand = new LinkCommand(this);
        this.feedbackCommand = new FeedbackCommand(this);
        this.statusCommand = new StatusCommand(this);
        this.adminCommand = new UptifyAdminCommand(this);

        // Register Commands and Tab Completers
        registerCommand("link", linkCommand);
        registerCommand("suggest", feedbackCommand);
        registerCommand("status", statusCommand);
        registerCommand("uptify", adminCommand);

        getLogger().info("==================================================");
        getLogger().info("  UptifyBridge v" + getDescription().getVersion() + " has been successfully enabled!");
        getLogger().info("  Status Project: " + getConfig().getString("status-project-id", "Not set"));
        getLogger().info("  Feedback Project: " + getConfig().getString("feedback-project-id", "Not set"));
        getLogger().info("  API Endpoint: " + getConfig().getString("api-base-url", "https://uptify.site/api"));
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        if (linkCommand != null) {
            linkCommand.cancelAllPollers();
        }
        getLogger().info("UptifyBridge has been disabled.");
        instance = null;
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            if (executor instanceof org.bukkit.command.CommandExecutor) {
                cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
            }
            if (executor instanceof org.bukkit.command.TabCompleter) {
                cmd.setTabCompleter((org.bukkit.command.TabCompleter) executor);
            }
        } else {
            getLogger().warning("Could not register command /" + name + " (Check plugin.yml)");
        }
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
}
