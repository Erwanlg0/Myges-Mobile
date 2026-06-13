package com.elg.myges.adapters.primary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.elg.myges.domain.model.Practical
import com.elg.myges.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class PracticalCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun practicalCardShowsCourseRoomAndStatus() {
        composeRule.setContent {
            MygesTheme {
                PracticalCard(
                    Practical(
                        id = "practical-1",
                        name = "TP Kotlin",
                        courseName = "Mobile",
                        startsAt = Instant.parse("2026-06-12T08:00:00Z"),
                        endsAt = Instant.parse("2026-06-12T10:00:00Z"),
                        room = "B204",
                        status = "Planifié"
                    )
                )
            }
        }

        composeRule.onNodeWithText("TP Kotlin").assertIsDisplayed()
        composeRule.onNodeWithText("Mobile").assertIsDisplayed()
        composeRule.onNodeWithText("B204").assertIsDisplayed()
        composeRule.onNodeWithText("Planifié").assertIsDisplayed()
    }
}
