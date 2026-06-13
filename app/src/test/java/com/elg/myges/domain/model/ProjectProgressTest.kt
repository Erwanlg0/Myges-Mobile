package com.elg.myges.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectProgressTest {
    @Test
    fun projectProgressCountsCompletedSteps() {
        val progress = project(
            ProjectStep("one", "Draft", null, "validé"),
            ProjectStep("two", "Final", null, "todo"),
            ProjectStep("three", "Oral", null, "completed")
        ).progress()

        assertEquals(2, progress.completedSteps)
        assertEquals(3, progress.totalSteps)
        assertEquals(2.0 / 3.0, progress.fraction, 0.0)
    }

    @Test
    fun projectProgressHandlesProjectsWithoutSteps() {
        val progress = project().progress()

        assertEquals(0, progress.completedSteps)
        assertEquals(0, progress.totalSteps)
        assertEquals(0.0, progress.fraction, 0.0)
    }

    private fun project(vararg steps: ProjectStep): Project {
        return Project(
            id = "project-1",
            name = "Project",
            courseName = null,
            groupName = null,
            status = null,
            deadline = null,
            steps = steps.toList(),
            fileCount = 0
        )
    }
}
