/**
 * Automated Modrinth Project & Version Publisher for UptifyBridge
 * Uses Modrinth v2 REST API (https://docs.modrinth.com/api-spec/)
 */

const fs = require('fs');
const path = require('path');

const MODRINTH_TOKEN = process.env.MODRINTH_TOKEN;

if (!MODRINTH_TOKEN) {
  console.error("❌ ERROR: MODRINTH_TOKEN environment variable is required!");
  console.log("Usage: MODRINTH_TOKEN=mrp_xxxx node scripts/publish_modrinth.js");
  process.exit(1);
}

const USER_AGENT = "ShaikhZaid404/UptifyBridge/1.0.0 (admin@uptify.site)";
const API_BASE = "https://api.modrinth.com/v2";

const GAME_VERSIONS = [
  "1.16.5", "1.17.1", "1.18.2", "1.19.4", 
  "1.20.1", "1.20.2", "1.20.4", "1.20.6", 
  "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4"
];

const LOADERS = ["paper", "purpur", "spigot", "folia"];

async function main() {
  console.log("🚀 Starting Modrinth automated publishing for UptifyBridge...");

  // 1. Verify User Token
  const userRes = await fetch(`${API_BASE}/user`, {
    headers: {
      "Authorization": MODRINTH_TOKEN,
      "User-Agent": USER_AGENT
    }
  });

  if (!userRes.ok) {
    const errText = await userRes.text();
    console.error(`❌ Authentication failed (${userRes.status}):`, errText);
    process.exit(1);
  }

  const user = await userRes.json();
  console.log(`✔ Authenticated as Modrinth user: @${user.username} (${user.id})`);

  // Read description body from DOCS/MODRINTH.md
  const docsPath = path.join(__dirname, "../DOCS/MODRINTH.md");
  let bodyContent = fs.readFileSync(docsPath, "utf-8");

  // Check if project already exists
  const slug = "uptifybridge";
  const checkProj = await fetch(`${API_BASE}/project/${slug}`, {
    headers: { "User-Agent": USER_AGENT }
  });

  let projectId = slug;
  if (checkProj.ok) {
    const existing = await checkProj.json();
    projectId = existing.id;
    console.log(`ℹ Project already exists on Modrinth (ID: ${projectId}). Proceeding to version upload.`);
  } else {
    console.log(`📦 Creating new project '${slug}' on Modrinth...`);

    const projectData = {
      slug: slug,
      title: "UptifyBridge",
      description: "Official Uptify Minecraft Server Integration: In-Game Feedback, Status Pages & 0-Click Account Linking",
      categories: ["utility", "management"],
      client_side: "unsupported",
      server_side: "required",
      body: bodyContent,
      issues_url: "https://github.com/ShaikhZaid404/UptifyBridge/issues",
      source_url: "https://github.com/ShaikhZaid404/UptifyBridge",
      wiki_url: "https://uptify.site/docs",
      license_id: "mit",
      project_type: "mod", // Modrinth classifies server plugins under "mod" with server loaders
      loaders: LOADERS,
      game_versions: GAME_VERSIONS,
      initial_versions: []
    };

    const formData = new FormData();
    formData.append("data", JSON.stringify(projectData));

    // Optional icon
    const iconPath = "/data/data/com.termux/files/home/uptify/uptify-frontend/src/app/icon.jpg";
    if (fs.existsSync(iconPath)) {
      const iconBuffer = fs.readFileSync(iconPath);
      const iconBlob = new Blob([iconBuffer], { type: "image/jpeg" });
      formData.append("icon", iconBlob, "icon.jpg");
    }

    const createRes = await fetch(`${API_BASE}/project`, {
      method: "POST",
      headers: {
        "Authorization": MODRINTH_TOKEN,
        "User-Agent": USER_AGENT
      },
      body: formData
    });

    if (!createRes.ok) {
      const err = await createRes.text();
      console.error(`❌ Failed to create project (${createRes.status}):`, err);
      process.exit(1);
    }

    const created = await createRes.json();
    projectId = created.id;
    console.log(`🎉 Project successfully created on Modrinth! ID: ${projectId}`);
  }

  // 2. Upload Version v1.0.0
  console.log(`📤 Uploading version v1.0.0...`);

  // Download compiled release jar from GitHub
  const jarUrl = "https://github.com/ShaikhZaid404/UptifyBridge/releases/download/v1.0.0/UptifyBridge-1.0.0.jar";
  console.log(`⬇ Fetching latest JAR from: ${jarUrl}`);
  const jarRes = await fetch(jarUrl);
  if (!jarRes.ok) {
    throw new Error(`Failed to download JAR from GitHub release: ${jarRes.statusText}`);
  }
  const jarArrayBuffer = await jarRes.arrayBuffer();
  const jarBlob = new Blob([jarArrayBuffer], { type: "application/java-archive" });

  const versionData = {
    name: "UptifyBridge 1.0.0 - Official Release",
    version_number: "1.0.0",
    changelog: "### Official Release v1.0.0\n- In-game `/link` with 0-click browser auto-link\n- In-game `/suggest` and `/feedback` with live Discord CV2 Webhook integration\n- In-game `/status` displaying node latency and incident alerts\n- `/uptify test` admin diagnostics command\n- PlaceholderAPI support (`%uptify_status%`, `%uptify_status_colored%`)\n- Cross-version Paper, Purpur, Spigot, and Folia support (1.16 - 1.21.x+)",
    dependencies: [],
    game_versions: GAME_VERSIONS,
    version_type: "release",
    loaders: LOADERS,
    featured: true,
    status: "listed",
    requested_status: "listed",
    project_id: projectId,
    file_parts: ["jarFile"]
  };

  const versionForm = new FormData();
  versionForm.append("data", JSON.stringify(versionData));
  versionForm.append("jarFile", jarBlob, "UptifyBridge-1.0.0.jar");

  const uploadRes = await fetch(`${API_BASE}/version`, {
    method: "POST",
    headers: {
      "Authorization": MODRINTH_TOKEN,
      "User-Agent": USER_AGENT
    },
    body: versionForm
  });

  if (!uploadRes.ok) {
    const err = await uploadRes.text();
    console.error(`❌ Failed to upload version (${uploadRes.status}):`, err);
    process.exit(1);
  }

  const ver = await uploadRes.json();
  console.log(`✅ VERSION v1.0.0 PUBLISHED SUCCESSFULLY!`);
  console.log(`🔗 Project URL: https://modrinth.com/plugin/${slug}`);
  console.log(`🔗 Version URL: https://modrinth.com/plugin/${slug}/version/${ver.id}`);
}

main().catch(err => {
  console.error("Fatal error:", err);
  process.exit(1);
});
