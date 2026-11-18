<div align="center">
  <img src="app/src/main/res/drawable/app_icon.png" width="120" alt="Kryptos Browser Logo">
  
  <h1>Kryptos Browser</h1>
  <p><b>Next-Gen Privacy. Dark Sci-Fi UI. Unmatched Speed.</b></p>

  <p>
    <a href="https://github.com/RishabhIsGreat77/Kryptos-Browser/releases/latest">
      <img src="https://img.shields.io/badge/Version-5.0.0-00E676?style=for-the-badge&logo=android" alt="Version 5.0.0">
    </a>
    <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 8.0+">
    <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
    <img src="https://img.shields.io/badge/Status-Stable-00E676?style=for-the-badge" alt="Stable">
  </p>

  <br>

  <a href="https://www.buymeacoffee.com/Emberlog" target="_blank">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 50px !important;width: 180px !important;" >
  </a>
</div>

---

## 📖 Overview

**Kryptos** is a high-performance, privacy-centric Android browser engineered for speed and security. Built with a deep matte-black and neon-green dark sci-fi aesthetic, it provides a seamless, feature-rich browsing experience without the bloatware. 

Developed entirely from an Android mobile device using cloud-based CI/CD (GitHub Actions), Kryptos proves that enterprise-grade software can be built anywhere.

---

## 🚀 Key Features (V5.0.0)

### 🔒 Privacy & Security First
*   **Incognito Auto-Kill (Android 12-16 compliant):** Advanced lifecycle management ensures that when the screen turns off, all incognito data is instantly wiped, and the app removes itself from the Recent Apps menu. No traces left behind.
*   **Advanced Ad-Blocker:** Built-in integration of 500+ blacklisted domains to block trackers, analytics, and malicious ads.
*   **Link Defanger:** Automatically strips tracking parameters (e.g., `utm_source`, `fbclid`) from URLs to prevent cross-site tracking.
*   **Local Isolated Data:** Complete separation of Normal and Incognito databases.

### ⚡ Performance & Intelligence
*   **Smart Speed Dial Algorithm:** A proprietary ranking system that calculates a website's relevance based on Visit Frequency (35%), Recency (25%), Duration (15%), Regularity (15%), and Engagement (10%). Only the top 8 highest-scoring sites are displayed.
*   **In-Built Download Manager:** Intercepts WebView downloads (including GitHub release assets and APKs) and manages them internally with cookie-aware requests.
*   **Auto Cache Cleaner:** Background threads monitor and automatically clear cache files exceeding 100MB to optimize storage.

### 🎨 User Experience & UI
*   **Dark Sci-Fi Interface:** True dark mode (#0B0B0D) with Neon Green (#00E676) accents and custom glowing UI elements.
*   **Auto-Hiding Dock:** A floating bottom navigation bar that smartly hides on scroll or webpage load, replaced by a minimal swipe-up pill bar.
*   **Reader Mode:** Advanced CSS/JS injection that strips away ads, sidebars, and clutter, providing a clean, readable format for articles and Wikipedia.
*   **Custom Voice Search:** Integrated SpeechRecognizer with a dedicated, animated dark-themed UI dialog.
*   **Search Suggestions:** Real-time Google suggestions with a smart "Paste" arrow, displaying 4 items when the keyboard is open and 8 when closed.

### ⚙️ System Integrations
*   **Set as Default Browser:** Fully compliant with Android RoleManager for handling `http`, `https`, and `tel`/`mailto` intents.
*   **Seamless In-Place Updates:** Custom Auto-Updater that utilizes GitHub Releases, ensuring app updates overwrite the existing installation without losing bookmarks or cookies.
*   **Export/Import Bookmarks:** Easily backup and restore bookmarks via JSON files using the native Android File Picker.

---

## 🏗️ Architecture & Tech Stack

Kryptos is built with a focus on native performance and minimal dependencies.

| Component | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Native Android XML (ConstraintLayout, Material Design) |
| **Local Storage** | SQLite (`SQLiteOpenHelper`) for Smart Dials, History, & Bookmarks |
| **Preferences** | SharedPreferences |
| **Cloud Build** | GitHub Actions (JDK 17, Gradle 8.7) |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 (Android 14) |

---

## 🛠️ Building the Project

Kryptos utilizes a fully cloud-based CI/CD pipeline. **No local PC or Android Studio is required.**

1. **Fork/Clone** the repository.
2. Make your desired code changes directly via the GitHub Web UI.
3. Commit your changes to the `main` branch.
4. Navigate to the **Actions** tab.
5. The `Build Debug APK` workflow will automatically compile the code.
6. Download the `.apk` artifact once the build succeeds.

*Note: For Release builds, ensure the `kryptos.jks.p12` keystore is present and GitHub Secrets (`KEYSTORE_PASS`, `KEY_ALIAS`, `KEY_PASS`) are configured.*

---

## 🤝 Contributing

As an open-source project, contributions are welcome. If you find a bug or have a feature request:
1. Check the [Issues](https://github.com/Emberlog/Kryptos-Browser/issues) tab to see if it's already reported.
2. If not, open a new issue with a detailed description of the bug and steps to reproduce.
3. For code contributions, please open a Pull Request with a clear explanation of the changes.

---

## 💖 Support the Developer

If you find Kryptos useful and want to support its ongoing development, consider buying me a coffee! Your support keeps this project alive and ad-free.

<a href="https://www.buymeacoffee.com/Emberlog" target="_blank">
  <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 40px !important;width: 145px !important;" >
</a>

---

<div align="center">
  <p>Developed with ❤️ by <b>Rishabh</b>.</p>
  <p><i>Built with passion, purely from an Android Phone.</i> 📱</p>
</div>
