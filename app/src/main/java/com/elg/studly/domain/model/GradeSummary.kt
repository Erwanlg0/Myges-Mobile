package com.elg.studly.domain.model

data class GradeSummary(
    val weightedAverage: Double?,
    val gpa: Double?,
    val gradedCount: Int,
    val missingCoefficientCount: Int,
    val incomplete: Boolean
)

fun Grade.isToeicExcluded(): Boolean =
    (courseName.contains("TOEIC", ignoreCase = true) || subject.contains("TOEIC", ignoreCase = true)) &&
        (value ?: 0.0) > 20.0

fun List<Grade>.toGradeSummary(): GradeSummary {
    val relevant = filterNot { it.isToeicExcluded() }
    val graded = relevant.filter { it.value != null && it.scale != null && it.scale > 0.0 }
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
        incomplete = missingCoefficientCount > 0 || graded.size < relevant.size
    )
}

private val CC_SUBJECT_REGEX = Regex("^(cc|contrôle continu)\\s*\\d*$", RegexOption.IGNORE_CASE)

fun combineCcExam(ccAverage: Double?, examValue: Double?): Double? = when {
    ccAverage != null && examValue != null -> 0.5 * ccAverage + 0.5 * examValue
    ccAverage != null -> ccAverage
    examValue != null -> examValue
    else -> null
}

fun List<Grade>.mainGrades(): List<Grade> {
    val structuredKeys = filter { it.subject.isBlank() && !it.id.contains("-cc-") && !it.id.contains("-exam") }
        .map { it.courseName to it.period }
        .toSet()
    return filter { grade ->
        !grade.id.contains("-cc-") &&
        !grade.id.contains("-exam") &&
        !((grade.courseName to grade.period) in structuredKeys &&
            (grade.subject.matches(CC_SUBJECT_REGEX) ||
             grade.subject.trim().equals("examen", ignoreCase = true)))
    }
}

private data class GradeWeight(
    val normalizedValue: Double,
    val coefficient: Double?
)
