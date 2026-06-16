package com.elg.studly.adapters.primary.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.studly.R
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class AgendaEventDetailsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingAgendaEventShowsAndDismissesDetails() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val event = AgendaEvent(
            id = "agenda-1",
            title = "Algorithmique",
            startsAt = Instant.parse("2026-06-12T08:00:00Z"),
            endsAt = Instant.parse("2026-06-12T10:00:00Z"),
            room = "A101",
            teacher = "Mme Martin",
            type = "Cours",
            modality = "Présentiel",
            courseId = "rc-1"
        )

        composeRule.setContent {
            MygesTheme {
                var selectedEvent by remember { mutableStateOf<AgendaEvent?>(null) }
                AgendaEventCard(
                    event = event,
                    onOpen = { selectedEvent = event }
                )
                selectedEvent?.let {
                    AgendaEventDetailsDialog(
                        event = it,
                        onDismiss = { selectedEvent = null }
                    )
                }
            }
        }

        composeRule.onAllNodesWithText(context.getString(R.string.agenda_details_title)).assertCountEquals(0)
        composeRule.onNodeWithText("Algorithmique").performClick()
        composeRule.onNodeWithText(context.getString(R.string.agenda_details_title)).assertIsDisplayed()
        composeRule.onNodeWithText("A101").assertIsDisplayed()
        composeRule.onNodeWithText("Mme Martin").assertIsDisplayed()
        composeRule.onNodeWithText("Présentiel").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.action_close)).performClick()
        composeRule.onAllNodesWithText(context.getString(R.string.agenda_details_title)).assertCountEquals(0)
    }
}
