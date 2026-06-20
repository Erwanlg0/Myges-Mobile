package com.elg.studly.adapters.primary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.studly.R
import com.elg.studly.domain.model.Absence
import com.elg.studly.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant
import com.elg.studly.adapters.time.*

class AbsenceCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun absenceCardShowsStatus() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MygesTheme {
                AbsenceCard(
                    Absence(
                        id = "absence-1",
                        courseName = "Architecture Android",
                        startsAt = Instant.parse("2026-06-12T08:00:00Z"),
                        endsAt = Instant.parse("2026-06-12T10:00:00Z"),
                        justified = false,
                        status = "En attente",
                        reason = "Transport"
                    )
                )
            }
        }

        composeRule.onNodeWithText("Architecture Android").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.absences_unjustified)).assertIsDisplayed()
    }
}
