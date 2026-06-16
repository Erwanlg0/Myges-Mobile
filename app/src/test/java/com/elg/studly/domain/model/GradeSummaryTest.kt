package com.elg.studly.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeSummaryTest {
    @Test
    fun gradeSummaryComputesWeightedAverageOnTwentyAndGpa() {
        val summary = listOf(
            grade("one", value = 16.0, scale = 20.0, coefficient = 2.0),
            grade("two", value = 8.0, scale = 10.0, coefficient = 1.0)
        ).toGradeSummary()

        assertEquals(16.0, summary.weightedAverage ?: 0.0, 0.0)
        assertEquals(3.2, summary.gpa ?: 0.0, 0.0)
        assertEquals(2, summary.gradedCount)
    }

    @Test
    fun gradeSummaryMarksIncompleteWhenCoefficientOrValueIsMissing() {
        val summary = listOf(
            grade("one", value = 15.0, scale = 20.0, coefficient = null),
            grade("two", value = null, scale = 20.0, coefficient = 1.0)
        ).toGradeSummary()

        assertEquals(15.0, summary.weightedAverage ?: 0.0, 0.0)
        assertEquals(1, summary.missingCoefficientCount)
        assertTrue(summary.incomplete)
    }

    private fun grade(
        id: String,
        value: Double?,
        scale: Double?,
        coefficient: Double?
    ): Grade {
        return Grade(
            id = id,
            courseName = "Course",
            subject = "Exam",
            value = value,
            scale = scale,
            coefficient = coefficient,
            average = null,
            date = null,
            period = null
        )
    }
}
