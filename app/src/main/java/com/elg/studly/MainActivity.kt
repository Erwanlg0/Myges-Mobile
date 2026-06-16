package com.elg.studly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elg.studly.adapters.primary.navigation.MygesApp
import com.elg.studly.adapters.secondary.play.AndroidPlayQualityManager
import com.elg.studly.application.usecase.ObserveSettingsUseCase
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.ui.theme.MygesTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var playQualityManager: AndroidPlayQualityManager
    @Inject lateinit var observeSettings: ObserveSettingsUseCase

    private var oauthCallbackUri by mutableStateOf<Uri?>(null)
    private var notificationRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val themeMode by observeSettings().collectAsStateWithLifecycle(initialValue = null)
            val darkTheme = when (themeMode?.themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                else -> isSystemInDarkTheme()
            }
            MygesTheme(darkTheme = darkTheme) {
                MygesApp(
                    oauthCallbackUri = oauthCallbackUri,
                    onOAuthCallbackConsumed = { oauthCallbackUri = null },
                    notificationRoute = notificationRoute,
                    onNotificationRouteConsumed = { notificationRoute = null },
                    onSuccessfulRefresh = playQualityManager::requestReview
                )
            }
        }
        playQualityManager.checkForFlexibleUpdate(this)
    }

    override fun onResume() {
        super.onResume()
        playQualityManager.checkForFlexibleUpdate(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            oauthCallbackUri = intent.data
        } else {
            notificationRoute = intent?.getStringExtra(EXTRA_NOTIFICATION_ROUTE)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ROUTE = "com.elg.Studly.extra.NOTIFICATION_ROUTE"
    }
}
