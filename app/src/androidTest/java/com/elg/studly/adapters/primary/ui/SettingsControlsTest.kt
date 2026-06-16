package com.elg.studly.adapters.primary.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.studly.R
import com.elg.studly.ui.theme.MygesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsControlsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun languageSelectorUpdatesSelectedLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var selectedLanguage: String? by mutableStateOf(null)

        composeRule.setContent {
            MygesTheme {
                LanguageSelector(
                    selectedLanguageTag = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it }
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_language_system)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_language_english)).performClick()

        assertEquals("en", selectedLanguage)
    }

    @Test
    fun switchRowTogglesValueFromEntireRow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var checked by mutableStateOf(false)

        composeRule.setContent {
            MygesTheme {
                SwitchRow(
                    title = R.string.settings_calendar_sync,
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_calendar_sync)).assertIsOff()
        composeRule.onNodeWithText(context.getString(R.string.settings_calendar_sync)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_calendar_sync)).assertIsOn()
    }
}
