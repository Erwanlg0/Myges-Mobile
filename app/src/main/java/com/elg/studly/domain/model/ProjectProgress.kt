package com.elg.studly.domain.model

import java.time.Instant

data class ProjectProgress(
    val completedSteps: Int,
    val totalSteps: Int,
    val fraction: Double
)


private fun ProjectStep.isCompleted(now: Instant): Boolean =
    status.isCompletedStatus() || (deadline != null && deadline.isBefore(now))

fun Project.progress(): ProjectProgress = stepsProgress(steps, status)

fun Practical.progress(): ProjectProgress = stepsProgress(steps, status)

private fun stepsProgress(steps: List<ProjectStep>, status: String?): ProjectProgress {
    val now = Instant.now()
    val total = steps.size
    val done = status.isCompletedStatus()
    if (total == 0) {
        return ProjectProgress(0, 0, if (done) 1.0 else 0.0)
    }
    val completed = if (done) total else steps.count { it.isCompleted(now) }
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
