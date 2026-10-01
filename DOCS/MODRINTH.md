# UptifyBridge

UptifyBridge is the official Paper / Spigot / Folia integration plugin for [Uptify](https://uptify.site). It connects your Minecraft server to your Uptify status page and feedback board.

## Features

- **/link**: Generates a one-time verification link for players to bind their Minecraft UUID and username to their Uptify account. If a player is already logged into Uptify in their browser, the link completes instantly.
- **/suggest `<text>`** (alias: `/feedback`): Allows players to submit suggestions directly from the game to your Uptify feedback board. Only linked players can submit suggestions to prevent spam.
- **/status**: Shows live ping/latency of your server nodes and proxies, plus any active maintenance or incident alerts.
- **/uptify test**: Built-in diagnostic command for server administrators to test API latency and verify project configuration.
- **PlaceholderAPI Support**: Optional placeholders for scoreboards and tablists (`%uptify_status%`, `%uptify_status_colored%`, `%uptify_version%`). Cached asynchronously with zero impact on TPS.
- **Zero NMS**: Built entirely on standard Bukkit, Paper, and BungeeCord component APIs. Compatible with Minecraft 1.16 through 1.21.4+ without breaking on game updates.
- **Folia Compatible**: Uses asynchronous standard Java HTTP client for all network calls. Safe on Folia multi-threaded region servers.

---

## Setup & Installation

1. Download `UptifyBridge.jar` and place it into your server's `plugins/` directory.
2. Start or reload your server to generate `plugins/UptifyBridge/config.yml`.
3. Open `plugins/UptifyBridge/config.yml` and paste your project IDs from your Uptify dashboard:
   ```yaml
   api-base-url: "https://uptify.site/api"
   
   # Status Project ID (used by /status)
   status-project-id: "YOUR_STATUS_PROJECT_ID"
   
   # Feedback Project ID (used by /suggest)
   feedback-project-id: "YOUR_FEEDBACK_PROJECT_ID"
   ```
4. Run `/uptify reload` to apply your configuration.
5. (Optional) Run `/uptify test` to verify your server can reach the Uptify API.

---

## Commands & Permissions

| Command | Aliases | Description | Permission | Default |
| :--- | :--- | :--- | :--- | :--- |
| `/link` | `/uptifylink` | Generates a link to verify your Uptify account | `uptify.use` | True |
| `/suggest <idea>` | `/feedback` | Submits feedback to your Uptify board | `uptify.use` | True |
| `/status` | `/uptifystatus` | Shows live node ping and active incidents | `uptify.use` | True |
| `/uptify test` | — | Tests API connection and reports latency | `uptify.admin` | OP |
| `/uptify reload` | — | Reloads config.yml | `uptify.admin` | OP |
| `/uptify help` | — | Displays plugin command reference | `uptify.admin` | OP |
| `/uptify version`| — | Displays plugin and server runtime info | `uptify.admin` | OP |

---

## Requirements

- **Server Software**: Paper, Purpur, Spigot, or Folia
- **Minecraft Version**: 1.16.5 to 1.21.4+
- **Java**: Java 17 or Java 21+
- **Dependencies**: None. (PlaceholderAPI is optional)

---

## Source & Support

- **Website**: https://uptify.site
- **Source Code**: https://github.com/ShaikhZaid404/UptifyBridge
- **Issue Tracker**: https://github.com/ShaikhZaid404/UptifyBridge/issues
- **License**: MIT
