package com.elg.myges.adapters.primary.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.myges.R
import com.elg.myges.domain.model.Course
import com.elg.myges.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test

class CourseCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun syllabusCanBeExpandedAndCollapsed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val syllabus = "Detailed algorithm syllabus"

        composeRule.setContent {
            MygesTheme {
                CourseCard(
                    Course(
                        id = "course-1",
                        name = "Algorithms",
                        teacher = "Teacher",
                        year = "2026",
                        period = "S2",
                        syllabus = syllabus,
                        fileCount = 1
                    )
                )
            }
        }

        composeRule.onAllNodesWithText(syllabus).assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.courses_show_syllabus)).performClick()
        composeRule.onNodeWithText(syllabus).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.courses_hide_syllabus)).performClick()
        composeRule.onAllNodesWithText(syllabus).assertCountEquals(0)
    }
}
