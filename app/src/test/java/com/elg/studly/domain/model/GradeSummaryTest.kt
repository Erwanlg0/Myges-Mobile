package com.elg.studly.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun gradeSummaryExcludesToeicAndNotCountedGrades() {
        val grades = listOf(
            grade("toeic", courseName = "TOEIC", subject = "Exam", value = 980.0, scale = 990.0, coefficient = 1.0),
            grade("bonus", value = 20.0, scale = 20.0, coefficient = NOT_COUNTED_COEFFICIENT),
            grade("exam", value = 12.0, scale = 20.0, coefficient = 1.0)
        )

        val summary = grades.toGradeSummary()

        assertTrue(grades[0].isToeicExcluded())
        assertTrue(grades[1].isNotCounted())
        assertTrue(grades[0].isExcludedFromAverage())
        assertEquals(12.0, summary.weightedAverage ?: 0.0, 0.0)
        assertFalse(summary.incomplete)
    }

    @Test
    fun toeicGradeAtTwentyIsNotExcluded() {
        val grade = grade("toeic", subject = "TOEIC", value = 20.0, scale = 20.0, coefficient = 1.0)

        assertFalse(grade.isToeicExcluded())
        assertFalse(grade("toeic-null", subject = "TOEIC", value = null, scale = 20.0, coefficient = 1.0).isToeicExcluded())
    }

    @Test
    fun gradeSummaryDetectsPerfectScores() {
        assertTrue(grade("perfect", value = 20.0, scale = 20.0, coefficient = 1.0).isPerfectScore())
        assertFalse(grade("missing", value = null, scale = 20.0, coefficient = 1.0).isPerfectScore())
        assertFalse(grade("missing-scale", value = 20.0, scale = null, coefficient = 1.0).isPerfectScore())
        assertFalse(grade("zero-scale", value = 20.0, scale = 0.0, coefficient = 1.0).isPerfectScore())
        assertFalse(grade("toeic", courseName = "TOEIC", value = 990.0, scale = 990.0, coefficient = 1.0).isPerfectScore())
    }

    @Test
    fun gradeSummaryReturnsNullAverageWhenNothingIsGraded() {
        val summary = listOf(grade("empty", value = null, scale = null, coefficient = null)).toGradeSummary()

        assertEquals(null, summary.weightedAverage)
        assertEquals(null, summary.gpa)
        assertEquals(0, summary.gradedCount)
        assertTrue(summary.incomplete)
    }

    @Test
    fun gradeSummaryTreatsZeroCoefficientAsMissing() {
        val summary = listOf(grade("zero", value = 12.0, scale = 20.0, coefficient = 0.0)).toGradeSummary()

        assertEquals(12.0, summary.weightedAverage ?: 0.0, 0.0)
        assertEquals(1, summary.missingCoefficientCount)
    }

    @Test
    fun combineCcExamUsesAvailableValues() {
        assertEquals(15.0, combineCcExam(10.0, 20.0) ?: 0.0, 0.0)
        assertEquals(10.0, combineCcExam(10.0, null) ?: 0.0, 0.0)
        assertEquals(20.0, combineCcExam(null, 20.0) ?: 0.0, 0.0)
        assertEquals(null, combineCcExam(null, null))
    }

    @Test
    fun mainGradesRemovesStructuredCcAndExamParts() {
        val grades = listOf(
            grade("math", courseName = "Math", subject = "", value = 14.0, scale = 20.0, coefficient = 1.0, period = "S1"),
            grade("math-cc", courseName = "Math", subject = "CC 1", value = 12.0, scale = 20.0, coefficient = 1.0, period = "S1"),
            grade("math-exam", courseName = "Math", subject = "examen", value = 16.0, scale = 20.0, coefficient = 1.0, period = "S1"),
            grade("history", courseName = "History", subject = "Essay", value = 15.0, scale = 20.0, coefficient = 1.0, period = "S1")
        )

        assertEquals(listOf("math", "history"), grades.mainGrades().map { it.id })
    }

    @Test
    fun mainGradesRemovesSyntheticCcAndExamIds() {
        val grades = listOf(
            grade("math-cc-1", courseName = "Math", subject = "Essay", value = 12.0, scale = 20.0, coefficient = 1.0, period = "S1"),
            grade("math-exam-1", courseName = "Math", subject = "Essay", value = 16.0, scale = 20.0, coefficient = 1.0, period = "S1"),
            grade("math-main", courseName = "Math", subject = "Essay", value = 14.0, scale = 20.0, coefficient = 1.0, period = "S1")
        )

        assertEquals(listOf("math-main"), grades.mainGrades().map { it.id })
    }

    @Test
    fun mainGradesKeepsExamSubjectWhenNoStructuredMainGradeExists() {
        val grades = listOf(
            grade("math-label", courseName = "Math", subject = "examen", value = 16.0, scale = 20.0, coefficient = 1.0, period = "S1")
        )

        assertEquals(listOf("math-label"), grades.mainGrades().map { it.id })
    }

    private fun grade(
        id: String,
        courseName: String = "Course",
        subject: String = "Exam",
        value: Double?,
        scale: Double?,
        coefficient: Double?,
        period: String? = null
    ): Grade {
        return Grade(
            id = id,
            courseName = courseName,
            subject = subject,
            value = value,
            scale = scale,
            coefficient = coefficient,
            average = null,
            date = null,
            period = period
        )
    }
}
