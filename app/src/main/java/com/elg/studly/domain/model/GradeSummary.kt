package com.elg.studly.domain.model

data class GradeSummary(
    val weightedAverage: Double?,
    val gpa: Double?,
    val gradedCount: Int,
    val missingCoefficientCount: Int,
    val incomplete: Boolean
)

const val NOT_COUNTED_COEFFICIENT = -1.0

fun Grade.isToeicExcluded(): Boolean =
    (courseName.contains("TOEIC", ignoreCase = true) || subject.contains("TOEIC", ignoreCase = true)) &&
        (value ?: 0.0) > 20.0

fun Grade.isNotCounted(): Boolean = coefficient == NOT_COUNTED_COEFFICIENT

fun Grade.isExcludedFromAverage(): Boolean = isToeicExcluded() || isNotCounted()

fun Grade.isPerfectScore(): Boolean =
    value != null && scale != null && scale > 0.0 && value >= scale && !isToeicExcluded()

fun List<Grade>.toGradeSummary(): GradeSummary {
    val relevant = filterNot { it.isExcludedFromAverage() }
    val graded = relevant.filter { it.value != null && it.scale != null && it.scale > 0.0 }
    val weightedGrades = graded.map { grade ->
        val coefficient = grade.coefficient?.takeIf { it > 0.0 }
        GradeWeight(
            normalizedValue = grade.value!! / grade.scale!! * 20.0,
            coefficient = coefficient ?: 1.0,
            missingCoefficient = coefficient == null
        )
    }
    val missingCoefficientCount = weightedGrades.count { it.missingCoefficient }
    val coefficientSum = weightedGrades.sumOf { it.coefficient }
    val weightedAverage = if (coefficientSum > 0.0) {
        weightedGrades.sumOf { it.normalizedValue * it.coefficient } / coefficientSum
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
    val coefficient: Double,
    val missingCoefficient: Boolean
)

fun Grade.getGradeLetterFromValue(): String? {
    val val20 = value ?: return null
    val score = if (scale != null && scale > 0.0) {
        val20 * (20.0 / scale)
    } else {
        val20
    }
    return when {
        score >= 18.0 -> "A+"
        score >= 16.5 -> "A"
        score >= 15.0 -> "A-"
        score >= 14.5 -> "B+"
        score >= 13.5 -> "B"
        score >= 12.0 -> "B-"
        score >= 10.5 -> "C"
        score >= 9.0 -> "C-"
        score >= 8.0 -> "D+"
        score >= 6.0 -> "D"
        else -> "F"
    }
}

fun getEstimationRangeFromLetter(letter: String): String? {
    return when (letter.uppercase().trim()) {
        "A+" -> "entre 18 et 20"
        "A" -> "entre 16.5 et 17.99"
        "A-" -> "entre 15 et 16.49"
        "B+" -> "entre 14.5 et 14.99"
        "B" -> "entre 13.5 et 14.49"
        "B-" -> "entre 12 et 13.49"
        "C" -> "entre 10.5 et 11.99"
        "C-" -> "entre 9 et 10.49"
        "D+" -> "entre 8 et 8.99"
        "D" -> "entre 6 et 7.99"
        "F" -> "entre 0 et 5.99"
        else -> null
    }
}
