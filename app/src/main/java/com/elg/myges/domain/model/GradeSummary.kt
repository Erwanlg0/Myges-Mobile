package com.elg.myges.domain.model

data class GradeSummary(
    val weightedAverage: Double?,
    val gpa: Double?,
    val gradedCount: Int,
    val missingCoefficientCount: Int,
    val incomplete: Boolean
)

fun List<Grade>.toGradeSummary(): GradeSummary {
    val graded = filter { it.value != null && it.scale != null && it.scale > 0.0 }
    val weightedGrades = graded.map { grade ->
        val coefficient = grade.coefficient?.takeIf { it > 0.0 }
        GradeWeight(
            normalizedValue = grade.value!! / grade.scale!! * 20.0,
            coefficient = coefficient
        )
    }
    val missingCoefficientCount = weightedGrades.count { it.coefficient == null }
    val usableWeights = weightedGrades.map { it.copy(coefficient = it.coefficient ?: 1.0) }
    val coefficientSum = usableWeights.sumOf { it.coefficient ?: 0.0 }
    val weightedAverage = if (coefficientSum > 0.0) {
        usableWeights.sumOf { it.normalizedValue * (it.coefficient ?: 0.0) } / coefficientSum
    } else {
        null
    }
    return GradeSummary(
        weightedAverage = weightedAverage,
        gpa = weightedAverage?.let { (it / 20.0 * 4.0).coerceIn(0.0, 4.0) },
        gradedCount = graded.size,
        missingCoefficientCount = missingCoefficientCount,
        incomplete = missingCoefficientCount > 0 || graded.size < size
    )
}

private data class GradeWeight(
    val normalizedValue: Double,
    val coefficient: Double?
)
