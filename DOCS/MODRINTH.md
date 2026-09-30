# ⚡ UptifyBridge

> **The Official Minecraft Integration for [Uptify.site](https://uptify.site)** — Connect your server network with live status pages, in-game suggestion boards, and zero-click account linking!

![Uptify Banner](https://uptify.site/icon.jpg)

[![Supports Paper](https://img.shields.io/badge/Paper-1.16%20--%201.21.x-brightgreen.svg)](https://papermc.io)
[![Folia Ready](https://img.shields.io/badge/Folia-Supported-blueviolet.svg)](https://papermc.io/software/folia)
[![Java](https://img.shields.io/badge/Java-17%20%7C%2021+-orange.svg)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

---

## 🌟 Key Features

### 🔗 1. Zero-Click Account Linking (`/link`)
- Generates a secure, 15-minute one-time link token.
- Clickable URL in chat leading directly to `https://uptify.site/mc/link?token=...`.
- **0-Click Instant Link**: If the player is already logged into Uptify, the account links automatically in the background with zero buttons to click!
- If unauthenticated, displays the player's Minecraft avatar head and prompts login or registration, then seamlessly links.
- In-game background poller immediately celebrates verification in chat!

### 💡 2. In-Game Feedback & Suggestions (`/suggest <idea>`)
- Let your community post ideas and suggestions straight from the Minecraft chat!
- Saves to your server's **Uptify Feedback Board**.
- Automatically broadcasts to your staff Discord channel via rich Discord Webhook cards.
- **Anti-Spam Verification**: Only verified, linked players can submit suggestions.

### 📊 3. Live Server Network Status (`/status`)
- Interactive in-game status card showing:
  - Overall status: `● All Systems Operational` / `● Degraded` / `● Outage`
  - Node latency: Proxy, Survival, Bedrock Gateway, etc. (in ms)
  - Active incidents and maintenance notices.
  - Clickable web link to your public status page on Uptify.

### 🧩 4. PlaceholderAPI Integration (Optional)
Hook status and project stats directly into your Scoreboard, TAB, or Chat:
- `%uptify_status%` - Returns `OPERATIONAL`, `DEGRADED`, or `OUTAGE`.
- `%uptify_status_colored%` - Returns `§aOperational`, `§eDegraded`, or `§cOutage`.
- `%uptify_status_project%` - Returns your configured status project ID.
- `%uptify_feedback_project%` - Returns your configured feedback project ID.
- `%uptify_version%` - Returns plugin version.

### 🛡️ 5. Zero NMS & Folia Native
- Built exclusively with Bukkit / Paper / BungeeCord Component APIs and standard Java 11+ `HttpClient`.
- **Lifelong Compatibility**: Never breaks across Minecraft minor updates (1.16, 1.17, 1.18, 1.19, 1.20, 1.21.x+).
- Fully non-blocking and safe on multi-threaded **Folia** servers.

---

## ⚙️ Quick Installation

1. Drop `UptifyBridge.jar` into your server's `plugins/` directory.
2. Start or restart your server.
3. Open `plugins/UptifyBridge/config.yml` and add your project UUIDs from your Uptify dashboard:
   ```yaml
   # Status Project (for /status command)
   status-project-id: "YOUR_STATUS_PROJECT_ID"

   # Feedback Project (for /suggest and /feedback commands)
   feedback-project-id: "YOUR_FEEDBACK_PROJECT_ID"
   ```
4. Run `/uptify reload` or test your setup with `/uptify test`!

---

## 🎮 Commands & Permissions

| Command | Aliases | Description | Permission | Default |
| :--- | :--- | :--- | :--- | :--- |
| `/link` | `/uptifylink` | Generates a clickable link to verify your Uptify account | `uptify.use` | Everyone |
| `/suggest <idea>` | `/feedback`, `/idea` | Submits feedback to your server's Uptify board | `uptify.use` | Everyone |
| `/status` | `/uptifystatus` | Displays live node latency and incident alerts | `uptify.use` | Everyone |
| `/uptify test` | — | Tests cloud API connection and measures latency ping | `uptify.admin` | OP |
| `/uptify reload` | — | Reloads config.yml | `uptify.admin` | OP |
| `/uptify help` | — | Shows help guide | `uptify.admin` | OP |
| `/uptify version`| — | Displays version and engine info | `uptify.admin` | OP |

---

## 🌐 Links
- **Platform**: [https://uptify.site](https://uptify.site)
- **Source Code**: [https://github.com/ShaikhZaid404/UptifyBridge](https://github.com/ShaikhZaid404/UptifyBridge)
- **Issues & Support**: [https://github.com/ShaikhZaid404/UptifyBridge/issues](https://github.com/ShaikhZaid404/UptifyBridge/issues)
