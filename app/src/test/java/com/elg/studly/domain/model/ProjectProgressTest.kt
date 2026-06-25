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
    fun projectProgressIsFullWhenCompletedProjectHasNoSteps() {
        val progress = Project(
            id = "project-1",
            name = "Project",
            courseName = null,
            groupName = null,
            status = "done",
            deadline = null,
            steps = emptyList(),
            fileCount = 0
        ).progress()

        assertEquals(0, progress.completedSteps)
        assertEquals(0, progress.totalSteps)
        assertEquals(1.0, progress.fraction, 0.0)
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
    fun projectProgressIsFullWhenAllDeadlinesPassed() {
        val past = java.time.Instant.now().minusSeconds(3600)
        val progress = project(
            ProjectStep("one", "Draft", past, "todo"),
            ProjectStep("two", "Final", past, "todo")
        ).progress()

        assertEquals(2, progress.completedSteps)
        assertEquals(2, progress.totalSteps)
        assertEquals(1.0, progress.fraction, 0.0)
    }

    @Test
    fun projectProgressCountsDeadlinesOlderThanSixMonths() {
        val veryPast = java.time.Instant.now().minus(190, java.time.temporal.ChronoUnit.DAYS)
        val progress = project(
            ProjectStep("one", "Draft", veryPast, "todo"),
            ProjectStep("two", "Final", veryPast, "completed")
        ).progress()

        assertEquals(2, progress.completedSteps)
        assertEquals(2, progress.totalSteps)
        assertEquals(1.0, progress.fraction, 0.0)
    }

    @Test
    fun projectProgressIgnoresProjectDeadlineAndUsesSteps() {
        
        val past = java.time.Instant.now().minusSeconds(3600)
        val future = java.time.Instant.now().plusSeconds(3600)
        val progress = Project(
            id = "project-1",
            name = "Project",
            courseName = null,
            groupName = null,
            status = null,
            deadline = past,
            steps = listOf(
                ProjectStep("one", "Draft", future, "todo"),
                ProjectStep("two", "Final", future, "todo")
            ),
            fileCount = 0
        ).progress()

        assertEquals(0, progress.completedSteps)
        assertEquals(2, progress.totalSteps)
        assertEquals(0.0, progress.fraction, 0.0)
    }

    @Test
    fun projectProgressIsFullWhenStatusCompleted() {
        val future = java.time.Instant.now().plusSeconds(3600)
        val progress = Project(
            id = "project-1",
            name = "Project",
            courseName = null,
            groupName = null,
            status = "terminé",
            deadline = null,
            steps = listOf(
                ProjectStep("one", "Draft", future, "todo"),
                ProjectStep("two", "Final", future, "todo")
            ),
            fileCount = 0
        ).progress()

        assertEquals(2, progress.completedSteps)
        assertEquals(1.0, progress.fraction, 0.0)
    }

    @Test
    fun practicalProgressHandlesPracticalsWithoutSteps() {
        val progress = practical().progress()

        assertEquals(0, progress.completedSteps)
        assertEquals(0, progress.totalSteps)
        assertEquals(0.0, progress.fraction, 0.0)
    }

    @Test
    fun practicalProgressIsFullWhenCompletedPracticalHasNoSteps() {
        val progress = practical(status = "closed").progress()

        assertEquals(0, progress.completedSteps)
        assertEquals(0, progress.totalSteps)
        assertEquals(1.0, progress.fraction, 0.0)
    }

    @Test
    fun practicalProgressCountsCompletedSteps() {
        val past = java.time.Instant.now().minusSeconds(3600)
        val future = java.time.Instant.now().plusSeconds(3600)
        val progress = practical(
            ProjectStep("one", "Draft", past, "todo"),
            ProjectStep("two", "Final", future, "todo"),
            ProjectStep("three", "Oral", null, "rendue")
        ).progress()

        assertEquals(2, progress.completedSteps)
        assertEquals(3, progress.totalSteps)
        assertEquals(2.0 / 3.0, progress.fraction, 0.0)
    }

    @Test
    fun practicalProgressIsFullWhenStatusCompleted() {
        val future = java.time.Instant.now().plusSeconds(3600)
        val progress = practical(
            ProjectStep("one", "Draft", future, "todo"),
            ProjectStep("two", "Final", future, "todo"),
            status = "finie"
        ).progress()

        assertEquals(2, progress.completedSteps)
        assertEquals(2, progress.totalSteps)
        assertEquals(1.0, progress.fraction, 0.0)
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

    private fun practical(vararg steps: ProjectStep, status: String? = null): Practical {
        return Practical(
            id = "practical-1",
            name = "Practical",
            courseName = null,
            startsAt = null,
            endsAt = null,
            room = null,
            status = status,
            steps = steps.toList()
        )
    }
}
