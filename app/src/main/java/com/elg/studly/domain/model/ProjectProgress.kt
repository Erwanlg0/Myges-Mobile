package com.elg.studly.domain.model

import java.time.Instant

data class ProjectProgress(
    val completedSteps: Int,
    val totalSteps: Int,
    val fraction: Double
)


private fun ProjectStep.isCompleted(now: Instant): Boolean =
    status.isCompletedStatus() || (deadline != null && deadline.isBefore(now))

fun Project.progress(): ProjectProgress {
    val now = Instant.now()
    val total = steps.size
    
    
    val projectDone = status.isCompletedStatus()
    if (total == 0) {
        return ProjectProgress(0, 0, if (projectDone) 1.0 else 0.0)
    }
    val completed = if (projectDone) total else steps.count { it.isCompleted(now) }
    return ProjectProgress(
        completedSteps = completed,
        totalSteps = total,
        fraction = completed.toDouble() / total.toDouble()
    )
}

fun Practical.progress(): ProjectProgress {
    val now = Instant.now()
    val total = steps.size
    val practicalDone = status.isCompletedStatus()
    if (total == 0) {
        return ProjectProgress(0, 0, if (practicalDone) 1.0 else 0.0)
    }
    val completed = if (practicalDone) total else steps.count { it.isCompleted(now) }
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
