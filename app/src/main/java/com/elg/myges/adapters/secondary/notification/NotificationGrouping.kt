package com.elg.myges.adapters.secondary.notification

internal fun notificationGroupKey(route: String, subject: String?): String {
    val normalizedSubject = subject
        ?.trim()
        ?.lowercase()
        ?.replace(Regex("\\s+"), "-")
        ?.takeIf { it.isNotBlank() }
        ?: "general"
    return "myges.$route.$normalizedSubject"
}

internal fun stableNotificationId(value: String): Int {
    return value.hashCode().let { if (it == Int.MIN_VALUE) Int.MAX_VALUE else kotlin.math.abs(it) }
}
