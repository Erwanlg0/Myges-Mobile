package com.elg.myges.adapters.primary.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.myges.R
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.ProjectStep
import com.elg.myges.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class ProjectCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun projectCardShowsProgressAndSteps() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val deadline = Instant.parse("2026-06-30T23:00:00Z")

        composeRule.setContent {
            MygesTheme {
                ProjectCard(
                    Project(
                        id = "project-1",
                        name = "Projet Android",
                        courseName = "Mobile",
                        groupName = "Groupe 1",
                        status = "En cours",
                        deadline = deadline,
                        steps = listOf(
                            ProjectStep(
                                id = "step-1",
                                title = "Sujet",
                                deadline = deadline.minusSeconds(86400),
                                status = "Terminé"
                            ),
                            ProjectStep(
                                id = "step-2",
                                title = "Rendu",
                                deadline = deadline,
                                status = "En cours"
                            )
                        ),
                        fileCount = 1
                    )
                )
            }
        }

        composeRule.onNodeWithText("Projet Android").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.projects_steps_completed, 1, 2)).assertIsDisplayed()
        composeRule.onNodeWithText("Rendu").assertIsDisplayed()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.5f, 0f..1f, 0))).assertIsDisplayed()
    }
}
