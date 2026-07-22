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

data class AcademicWarning(
    val type: WarningType,
    val subjectOrBlockName: String = "",
    val numericValue: Double? = null
)

enum class WarningType {
    E_LEARNING_LOW,
    ZERO_GRADE,
    TWO_GRADES_BELOW_SIX,
    BLOCK_AVERAGE_LOW,
    MANDATORY_COURSE_FAILED,
    ANNUAL_AVERAGE_RATTRAPAGE,
    ANNUAL_AVERAGE_REDOUBLEMENT
}

enum class AcademicStatus {
    VALIDATED,
    RATTRAPAGE,
    REDOUBLEMENT
}

data class AcademicEvaluation(
    val status: AcademicStatus,
    val warnings: List<AcademicWarning>
)

fun Grade.blockKey(): String {
    return "${academicYearLabel()}|$courseName"
}

private fun Grade.academicYearLabel(): String {
    val regex = Regex("\\d{4}-\\d{4}")
    regex.find(period.orEmpty())?.value?.replace(" ", "")?.let { return it }
    val gradeDate = date ?: return ""
    val startYear = if (gradeDate.monthValue >= 9) gradeDate.year else gradeDate.year - 1
    return "$startYear-${startYear + 1}"
}

fun List<Grade>.evaluateAcademicRules(
    blockAssignments: Map<String, String>,
    mandatoryCourses: Set<String> = emptySet()
): AcademicEvaluation {
    val mainGradesList = this.mainGrades()
    val warnings = mutableListOf<AcademicWarning>()

    val summary = mainGradesList.toGradeSummary()
    val annualAvg = summary.weightedAverage

    // 1. Check e-learning grades
    val elearningGrades = mainGradesList.filter { grade ->
        grade.courseName.contains("e-learning", ignoreCase = true) ||
        grade.courseName.contains("elearning", ignoreCase = true) ||
        grade.courseName.contains("e learning", ignoreCase = true) ||
        grade.subject.contains("e-learning", ignoreCase = true) ||
        grade.subject.contains("elearning", ignoreCase = true)
    }

    for (grade in elearningGrades) {
        val valOn20 = grade.value?.let { v ->
            val s = grade.scale ?: 20.0
            if (s > 0) v / s * 20.0 else v
        }
        if (valOn20 != null && valOn20 < 6.0) {
            warnings.add(
                AcademicWarning(
                    type = WarningType.E_LEARNING_LOW,
                    subjectOrBlockName = grade.courseName,
                    numericValue = valOn20
                )
            )
        }
    }

    // 2. Check 0/20 in any UV
    for (grade in mainGradesList) {
        val valOn20 = grade.value?.let { v ->
            val s = grade.scale ?: 20.0
            if (s > 0) v / s * 20.0 else v
        }
        if (valOn20 != null && valOn20 == 0.0) {
            warnings.add(
                AcademicWarning(
                    type = WarningType.ZERO_GRADE,
                    subjectOrBlockName = grade.courseName,
                    numericValue = 0.0
                )
            )
        }
    }

    // 3. Check blocks
    val groupedByBlock = mainGradesList.groupBy { blockAssignments[it.blockKey()]?.takeIf { b -> b.isNotBlank() } }

    for ((blockName, blockGrades) in groupedByBlock) {
        if (blockName == null) continue

        val uvsBelowSix = blockGrades.count { grade ->
            val valOn20 = grade.value?.let { v ->
                val s = grade.scale ?: 20.0
                if (s > 0) v / s * 20.0 else v
            }
            valOn20 != null && valOn20 < 6.0
        }
        if (uvsBelowSix >= 2) {
            warnings.add(
                AcademicWarning(
                    type = WarningType.TWO_GRADES_BELOW_SIX,
                    subjectOrBlockName = blockName,
                    numericValue = uvsBelowSix.toDouble()
                )
            )
        }

        val blockSummary = blockGrades.toGradeSummary()
        val blockAvg = blockSummary.weightedAverage
        if (blockAvg != null && blockAvg < 10.0) {
            warnings.add(
                AcademicWarning(
                    type = WarningType.BLOCK_AVERAGE_LOW,
                    subjectOrBlockName = blockName,
                    numericValue = blockAvg
                )
            )
        }
    }

    // 4. Check mandatory courses (< 10/20)
    for (grade in mainGradesList) {
        val key = grade.blockKey()
        if (key in mandatoryCourses) {
            val valOn20 = grade.value?.let { v ->
                val s = grade.scale ?: 20.0
                if (s > 0) v / s * 20.0 else v
            }
            if (valOn20 != null && valOn20 < 10.0) {
                warnings.add(
                    AcademicWarning(
                        type = WarningType.MANDATORY_COURSE_FAILED,
                        subjectOrBlockName = grade.courseName,
                        numericValue = valOn20
                    )
                )
            }
        }
    }

    // 4. Annual average check
    if (annualAvg != null) {
        if (annualAvg < 8.0) {
            warnings.add(
                AcademicWarning(
                    type = WarningType.ANNUAL_AVERAGE_REDOUBLEMENT,
                    numericValue = annualAvg
                )
            )
        } else if (annualAvg < 10.0) {
            warnings.add(
                AcademicWarning(
                    type = WarningType.ANNUAL_AVERAGE_RATTRAPAGE,
                    numericValue = annualAvg
                )
            )
        }
    }

    val status = when {
        annualAvg != null && annualAvg < 8.0 -> AcademicStatus.REDOUBLEMENT
        warnings.any { it.type == WarningType.ANNUAL_AVERAGE_REDOUBLEMENT } -> AcademicStatus.REDOUBLEMENT
        warnings.isNotEmpty() || (annualAvg != null && annualAvg < 10.0) -> AcademicStatus.RATTRAPAGE
        annualAvg != null && annualAvg >= 10.0 -> AcademicStatus.VALIDATED
        else -> AcademicStatus.VALIDATED
    }

    return AcademicEvaluation(status = status, warnings = warnings)
}

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

