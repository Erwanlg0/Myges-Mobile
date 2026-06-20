package com.elg.studly.adapters.primary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.studly.R
import com.elg.studly.domain.model.Grade
import com.elg.studly.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test
import kotlinx.datetime.LocalDate

class GradeSummaryCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun summaryShowsWeightedAverageAndIncompleteNotice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MygesTheme {
                GradeSummaryCard(
                    listOf(
                        Grade(
                            id = "grade-1",
                            courseName = "Algorithms",
                            subject = "Exam",
                            value = 16.0,
                            scale = 20.0,
                            coefficient = 2.0,
                            average = null,
                            date = LocalDate.parse("2026-06-12"),
                            period = "S2"
                        ),
                        Grade(
                            id = "grade-2",
                            courseName = "Android",
                            subject = "Project",
                            value = null,
                            scale = 20.0,
                            coefficient = 1.0,
                            average = null,
                            date = LocalDate.parse("2026-06-13"),
                            period = "S2"
                        )
                    )
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.grades_summary_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.grades_summary_incomplete)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.grades_count)).assertIsDisplayed()
    }

    @Test
    fun summaryShowsNoGradesWhenEveryValueIsMissing() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MygesTheme {
                GradeSummaryCard(
                    listOf(
                        Grade(
                            id = "grade-1",
                            courseName = "Algorithms",
                            subject = "Exam",
                            value = null,
                            scale = 20.0,
                            coefficient = 1.0,
                            average = null,
                            date = LocalDate.parse("2026-06-12"),
                            period = "S2"
                        )
                    )
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.grades_no_grade)).assertIsDisplayed()
    }
}
