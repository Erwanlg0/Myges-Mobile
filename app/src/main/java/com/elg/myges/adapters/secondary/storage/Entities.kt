package com.elg.myges.adapters.secondary.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String?,
    val school: String?,
    val program: String?,
    val academicYear: String?,
    val avatarUrl: String?
)

@Entity(tableName = "agenda_events")
data class AgendaEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startsAtEpochMillis: Long,
    val endsAtEpochMillis: Long,
    val room: String?,
    val teacher: String?,
    val type: String?,
    val modality: String?,
    val courseId: String?,
    val address: String? = null,
    val colorId: String? = null
)

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey val id: String,
    val courseName: String,
    val subject: String,
    val value: Double?,
    val scale: Double?,
    val coefficient: Double?,
    val average: Double?,
    val dateIso: String?,
    val period: String?
)

@Entity(tableName = "absences")
data class AbsenceEntity(
    @PrimaryKey val id: String,
    val courseName: String,
    val startsAtEpochMillis: Long,
    val endsAtEpochMillis: Long,
    val justified: Boolean,
    val status: String?,
    val reason: String?,
    val period: String? = null
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val teacher: String?,
    val year: String?,
    val period: String?,
    val syllabus: String?,
    val fileCount: Int,
    val location: String?
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val courseName: String?,
    val groupName: String?,
    val status: String?,
    val deadlineEpochMillis: Long?,
    val fileCount: Int
)

@Entity(tableName = "project_steps", primaryKeys = ["projectId", "id"])
data class ProjectStepEntity(
    val projectId: String,
    val id: String,
    val title: String,
    val deadlineEpochMillis: Long?,
    val status: String?
)

@Entity(tableName = "practicals")
data class PracticalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val courseName: String?,
    val startsAtEpochMillis: Long?,
    val endsAtEpochMillis: Long?,
    val room: String?,
    val status: String?
)

@Entity(tableName = "documents")
data class AcademicDocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String?,
    val year: String?,
    val mimeType: String?,
    val fileName: String,
    val downloadUrl: String?,
    val updatedAtEpochMillis: Long?
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String?,
    val publishedAtEpochMillis: Long?
)
