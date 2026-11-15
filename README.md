<div align="center">
  <img src="app/src/main/res/drawable/app_icon.png" width="120" alt="Kryptos Logo">
  <h1>Kryptos Browser</h1>
  <p><b>Next-Gen Privacy. Dark Sci-Fi UI. Unmatched Speed.</b></p>
  
  [![Version](https://img.shields.io/badge/Version-5.0.0-00E676?style=for-the-badge)]()
  [![Android](https://img.shields.io/badge/Android-8.0%2B-141418?style=for-the-badge&logo=android)](https://developer.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-100%25-00E676?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)

  <br><br>

  <!-- Buy Me a Coffee Button -->
  <a href="https://www.buymeacoffee.com/Emberlog"><img src="https://img.buymeacoffee.com/button-api/?text=Buy me a coffee&emoji=☕&slug=Emberlog&button_colour=FFDD00&font_colour=000000&font_family=Cookie&outline_colour=000000&coffee_colour=ffffff" /></a>
  <br>
  <i>If you love Kryptos, consider supporting the development!</i>

</div>

<br>

**Kryptos** is a high-performance, privacy-focused Android browser designed with a deep matte black and neon green dark sci-fi theme. Built entirely from a mobile phone using GitHub Actions, it features advanced ad-blocking, true incognito mode, and a seamless user experience.

---

## 🚀 What's New in V5 (The Ultimate Privacy Update)

V5 is a massive milestone that transforms Kryptos into a fully-fledged, crash-free, and highly secure browsing environment.

*   🛡️ **Incognito Auto-Kill (Android 12-16 Safe):** The ultimate privacy feature. If any Incognito tab is open and you press the power button, the app instantly clears all data and completely removes itself from the Recent Apps menu. No traces left behind.
*   📖 **Reader Mode:** A custom-built JavaScript injection that strips away ads, sidebars, and headers, leaving you with a clean, dark-themed article (perfect for Wikipedia and news sites).
*   📥 **In-Built Download Manager:** Downloads are now handled internally. View, open, and manage your downloaded files directly from the 3-dots menu.
*   📤 **Export/Import Bookmarks:** Backup your bookmarks into a JSON file and restore them anytime, anywhere.
*   🎭 **Ad-Blocker Whitelist:** Bypass the 500+ tracker blocker for specific websites that break due to strict ad-blocking.
*   🔄 **Seamless Auto-Updater:** Fixed the Keystore conflict. Updates now install directly over the old app without deleting your bookmarks, history, or logins.
*   🎨 **UI/UX Fixes:** Added a toggle for Force Dark Mode, fixed the Light Mode icons, and introduced the Auto-Hiding Dock with a swipeable pill bar.

---

## ✨ Core Features

*   **Dark Sci-Fi UI:** Deep matte black (#0B0B0D) and Neon Green (#00E676) accents.
*   **Floating Dock:** Custom glowing fire button, tabs counter, and back/home controls.
*   **Speed Dial:** Automatically fetches your top 5 most visited sites. Long-press any site to remove it.
*   **Search Suggestions:** Real-time search suggestions (4 when the keyboard is open, 8 when closed) with a quick-paste arrow.
*   **Voice Search:** Custom dark-themed voice search dialog with live transcription.
*   **Desktop Site Toggle:** Quickly switch any website to Desktop (PC) mode.
*   **Custom WebView:** Spoofs a Pixel 8 user agent to fix CAPTCHA issues.
*   **Link Defanger:** Automatically strips tracking parameters (like `utm_source` and `fbclid`) from URLs before loading them.
*   **Auto Cache Cleaner:** Background thread automatically clears the cache if it exceeds 100MB.

---

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **UI:** Native Android XML (ConstraintLayout, LinearLayout, GridLayout)
*   **Database:** SQLite (SQLiteOpenHelper)
*   **Preferences:** SharedPreferences
*   **Build System:** GitHub Actions (JDK 17, Gradle 8.7)
*   **Min SDK:** 26 (Android 8.0)
*   **Target SDK:** 34 (Android 14)

---

## 📂 Architecture Overview

*   `MainActivity.kt`: Core WebView, Toolbar, Auto-Hiding Dock, Tracker Interception, Lifecycle management.
*   `DockController.kt`: Handles all animations for the floating dock and pill bar.
*   `TrackerList.kt`: 500+ blacklisted domains for the ad-blocker.
*   `TabManager.kt`: Manages normal and incognito tabs in memory.
*   `SettingsActivity.kt`: Preferences, In-place Auto-Updater, and security toggles.

---

## ⚙️ How to Build

This project runs on a completely cloud-based workflow. No PC is required.

1. Edit code directly via the GitHub Web UI.
2. Commit changes to the `main` branch.
3. GitHub Actions will automatically trigger and generate `Kryptos-Debug.apk`.

---

## 👨‍💻 Developer

**Rishabh**
*Built with passion, purely from an Android Phone.* 📱
