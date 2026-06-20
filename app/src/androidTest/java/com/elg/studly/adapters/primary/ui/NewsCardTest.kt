package com.elg.studly.adapters.primary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant
import com.elg.studly.adapters.time.*

class NewsCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun newsCardShowsTitleAndBody() {
        composeRule.setContent {
            MygesTheme {
                NewsCard(
                    NewsItem(
                        id = "news-1",
                        title = "Campus",
                        body = "Salle modifiée pour le cours de demain.",
                        publishedAt = Instant.parse("2026-06-12T08:00:00Z")
                    )
                )
            }
        }

        composeRule.onNodeWithText("Campus").assertIsDisplayed()
        composeRule.onNodeWithText("Salle modifiée pour le cours de demain.").assertIsDisplayed()
    }
}
