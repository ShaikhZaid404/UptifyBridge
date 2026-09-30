# ⚡ UptifyBridge

The official Minecraft integration plugin for **[Uptify.site](https://uptify.site)** — Connect your Minecraft server with real-time status monitoring, player feedback boards, and instant zero-click account linking.

---

## 🚀 Features

- **🔗 Seamless Account Linking (`/link`)**:
  - Generates a secure, 15-minute one-time link session.
  - Players click the in-game link to visit `https://uptify.site/mc/link?token=...`.
  - **0-Click Instant Link**: If already logged in to Uptify, the account links immediately.
  - Non-registered players are prompted to log in/register, then auto-linked.
  - Auto-polls in-game and notifies the player as soon as verification succeeds.
- **💡 In-Game Feedback & Suggestions (`/suggest <idea>`)**:
  - Lets verified players submit server suggestions directly to your Uptify feedback board.
  - Automatically pushes suggestions to your connected Discord channel via CV2 rich cards.
  - Anti-spam: Only verified/linked players can submit suggestions.
- **📊 Real-time Network Status (`/status`)**:
  - Displays overall server health, individual monitor latencies (proxy, survival, bedrock), and active incidents in chat.
  - Includes clickable web button leading to your public status page.
- **⚡ Dual Project Architecture**:
  - Distinct `status-project-id` and `feedback-project-id` in `config.yml`.
- **🛡️ Cross-Version & Folia Native**:
  - Supports Minecraft **1.16 through the latest 1.21.x+**.
  - **Zero NMS**: Pure Bukkit / Paper / BungeeCord Chat API — will never break on Minecraft minor updates!
  - Fully Folia-compatible with asynchronous non-blocking I/O via Java standard `HttpClient`.

---

## 📦 Installation

1. Download the latest `UptifyBridge.jar` from the **[Releases](https://github.com/ShaikhZaid404/UptifyBridge/releases)** tab.
2. Place `UptifyBridge.jar` into your Minecraft server's `plugins/` directory.
3. Restart or start your server to generate `plugins/UptifyBridge/config.yml`.
4. Open `plugins/UptifyBridge/config.yml` and paste your project IDs:
   ```yaml
   status-project-id: "your-status-project-id"
   feedback-project-id: "your-feedback-project-id"
   ```
5. Run `/uptify reload` in-game or in the console.

---

## 🎮 Commands & Permissions

| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/link` | `/uptifylink` | Generates a clickable link to verify your Uptify account | `uptify.use` (default: true) |
| `/suggest <idea>` | `/feedback`, `/idea` | Submits feedback to your server's Uptify board | `uptify.use` (default: true) |
| `/status` | `/uptifystatus`, `/serverstatus` | Checks live server latency, uptime, and incident alerts | `uptify.use` (default: true) |
| `/uptify reload` | — | Reloads the configuration | `uptify.admin` (default: op) |

---

## 🛠️ Building from Source

This project uses Maven. It is automatically built via GitHub Actions CI/CD on every push.

To compile manually:
```bash
git clone https://github.com/ShaikhZaid404/UptifyBridge.git
cd UptifyBridge
mvn clean package
```
The compiled JAR will be in `target/UptifyBridge-1.0.0.jar`.

---

## 📄 License
MIT License © 2026 Uptify.site
