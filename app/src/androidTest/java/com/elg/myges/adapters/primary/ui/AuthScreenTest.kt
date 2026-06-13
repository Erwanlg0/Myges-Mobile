package com.elg.myges.adapters.primary.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.myges.R
import com.elg.myges.ui.theme.MygesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun authScreenShowsBiometricSessionActionAndTogglesPreference() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var biometricPreference: Boolean? = null
        var loginCount = 0
        var unlockCount = 0

        composeRule.setContent {
            MygesTheme {
                AuthScreen(
                    loading = false,
                    errorMessage = null,
                    hasBiometricSession = true,
                    enableBiometric = true,
                    authorizationUrl = "https://authentication.example/oauth",
                    onBiometricEnabledChange = { biometricPreference = it },
                    onLogin = { loginCount += 1 },
                    onBiometricUnlock = { unlockCount += 1 }
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.auth_enable_biometric)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.auth_login_kordis)).assertIsEnabled().performClick()
        composeRule.onNodeWithText(context.getString(R.string.auth_biometric_unlock)).assertIsEnabled().performClick()

        assertEquals(false, biometricPreference)
        assertEquals(1, loginCount)
        assertEquals(1, unlockCount)
    }

    @Test
    fun authScreenDisablesActionsWhileLoadingAndShowsError() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MygesTheme {
                AuthScreen(
                    loading = true,
                    errorMessage = R.string.error_unauthorized,
                    hasBiometricSession = true,
                    enableBiometric = true,
                    authorizationUrl = "https://authentication.example/oauth",
                    onBiometricEnabledChange = {},
                    onLogin = {},
                    onBiometricUnlock = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.error_unauthorized)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.auth_biometric_unlock)).assertIsNotEnabled()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }
}
