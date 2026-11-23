package com.kryptos.browser

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.widget.LinearLayout
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private var pendingUpdateFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("kryptos_prefs", Context.MODE_PRIVATE)

        // UI Bindings
        val switchSuggestions = findViewById<Switch>(R.id.switch_suggestions)
        val switchDarkMode = findViewById<Switch>(R.id.switch_dark_mode)
        val switchIncognitoKill = findViewById<Switch>(R.id.switch_incognito_kill)
        val switchForceDark = findViewById<Switch>(R.id.switch_force_dark)
        val switchFullscreen = findViewById<Switch>(R.id.switch_fullscreen)

        val txtClearData = findViewById<LinearLayout>(R.id.txt_clear_data_settings)
        val txtSetDefault = findViewById<LinearLayout>(R.id.txt_set_default_browser)
        val txtCheckUpdates = findViewById<LinearLayout>(R.id.txt_check_updates)
        val txtBuyMeCoffee = findViewById<LinearLayout>(R.id.txt_buy_me_coffee)
        val txtPasswordManager = findViewById<LinearLayout>(R.id.txt_password_manager)

        // Load Saved States
        switchSuggestions.isChecked = prefs.getBoolean("search_suggestions", true)
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", true)
        switchIncognitoKill.isChecked = prefs.getBoolean("incognito_kill", true)
        switchForceDark.isChecked = prefs.getBoolean("force_dark_mode", false)
        switchFullscreen.isChecked = prefs.getBoolean("fullscreen_mode", false)

        // Listeners for Switches
        switchSuggestions.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("search_suggestions", isChecked).apply()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }

        switchIncognitoKill.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("incognito_kill", isChecked).commit()
        }

        switchForceDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("force_dark_mode", isChecked).apply()
        }

        switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("fullscreen_mode", isChecked).apply()
        }

        // Set as Default Browser
        txtSetDefault.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as android.app.role.RoleManager
                if (roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_BROWSER)) {
                    if (roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_BROWSER)) {
                        Toast.makeText(this, "Kryptos is already your default browser!", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_BROWSER)
                        startActivityForResult(intent, 1234)
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                startActivity(intent)
            }
        }

        // Password Manager (Placeholder for V6)
        txtPasswordManager.setOnClickListener {
            Toast.makeText(this, "Password Manager coming in V6!", Toast.LENGTH_SHORT).show()
        }

        // Clear Data Option
        txtClearData.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_custom)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val title = dialog.findViewById<TextView>(R.id.dialog_title)
            val message = dialog.findViewById<TextView>(R.id.dialog_message)
            val btnCancel = dialog.findViewById<TextView>(R.id.btn_dialog_cancel)
            val btnConfirm = dialog.findViewById<TextView>(R.id.btn_dialog_confirm)

            title.text = "Clear Data"
            message.text = "Are you sure you want to clear all browsing data?"
            btnConfirm.text = "Clear"

            btnCancel.setOnClickListener { dialog.dismiss() }
            btnConfirm.setOnClickListener {
                dialog.dismiss()
                
                WebStorage.getInstance().deleteAllData()
                val cookieManager = CookieManager.getInstance()
                cookieManager.removeAllCookies(null)
                cookieManager.flush()
                
                val historyDb = HistoryDatabaseHelper(this)
                historyDb.clearHistory()
                
                val bookmarkDb = BookmarkDatabaseHelper(this)
                bookmarkDb.clearBookmarks()
                
                TabManager.resetAllTabs()
                
                showCustomToast("All browsing data cleared successfully!")
            }
            dialog.show()
        }

        // Check for Updates
        txtCheckUpdates.setOnClickListener {
            showCheckingDialog()
        }

        // Buy Me a Coffee
        txtBuyMeCoffee.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://www.buymeacoffee.com/Emberlog")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val apkToInstall = pendingUpdateFile
        if (apkToInstall != null && apkToInstall.exists()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls()) {
                pendingUpdateFile = null
                installApk(apkToInstall)
            }
        }
    }

    private fun showCheckingDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val message = dialog.findViewById<TextView>(R.id.dialog_message)
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_dialog_cancel)
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_dialog_confirm)

        title.text = "Updates"
        message.text = "Checking for updates..."
        btnCancel.visibility = View.GONE
        btnConfirm.visibility = View.GONE
        dialog.show()

        Thread {
            try {
                val url = URL("https://api.github.com/repos/RishabhIsGreat77/Kryptos-Browser/releases/latest")
                val connection = (url.openConnection() as HttpsURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "KryptosBrowser-Updater")
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(response)
                    val rawTag = json.optString("tag_name", "")
                    val latestVersion = extractVersionNumbers(rawTag)

                    val packageInfo = packageManager.getPackageInfo(packageName, 0)
                    val currentVersion = extractVersionNumbers(packageInfo.versionName ?: "1.0.0")

                    runOnUiThread {
                        dialog.dismiss()
                        if (latestVersion.isNotEmpty() && isNewerVersion(latestVersion, currentVersion)) {
                            val assets = json.optJSONArray("assets")
                            var downloadUrl = ""
                            if (assets != null) {
                                for (i in 0 until assets.length()) {
                                    val assetObj = assets.getJSONObject(i)
                                    if (assetObj.optString("name", "").lowercase().endsWith(".apk")) {
                                        downloadUrl = assetObj.optString("browser_download_url", "")
                                        break
                                    }
                                }
                            }
                            if (downloadUrl.isNotEmpty()) {
                                showUpdateDialog(rawTag.ifEmpty { latestVersion }, downloadUrl)
                            } else {
                                showCustomToast("New release found ($rawTag), but no APK asset is attached.")
                            }
                        } else {
                            showCustomToast("You are on the latest version (v${packageInfo.versionName ?: currentVersion})")
                        }
                    }
                } else {
                    runOnUiThread {
                        dialog.dismiss()
                        showCustomToast("Could not check updates (HTTP $responseCode)")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { 
                    dialog.dismiss()
                    showCustomToast("Failed to check updates: ${e.message}") 
                }
            }
        }.start()
    }

    private fun extractVersionNumbers(raw: String): String {
        val match = Regex("\\d+(\\.\\d+)*").find(raw)
        return match?.value ?: raw.replace("v", "", ignoreCase = true).trim()
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    private fun showUpdateDialog(version: String, downloadUrl: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val message = dialog.findViewById<TextView>(R.id.dialog_message)
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_dialog_cancel)
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_dialog_confirm)

        title.text = "Update Available"
        message.text = "A new version ($version) is available. Updating will overwrite the app while keeping your bookmarks, downloads, and cookies intact."
        btnConfirm.text = "Update"
        btnConfirm.setTextColor(Color.parseColor("#00E676"))

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            downloadAndInstallApk(downloadUrl)
        }
        dialog.show()
    }

    private fun downloadAndInstallApk(apkUrl: String) {
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Downloading update...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        Thread {
            try {
                try { CookieManager.getInstance().flush() } catch (e: Exception) { }

                val targetDir = externalCacheDir ?: cacheDir
                val apkFile = File(targetDir, "kryptos_update.apk")
                if (apkFile.exists()) apkFile.delete()

                var currentUrl = apkUrl
                var redirects = 0
                var connection: HttpURLConnection

                while (true) {
                    connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = true
                        connectTimeout = 20000
                        readTimeout = 30000
                        setRequestProperty("User-Agent", "KryptosBrowser-Updater")
                        setRequestProperty("Accept", "application/octet-stream")
                    }
                    connection.connect()
                    val code = connection.responseCode
                    if (code in 300..399 && redirects < 5) {
                        val location = connection.getHeaderField("Location") ?: break
                        connection.disconnect()
                        currentUrl = location
                        redirects++
                    } else {
                        break
                    }
                }

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Download failed with HTTP ${connection.responseCode}")
                }

                connection.inputStream.use { input ->
                    apkFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (!apkFile.exists() || apkFile.length() < 10_000L) {
                    throw Exception("Downloaded APK is incomplete or corrupted.")
                }

                runOnUiThread {
                    progressDialog.dismiss()
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressDialog.dismiss()
                    showCustomToast("Update failed: ${e.message}")
                }
            }
        }.start()
    }

    private fun installApk(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                pendingUpdateFile = apkFile
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun showCustomToast(message: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val msg = dialog.findViewById<TextView>(R.id.dialog_message)
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_dialog_cancel)
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_dialog_confirm)

        title.text = "Message"
        title.visibility = View.VISIBLE
        btnCancel.visibility = View.GONE
        btnConfirm.text = "OK"
        msg.text = message

        btnConfirm.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
