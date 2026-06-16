package com.elg.studly.domain.model

data class ProjectProgress(
    val completedSteps: Int,
    val totalSteps: Int,
    val fraction: Double
)

fun Project.progress(): ProjectProgress {
    val total = steps.size
    if (total == 0) return ProjectProgress(0, 0, 0.0)
    val now = java.time.Instant.now()
    val sixMonthsAgo = now.minus(180, java.time.temporal.ChronoUnit.DAYS)
    val completedCount = steps.count {
        it.status.isCompletedStatus() || (it.deadline != null && it.deadline.isBefore(now) && it.deadline.isAfter(sixMonthsAgo))
    }
    val hasUncompleted = steps.any { !it.status.isCompletedStatus() }
    val completed = if (hasUncompleted && completedCount == total) total - 1 else completedCount
    return ProjectProgress(
        completedSteps = completed,
        totalSteps = total,
        fraction = completed.toDouble() / total.toDouble()
    )
}

fun Practical.progress(): ProjectProgress {
    val total = steps.size
    if (total == 0) return ProjectProgress(0, 0, 0.0)
    val now = java.time.Instant.now()
    val sixMonthsAgo = now.minus(180, java.time.temporal.ChronoUnit.DAYS)
    val completedCount = steps.count {
        it.status.isCompletedStatus() || (it.deadline != null && it.deadline.isBefore(now) && it.deadline.isAfter(sixMonthsAgo))
    }
    val hasUncompleted = steps.any { !it.status.isCompletedStatus() }
    val completed = if (hasUncompleted && completedCount == total) total - 1 else completedCount
    return ProjectProgress(
        completedSteps = completed,
        totalSteps = total,
        fraction = completed.toDouble() / total.toDouble()
    )
}

private fun String?.isCompletedStatus(): Boolean {
    val normalized = this?.trim()?.lowercase().orEmpty()
    return normalized in setOf(
        "done",
        "completed",
        "complete",
        "closed",
        "validated",
        "validé",
        "validée",
        "terminé",
        "terminée",
        "fini",
        "finie",
        "rendu",
        "rendue"
    )
}
