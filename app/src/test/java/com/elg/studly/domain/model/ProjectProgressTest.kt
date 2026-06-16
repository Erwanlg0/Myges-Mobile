package com.elg.studly.domain.model

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

    @Test
    fun projectProgressIncludesPassedDeadlines() {
        val past = java.time.Instant.now().minusSeconds(3600)
        val future = java.time.Instant.now().plusSeconds(3600)
        val progress = project(
            ProjectStep("one", "Draft", past, "todo"),
            ProjectStep("two", "Final", future, "todo")
        ).progress()

        assertEquals(1, progress.completedSteps)
        assertEquals(2, progress.totalSteps)
        assertEquals(0.5, progress.fraction, 0.0)
    }

    @Test
    fun projectProgressCapsBeforeOneIfUncompleted() {
        val past = java.time.Instant.now().minusSeconds(3600)
        val progress = project(
            ProjectStep("one", "Draft", past, "todo"),
            ProjectStep("two", "Final", past, "todo")
        ).progress()

        assertEquals(1, progress.completedSteps)
        assertEquals(2, progress.totalSteps)
        assertEquals(0.5, progress.fraction, 0.0)
    }

    @Test
    fun projectProgressIgnoresDeadlinesOlderThanSixMonths() {
        val veryPast = java.time.Instant.now().minus(190, java.time.temporal.ChronoUnit.DAYS)
        val progress = project(
            ProjectStep("one", "Draft", veryPast, "todo"),
            ProjectStep("two", "Final", veryPast, "completed")
        ).progress()

        assertEquals(1, progress.completedSteps)
        assertEquals(2, progress.totalSteps)
        assertEquals(0.5, progress.fraction, 0.0)
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
