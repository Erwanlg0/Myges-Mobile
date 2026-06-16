package com.elg.studly.adapters.primary.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.studly.R
import com.elg.studly.adapters.primary.state.FeatureUiState
import com.elg.studly.domain.model.AppError
import com.elg.studly.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test

class FeatureStateContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingStateShowsProgressIndicator() {
        composeRule.setContent {
            MygesTheme {
                FeatureStateContent(
                    state = FeatureUiState(emptyList<String>(), loading = true),
                    empty = List<String>::isEmpty,
                    emptyTitle = R.string.grades_empty_title,
                    emptyBody = R.string.grades_empty_body,
                    onRetry = {},
                    content = {}
                )
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsLocalizedMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            MygesTheme {
                FeatureStateContent(
                    state = FeatureUiState(emptyList<String>(), loading = false),
                    empty = List<String>::isEmpty,
                    emptyTitle = R.string.grades_empty_title,
                    emptyBody = R.string.grades_empty_body,
                    onRetry = {},
                    content = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.grades_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.grades_empty_body)).assertIsDisplayed()
    }

    @Test
    fun errorStateShowsLocalizedMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            MygesTheme {
                FeatureStateContent(
                    state = FeatureUiState(emptyList<String>(), loading = false, error = AppError.Network),
                    empty = List<String>::isEmpty,
                    emptyTitle = R.string.grades_empty_title,
                    emptyBody = R.string.grades_empty_body,
                    onRetry = {},
                    content = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.error_network)).assertIsDisplayed()
    }

    @Test
    fun offlineStateWithoutCacheShowsLocalizedMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            MygesTheme {
                FeatureStateContent(
                    state = FeatureUiState(emptyList<String>(), loading = false, online = false),
                    empty = List<String>::isEmpty,
                    emptyTitle = R.string.grades_empty_title,
                    emptyBody = R.string.grades_empty_body,
                    onRetry = {},
                    content = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.state_offline_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.state_offline_empty_body)).assertIsDisplayed()
    }
}
