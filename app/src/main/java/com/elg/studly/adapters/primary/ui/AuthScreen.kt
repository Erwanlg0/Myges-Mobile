package com.elg.studly.adapters.primary.ui

import android.net.Uri
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elg.studly.R
import com.elg.studly.adapters.primary.viewmodel.AuthViewModel

@Composable
fun AuthRoute(
    oauthCallbackUri: Uri?,
    onOAuthCallbackConsumed: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val promptTitle = stringResource(R.string.auth_biometric_title)
    val promptSubtitle = stringResource(R.string.auth_biometric_subtitle)
    val promptCancel = stringResource(R.string.action_cancel)
    val biometricPrompt = activity?.let {
        BiometricPrompt(
            it,
            ContextCompat.getMainExecutor(it),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.unlockWithBiometrics()
                }
            }
        )
    }
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(promptTitle)
        .setSubtitle(promptSubtitle)
        .setNegativeButtonText(promptCancel)
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    LaunchedEffect(oauthCallbackUri) {
        oauthCallbackUri?.let {
            viewModel.completeOAuthCallback(it)
            onOAuthCallbackConsumed()
        }
    }

    AuthScreen(
        loading = state.loading,
        error = state.error,
        hasBiometricSession = state.hasBiometricSession,
        authorizationUrl = state.authorizationUrl,
        onLogin = {
            val authorizationUrl = viewModel.beginOAuthLogin()
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(authorizationUrl))
        },
        onBiometricUnlock = { biometricPrompt?.authenticate(promptInfo) }
    )
}

@Composable
internal fun AuthScreen(
    loading: Boolean,
    error: com.elg.studly.domain.model.AppError?,
    hasBiometricSession: Boolean,
    authorizationUrl: String,
    onLogin: () -> Unit,
    onBiometricUnlock: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.auth_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))
            error?.let {
                StateBanner(it)
                Spacer(Modifier.height(12.dp))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && authorizationUrl.isNotBlank(),
                onClick = onLogin
            ) {
                if (loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.Login, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.auth_login_kordis))
                }
            }
            if (hasBiometricSession) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    onClick = onBiometricUnlock
                ) {
                    Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.auth_biometric_unlock))
                }
            }
        }
    }
}
