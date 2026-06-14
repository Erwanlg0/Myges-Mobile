package com.elg.myges.domain.model

import java.time.Instant
import java.time.LocalDate

data class StudentProfile(
    val id: String,
    val displayName: String,
    val email: String?,
    val school: String?,
    val program: String?,
    val academicYear: String?,
    val avatarUrl: String?
)

data class AgendaEvent(
    val id: String,
    val title: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val room: String?,
    val teacher: String?,
    val type: String?,
    val modality: String?,
    val courseId: String?,
    val address: String? = null,
    val colorId: String? = null
)

data class Grade(
    val id: String,
    val courseName: String,
    val subject: String,
    val value: Double?,
    val scale: Double?,
    val coefficient: Double?,
    val average: Double?,
    val date: LocalDate?,
    val period: String?
)

data class Absence(
    val id: String,
    val courseName: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val justified: Boolean,
    val status: String?,
    val reason: String?,
    val period: String? = null
)

data class Course(
    val id: String,
    val name: String,
    val teacher: String?,
    val year: String?,
    val period: String?,
    val syllabus: String?,
    val fileCount: Int,
    val location: String? = null
)

data class ProjectStep(
    val id: String,
    val title: String,
    val deadline: Instant?,
    val status: String?
)

data class Project(
    val id: String,
    val name: String,
    val courseName: String?,
    val groupName: String?,
    val status: String?,
    val deadline: Instant?,
    val steps: List<ProjectStep>,
    val fileCount: Int
)

data class Practical(
    val id: String,
    val name: String,
    val courseName: String?,
    val startsAt: Instant?,
    val endsAt: Instant?,
    val room: String?,
    val status: String?
)

data class AcademicDocument(
    val id: String,
    val title: String,
    val category: String?,
    val year: String?,
    val mimeType: String?,
    val fileName: String,
    val downloadUrl: String?,
    val updatedAt: Instant?
)

data class NewsItem(
    val id: String,
    val title: String,
    val body: String?,
    val publishedAt: Instant?
)

data class DashboardSummary(
    val profile: StudentProfile?,
    val nextEvent: AgendaEvent?,
    val latestGrades: List<Grade>,
    val recentAbsences: List<Absence>,
    val dueProjects: List<Project>,
    val lastSyncAt: Instant?
)

data class Session(
    val username: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Instant?,
    val biometricEnabled: Boolean,
    val issuedAt: Instant,
    val refreshAfter: Instant
) {
    val isExpired: Boolean
        get() = expiresAt?.isBefore(Instant.now()) == true

    val requiresRefresh: Boolean
        get() = refreshAfter.isBefore(Instant.now())
}

data class NotificationPreferences(
    val grades: Boolean,
    val absences: Boolean,
    val agenda: Boolean,
    val projects: Boolean,
    val documents: Boolean
)

data class UserSettings(
    val languageTag: String?,
    val notifications: NotificationPreferences,
    val calendarSyncEnabled: Boolean,
    val lastSyncAt: Instant?
)

enum class Feature {
    Agenda,
    Grades,
    Absences,
    Courses,
    Projects,
    Practicals,
    Documents,
    Notifications
}
