package com.elg.myges.domain.model

data class ProjectProgress(
    val completedSteps: Int,
    val totalSteps: Int,
    val fraction: Double
)

fun Project.progress(): ProjectProgress {
    val total = steps.size
    if (total == 0) return ProjectProgress(0, 0, 0.0)
    val completed = steps.count { it.status.isCompletedStatus() }
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
