package com.elg.studly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elg.studly.adapters.primary.navigation.MygesApp
import com.elg.studly.adapters.secondary.play.AndroidPlayQualityManager
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.ui.theme.MygesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var playQualityManager: AndroidPlayQualityManager
    @Inject lateinit var settingsRepository: SettingsRepository

    private var oauthCallbackUri by mutableStateOf<Uri?>(null)
    private var notificationRoute by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestNotificationPermissionIfNeeded()
        handleIntent(intent)
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val darkTheme = when (settings?.themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                else -> isSystemInDarkTheme()
            }
            val view = LocalView.current
            SideEffect {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
            MygesTheme(
                darkTheme = darkTheme,
                dynamicColor = settings?.dynamicColorEnabled == true
            ) {
                MygesApp(
                    oauthCallbackUri = oauthCallbackUri,
                    onOAuthCallbackConsumed = {
                        oauthCallbackUri = null
                        intent.data = null
                    },
                    notificationRoute = notificationRoute,
                    onNotificationRouteConsumed = {
                        notificationRoute = null
                        intent.removeExtra(EXTRA_NOTIFICATION_ROUTE)
                        if (intent.data?.scheme == SHORTCUT_SCHEME) intent.data = null
                    },
                    onSuccessfulRefresh = playQualityManager::requestReview
                )
            }
        }
        playQualityManager.checkForFlexibleUpdate(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun handleIntent(intent: Intent?) {
        val incomingIntent = intent ?: return
        val data = incomingIntent.data
        when {
            incomingIntent.action == Intent.ACTION_VIEW && data.isOAuthCallback() -> {
                oauthCallbackUri = data
            }
            incomingIntent.action == Intent.ACTION_VIEW && data?.scheme == SHORTCUT_SCHEME -> {
                notificationRoute = data.host
            }
            incomingIntent.hasExtra(EXTRA_NOTIFICATION_ROUTE) -> {
                notificationRoute = incomingIntent.getStringExtra(EXTRA_NOTIFICATION_ROUTE)
            }
        }
    }

    private fun Uri?.isOAuthCallback(): Boolean {
        if (this == null) return false
        val redirect = Uri.parse(BuildConfig.KORDIS_OAUTH_REDIRECT_URI)
        return scheme.equals(redirect.scheme, ignoreCase = true) &&
            authority.equals(redirect.authority, ignoreCase = true) &&
            (redirect.path.isNullOrEmpty() || path == redirect.path)
    }

    companion object {
        const val EXTRA_NOTIFICATION_ROUTE = "com.elg.studly.extra.NOTIFICATION_ROUTE"
        private const val SHORTCUT_SCHEME = "myges"
    }
}
