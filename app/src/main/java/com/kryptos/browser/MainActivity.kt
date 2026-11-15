package com.kryptos.browser

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.app.DownloadManager
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var addressBar: EditText? = null
    private var progressBar: ProgressBar? = null
    private var homepageContainer: FrameLayout? = null
    private var errorContainer: FrameLayout? = null
    private var bottomDock: LinearLayout? = null
    private var txtTrackerCount: TextView? = null
    private var txtTabCount: TextView? = null
    private var btnEngineSwitch: ImageView? = null
    private var speedDialGrid: GridLayout? = null
    private var btnBookmark: ImageView? = null

    private var suggestionsList: ListView? = null
    private var suggestionsAdapter: ArrayAdapter<String>? = null
    private val suggestionHandler = Handler(Looper.getMainLooper())
    private var suggestionRunnable: Runnable? = null
    private var isKeyboardVisible = false
    private val currentSuggestions = mutableListOf<String>()
    private var isUpdatingUrl = false
    private var isReaderMode = false

    private var lastFailedUrl: String? = null
    private lateinit var prefs: android.content.SharedPreferences
    private var trackerCount = 0

    private lateinit var dbHelper: HistoryDatabaseHelper
    private lateinit var bookmarkDb: BookmarkDatabaseHelper
    private lateinit var downloadDb: DownloadDatabaseHelper
    private lateinit var dockController: DockController

    private var speechRecognizer: SpeechRecognizer? = null
    private var voiceSearchDialog: Dialog? = null
    private val normalCookiesBackup = mutableMapOf<String, String>()

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var isImmersiveMode = false

    // Incognito Auto-Kill State & Detection
    private val isWipingAndKilling = AtomicBoolean(false)
    private var userLeftViaHomeOrRecents = false
    private val screenOffCheckHandler = Handler(Looper.getMainLooper())
    private val pollDelays = longArrayOf(100L, 250L, 500L, 850L, 1300L)

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == Display.DEFAULT_DISPLAY) {
                if (!isDisplayOn() && shouldKillIncognito()) {
                    executeIncognitoKillNow()
                }
            }
        }
    }

    companion object {
        private val trackedActivities = mutableListOf<WeakReference<Activity>>()
        private var currentMainRef: WeakReference<MainActivity>? = null
        private var lifecycleCallbacksRegistered = false
        private var startedActivityCount = 0

        private fun registerActivityTracker(app: Application, mainActivity: MainActivity) {
            currentMainRef = WeakReference(mainActivity)
            trackedActivities.removeAll { it.get() == null || it.get() == mainActivity }
            trackedActivities.add(WeakReference(mainActivity))

            if (lifecycleCallbacksRegistered) return
            lifecycleCallbacksRegistered = true

            app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    trackedActivities.removeAll { it.get() == null || it.get() == activity }
                    trackedActivities.add(WeakReference(activity))
                    applySecureStateToActivity(activity)
                }

                override fun onActivityStarted(activity: Activity) {
                    if (activity !is SplashActivity) {
                        startedActivityCount++
                    }
                    applySecureStateToActivity(activity)
                }

                override fun onActivityResumed(activity: Activity) {
                    applySecureStateToActivity(activity)
                    val main = currentMainRef?.get()
                    if (main != null) {
                        main.userLeftViaHomeOrRecents = false
                    }
                }

                override fun onActivityPaused(activity: Activity) {
                    val main = currentMainRef?.get()
                    if (main != null && main.isScreenOffOrLocked() && main.shouldKillIncognito()) {
                        main.executeIncognitoKillNow()
                    }
                }

                override fun onActivityStopped(activity: Activity) {
                    if (activity !is SplashActivity) {
                        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
                    }
                    val main = currentMainRef?.get() ?: return

                    if (startedActivityCount == 0 && !activity.isChangingConfigurations && main.shouldKillIncognito()) {
                        if (!main.userLeftViaHomeOrRecents || main.isScreenOffOrLocked()) {
                            main.executeIncognitoKillNow()
                        } else {
                            main.scheduleScreenOffPolls()
                        }
                    }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    if (TabManager.hasIncognitoTabs()) {
                        outState.clear()
                    }
                }

                override fun onActivityDestroyed(activity: Activity) {
                    trackedActivities.removeAll { it.get() == null || it.get() == activity }
                }
            })
        }

        private fun applySecureStateToActivity(activity: Activity) {
            if (activity is SplashActivity) return
            val hasIncognito = TabManager.hasIncognitoTabs()
            if (hasIncognito) {
                activity.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.setRecentsScreenshotEnabled(false)
                }
            } else {
                activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.setRecentsScreenshotEnabled(true)
                }
            }
        }
    }

    private val searchEngines = listOf(
        Pair("Google", "https://www.google.com/search?q="),
        Pair("Bing", "https://www.bing.com/search?q="),
        Pair("DuckDuckGo", "https://duckduckgo.com/?q="),
        Pair("Brave Search", "https://search.brave.com/search?q="),
        Pair("Yahoo", "https://search.yahoo.com/search?p="),
        Pair("Ecosia", "https://www.ecosia.org/search?q=")
    )
    private var selectedEngineIndex = 0

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                if (shouldKillIncognito()) {
                    executeIncognitoKillNow()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences("kryptos_prefs", Context.MODE_PRIVATE)

        if (!prefs.getBoolean("incognito_kill_v2_fixed", false)) {
            prefs.edit()
                .putBoolean("incognito_kill", true)
                .putBoolean("incognito_kill_v2_fixed", true)
                .commit()
        }

        if (intent?.getBooleanExtra("from_splash", false) == true) {
            isWipingAndKilling.set(false)
            prefs.edit().putBoolean("pending_incognito_exit", false).commit()
            intent?.removeExtra("from_splash")
        } else if (prefs.getBoolean("pending_incognito_exit", false)) {
            super.onCreate(null)
            window.setBackgroundDrawable(ColorDrawable(Color.BLACK))
            abortAndRemoveTaskSilently()
            return
        }

        super.onCreate(if (TabManager.hasIncognitoTabs()) null else savedInstanceState)
        registerActivityTracker(application, this)
        setContentView(R.layout.activity_main)

        dbHelper = HistoryDatabaseHelper(this)
        bookmarkDb = BookmarkDatabaseHelper(this)
        downloadDb = DownloadDatabaseHelper(this)

        selectedEngineIndex = prefs.getInt("default_search_engine", 0).coerceIn(0, searchEngines.lastIndex)
        trackerCount = prefs.getInt("tracker_count", 0)

        webView = findViewById(R.id.webview)
        addressBar = findViewById(R.id.address_bar)
        progressBar = findViewById(R.id.progress_bar)
        homepageContainer = findViewById(R.id.homepage_container)
        errorContainer = findViewById(R.id.error_container)
        bottomDock = findViewById(R.id.bottom_dock)
        txtTrackerCount = findViewById(R.id.txt_tracker_count)
        txtTabCount = findViewById(R.id.txt_tab_count)
        btnEngineSwitch = findViewById(R.id.btn_engine_switch)
        speedDialGrid = findViewById(R.id.speed_dial_grid)
        btnBookmark = findViewById(R.id.btn_bookmark)
        suggestionsList = findViewById(R.id.suggestions_list)

        dockController = DockController(
            this,
            bottomDock!!,
            findViewById(R.id.pill_container),
            findViewById(R.id.pill_view),
            webView
        )

        setupSuggestions()
        updateTrackerUI()
        updateTabCountUI()
        setupWebView()
        setupToolbar()
        setupErrorPage()
        setupBackPressHandler()

        renderSpeedDials()
        checkAndClearCache()

        try {
            val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            dm.registerDisplayListener(displayListener, screenOffCheckHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOffReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(screenOffReceiver, filter)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("from_splash", false)) {
            isWipingAndKilling.set(false)
            prefs.edit().putBoolean("pending_incognito_exit", false).commit()
            intent.removeExtra("from_splash")
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        userLeftViaHomeOrRecents = true
    }

    private fun shouldKillIncognito(): Boolean {
        return prefs.getBoolean("incognito_kill", true) && TabManager.hasIncognitoTabs()
    }

    private fun isDisplayOn(): Boolean {
        return try {
            val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = dm.getDisplay(Display.DEFAULT_DISPLAY)
            display == null || display.state == Display.STATE_ON
        } catch (e: Exception) {
            true
        }
    }

    private fun isScreenOffOrLocked(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return !powerManager.isInteractive || keyguardManager.isKeyguardLocked || !isDisplayOn()
    }

    private fun scheduleScreenOffPolls() {
        screenOffCheckHandler.removeCallbacksAndMessages(null)
        for (delayMs in pollDelays) {
            screenOffCheckHandler.postDelayed({
                if (isScreenOffOrLocked() && shouldKillIncognito()) {
                    executeIncognitoKillNow()
                }
            }, delayMs)
        }
    }

    fun updateSecureFlag() {
        val hasIncognito = TabManager.hasIncognitoTabs()
        if (hasIncognito) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                setRecentsScreenshotEnabled(false)
            }
            setTaskExcludedFromRecents(true)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                setRecentsScreenshotEnabled(true)
            }
            setTaskExcludedFromRecents(false)
        }
    }

    private fun setTaskExcludedFromRecents(exclude: Boolean) {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.appTasks.forEach { it.setExcludeFromRecents(exclude) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun abortAndRemoveTaskSilently() {
        trackedActivities.mapNotNull { it.get() }.forEach { act ->
            if (act !== this && !act.isFinishing) {
                try {
                    act.finish()
                    act.overridePendingTransition(0, 0)
                } catch (e: Exception) { }
            }
        }

        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.appTasks.forEach { task ->
                task.setExcludeFromRecents(true)
                task.finishAndRemoveTask()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        finishAndRemoveTask()
        overridePendingTransition(0, 0)
    }

    private fun updateTrackerUI() {
        txtTrackerCount?.text = String.format("%,d", trackerCount)
    }

    private fun updateTabCountUI() {
        val countTxt = findViewById<TextView>(R.id.txt_tab_count)
        val incognitoIcon = findViewById<ImageView>(R.id.img_incognito_icon)

        val activeTab = TabManager.getActiveTab()

        if (activeTab?.isIncognito == true) {
            countTxt?.visibility = View.GONE
            incognitoIcon?.visibility = View.VISIBLE
        } else {
            countTxt?.visibility = View.VISIBLE
            incognitoIcon?.visibility = View.GONE
            countTxt?.text = TabManager.tabsList.size.toString()
        }
        updateSecureFlag()
    }

    private fun setupSuggestions() {
        suggestionsAdapter = object : ArrayAdapter<String>(this, R.layout.item_suggestion, R.id.txt_suggestion, mutableListOf()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val txtSuggestion = view.findViewById<TextView>(R.id.txt_suggestion)
                val btnArrow = view.findViewById<ImageView>(R.id.btn_arrow_paste)

                val item = getItem(position) ?: ""
                txtSuggestion.text = item

                view.setOnClickListener {
                    if (item.isNotEmpty()) {
                        isUpdatingUrl = true
                        addressBar?.setText(item)
                        isUpdatingUrl = false
                        hideSuggestions()
                        loadUrlOrSearch(item)
                    }
                }

                btnArrow.setOnClickListener {
                    isUpdatingUrl = true
                    addressBar?.setText(item)
                    isUpdatingUrl = false
                    addressBar?.setSelection(addressBar?.text?.length ?: 0)
                }
                return view
            }
        }
        suggestionsList?.adapter = suggestionsAdapter

        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - r.bottom
            val newKeyboardState = keypadHeight > screenHeight * 0.15

            if (isKeyboardVisible != newKeyboardState) {
                isKeyboardVisible = newKeyboardState
                if (addressBar?.hasFocus() == true && suggestionsList?.visibility == View.VISIBLE) {
                    val limit = if (isKeyboardVisible) 4 else 8
                    updateSuggestionsView(currentSuggestions.toList(), limit)
                }
            }
        }

        addressBar?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingUrl) return
                val query = s.toString().trim()
                if (query.isEmpty() || query == "kryptos://home") {
                    hideSuggestions()
                    return
                }
                if (prefs.getBoolean("search_suggestions", true)) {
                    fetchSuggestions(query)
                }
            }
        })
    }

    private fun updateSuggestionsView(suggestions: List<String>, limit: Int) {
        suggestionsAdapter?.clear()
        suggestionsAdapter?.addAll(suggestions.take(limit))
        suggestionsAdapter?.notifyDataSetChanged()
        if (suggestions.isNotEmpty()) {
            suggestionsList?.visibility = View.VISIBLE
        } else {
            suggestionsList?.visibility = View.GONE
        }
    }

    private fun fetchSuggestions(query: String) {
        suggestionRunnable?.let { suggestionHandler.removeCallbacks(it) }
        suggestionRunnable = Runnable {
            Thread {
                try {
                    val encodedQuery = URLEncoder.encode(query, "UTF-8")
                    val url = java.net.URL("https://suggestqueries.google.com/complete/search?c