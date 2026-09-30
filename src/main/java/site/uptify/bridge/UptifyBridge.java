package site.uptify.bridge;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import site.uptify.bridge.api.ApiClient;
import site.uptify.bridge.commands.FeedbackCommand;
import site.uptify.bridge.commands.LinkCommand;
import site.uptify.bridge.commands.StatusCommand;
import site.uptify.bridge.commands.UptifyAdminCommand;
import site.uptify.bridge.papi.UptifyExpansion;
import site.uptify.bridge.util.ChatUtils;

public class UptifyBridge extends JavaPlugin {

    private static UptifyBridge instance;
    private ApiClient apiClient;
    private LinkCommand linkCommand;
    private FeedbackCommand feedbackCommand;
    private StatusCommand statusCommand;
    private UptifyAdminCommand adminCommand;

    private String cachedStatus = "OPERATIONAL";
    private BukkitTask statusCacheTask;

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

        // Register PlaceholderAPI expansion if present
        registerPlaceholderAPI();

        // Start background status refresher
        startStatusRefresher();

        // Print stylish startup banner
        printStartupBanner();

        // Asynchronous cloud connectivity check
        performCloudCheck();
    }

    @Override
    public void onDisable() {
        if (linkCommand != null) {
            linkCommand.cancelAllPollers();
        }
        if (statusCacheTask != null) {
            statusCacheTask.cancel();
        }

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(ChatUtils.colorize("&8[&aUptifyBridge&8] &cSafely stopping link pollers and disabling plugin. Goodbye!"));

        instance = null;
    }

    private void printStartupBanner() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String version = getDescription().getVersion();
        String serverSoftware = Bukkit.getName();
        String mcVersion = Bukkit.getBukkitVersion();
        String javaVer = System.getProperty("java.version");
        boolean isFolia = checkFolia();

        console.sendMessage(ChatUtils.colorize("&a  _   _       _   _  __       ____       _     _             "));
        console.sendMessage(ChatUtils.colorize("&a | | | |_ __ | |_(_)/ _|_   _| __ ) _ __(_) __| | __ _  ___ "));
        console.sendMessage(ChatUtils.colorize("&a | | | | '_ \\| __| | |_| | | |  _ \\| '__| |/ _` |/ _` |/ _ \\"));
        console.sendMessage(ChatUtils.colorize("&a | |_| | |_) | |_| |  _| |_| | |_) | |  | | (_| | (_| |  __/"));
        console.sendMessage(ChatUtils.colorize("&2  \\___/| .__/ \\__|_|_|  \\__, |____/|_|  |_|\\__,_|\\__, |\\___|"));
        console.sendMessage(ChatUtils.colorize("&2       |_|              |___/                    |___/      "));
        console.sendMessage(ChatUtils.colorize("&8================================================================="));
        console.sendMessage(ChatUtils.colorize("&a  ⚡ UptifyBridge &fv" + version + " &7- Official Minecraft Cloud Integration"));
        console.sendMessage(ChatUtils.colorize("&7  Server Engine:  &b" + serverSoftware + " (" + mcVersion + ") &8| &7Java: &f" + javaVer));
        console.sendMessage(ChatUtils.colorize("&7  Architecture:   " + (isFolia ? "&dFolia Multi-Threaded Region" : "&eBukkit/Paper Unified Tick")));
        console.sendMessage(ChatUtils.colorize("&7  API Endpoint:   &a" + getConfig().getString("api-base-url", "https://uptify.site/api")));

        String statusProj = getConfig().getString("status-project-id", "").trim();
        String feedbackProj = getConfig().getString("feedback-project-id", "").trim();

        boolean statusConfigured = !statusProj.isEmpty() && !statusProj.equalsIgnoreCase("YOUR_STATUS_PROJECT_ID_HERE");
        boolean feedbackConfigured = !feedbackProj.isEmpty() && !feedbackProj.equalsIgnoreCase("YOUR_FEEDBACK_PROJECT_ID_HERE");

        console.sendMessage(ChatUtils.colorize("&7  Status Project:   " + (statusConfigured ? "&a✔ " + statusProj : "&c✖ Unconfigured (plugins/UptifyBridge/config.yml)")));
        console.sendMessage(ChatUtils.colorize("&7  Feedback Project: " + (feedbackConfigured ? "&a✔ " + feedbackProj : "&c✖ Unconfigured (plugins/UptifyBridge/config.yml)")));
        console.sendMessage(ChatUtils.colorize("&8================================================================="));

        if (!statusConfigured || !feedbackConfigured) {
            console.sendMessage(ChatUtils.colorize("&e[UptifyBridge] ⚠ Setup: Open plugins/UptifyBridge/config.yml and configure your project IDs."));
        }
    }

    private void performCloudCheck() {
        apiClient.testConnection().thenAccept(health -> {
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            if (health.isReachable()) {
                console.sendMessage(ChatUtils.colorize("&8[&aUptifyBridge&8] &a✔ Connected to Uptify Cloud API &8(&f" + health.getLatencyMs() + "ms latency&8)"));
            } else {
                console.sendMessage(ChatUtils.colorize("&8[&aUptifyBridge&8] &e⚠ Warning: Uptify API returned HTTP " + health.getStatusCode() + ". Check network connectivity."));
            }
        }).exceptionally(ex -> {
            Bukkit.getConsoleSender().sendMessage(ChatUtils.colorize("&8[&aUptifyBridge&8] &e⚠ Notice: Uptify API check encountered an issue: " + ex.getMessage()));
            return null;
        });
    }

    private void registerPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new UptifyExpansion(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI expansion (%uptify_status%, %uptify_status_colored%).");
        }
    }

    private void startStatusRefresher() {
        String statusProj = getConfig().getString("status-project-id", "").trim();
        if (statusProj.isEmpty() || statusProj.equalsIgnoreCase("YOUR_STATUS_PROJECT_ID_HERE")) {
            return;
        }

        int interval = Math.max(30, getConfig().getInt("cache-status-interval-seconds", 60));
        long ticks = interval * 20L;

        statusCacheTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            apiClient.getStatus(statusProj).thenAccept(res -> {
                if (res.isSuccess() && res.getData().has("overallStatus")) {
                    cachedStatus = res.getData().get("overallStatus").getAsString().toUpperCase();
                }
            }).exceptionally(t -> null);
        }, 40L, ticks);
    }

    private boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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

    public String getCachedStatus() {
        return cachedStatus;
    }
}
