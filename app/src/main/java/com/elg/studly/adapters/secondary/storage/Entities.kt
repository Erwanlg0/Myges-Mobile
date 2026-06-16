package com.elg.studly.adapters.secondary.storage

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
    val fileCount: Int,
    val year: String?,
    val courseId: String?,
    val startsAtEpochMillis: Long? = null
)

@Entity(tableName = "project_groups", primaryKeys = ["projectId", "id"])
data class ProjectGroupEntity(
    val projectId: String,
    val id: String,
    val name: String,
    val students: String,
    val isMine: Boolean
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
    val status: String?,
    val year: String?
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
    val updatedAtEpochMillis: Long?,
    val ownerId: String?,
    val groupId: String?,
    val inlineContent: String?
)

@Entity(tableName = "directory_people")
data class DirectoryPersonEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String?,
    val role: String,
    val year: String?,
    val groupName: String?,
    val avatarUrl: String?
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String?,
    val publishedAtEpochMillis: Long?
)
