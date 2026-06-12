package com.elg.myges.adapters.secondary.storage

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class StudentDao {
    @Query("SELECT * FROM student_profile LIMIT 1")
    abstract fun observeProfile(): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM agenda_events ORDER BY startsAtEpochMillis ASC")
    abstract fun observeAgenda(): Flow<List<AgendaEventEntity>>

    @Query("SELECT * FROM grades ORDER BY dateIso DESC, courseName ASC")
    abstract fun observeGrades(): Flow<List<GradeEntity>>

    @Query("SELECT * FROM absences ORDER BY startsAtEpochMillis DESC")
    abstract fun observeAbsences(): Flow<List<AbsenceEntity>>

    @Query("SELECT * FROM courses ORDER BY name ASC")
    abstract fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM projects ORDER BY deadlineEpochMillis ASC, name ASC")
    abstract fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM project_steps ORDER BY deadlineEpochMillis ASC, title ASC")
    abstract fun observeProjectSteps(): Flow<List<ProjectStepEntity>>

    @Query("SELECT * FROM practicals ORDER BY startsAtEpochMillis ASC, name ASC")
    abstract fun observePracticals(): Flow<List<PracticalEntity>>

    @Query("SELECT * FROM documents ORDER BY updatedAtEpochMillis DESC, title ASC")
    abstract fun observeDocuments(): Flow<List<AcademicDocumentEntity>>

    @Query("SELECT * FROM news ORDER BY publishedAtEpochMillis DESC, title ASC")
    abstract fun observeNews(): Flow<List<NewsEntity>>

    @Query("SELECT id FROM agenda_events")
    abstract suspend fun agendaIds(): List<String>

    @Query("SELECT id FROM grades")
    abstract suspend fun gradeIds(): List<String>

    @Query("SELECT id FROM absences")
    abstract suspend fun absenceIds(): List<String>

    @Query("SELECT id FROM projects")
    abstract suspend fun projectIds(): List<String>

    @Query("SELECT id FROM documents")
    abstract suspend fun documentIds(): List<String>

    @Upsert
    abstract suspend fun upsertProfile(profile: StudentProfileEntity)

    @Upsert
    abstract suspend fun upsertAgenda(events: List<AgendaEventEntity>)

    @Upsert
    abstract suspend fun upsertGrades(grades: List<GradeEntity>)

    @Upsert
    abstract suspend fun upsertAbsences(absences: List<AbsenceEntity>)

    @Upsert
    abstract suspend fun upsertCourses(courses: List<CourseEntity>)

    @Upsert
    abstract suspend fun upsertProjects(projects: List<ProjectEntity>)

    @Upsert
    abstract suspend fun upsertProjectSteps(steps: List<ProjectStepEntity>)

    @Upsert
    abstract suspend fun upsertPracticals(practicals: List<PracticalEntity>)

    @Upsert
    abstract suspend fun upsertDocuments(documents: List<AcademicDocumentEntity>)

    @Upsert
    abstract suspend fun upsertNews(news: List<NewsEntity>)

    @Query("DELETE FROM student_profile")
    abstract suspend fun clearProfile()

    @Query("DELETE FROM agenda_events")
    abstract suspend fun clearAgenda()

    @Query("DELETE FROM grades")
    abstract suspend fun clearGrades()

    @Query("DELETE FROM absences")
    abstract suspend fun clearAbsences()

    @Query("DELETE FROM courses")
    abstract suspend fun clearCourses()

    @Query("DELETE FROM projects")
    abstract suspend fun clearProjects()

    @Query("DELETE FROM project_steps")
    abstract suspend fun clearProjectSteps()

    @Query("DELETE FROM practicals")
    abstract suspend fun clearPracticals()

    @Query("DELETE FROM documents")
    abstract suspend fun clearDocuments()

    @Query("DELETE FROM news")
    abstract suspend fun clearNews()

    @Transaction
    open suspend fun replaceSyncedData(
        profile: StudentProfileEntity,
        agenda: List<AgendaEventEntity>,
        grades: List<GradeEntity>,
        absences: List<AbsenceEntity>,
        courses: List<CourseEntity>,
        projects: List<ProjectEntity>,
        projectSteps: List<ProjectStepEntity>,
        practicals: List<PracticalEntity>,
        documents: List<AcademicDocumentEntity>,
        news: List<NewsEntity>
    ) {
        clearProfile()
        clearAgenda()
        clearGrades()
        clearAbsences()
        clearCourses()
        clearProjects()
        clearProjectSteps()
        clearPracticals()
        clearDocuments()
        clearNews()
        upsertProfile(profile)
        upsertAgenda(agenda)
        upsertGrades(grades)
        upsertAbsences(absences)
        upsertCourses(courses)
        upsertProjects(projects)
        upsertProjectSteps(projectSteps)
        upsertPracticals(practicals)
        upsertDocuments(documents)
        upsertNews(news)
    }

    @Transaction
    open suspend fun clearAll() {
        clearProfile()
        clearAgenda()
        clearGrades()
        clearAbsences()
        clearCourses()
        clearProjects()
        clearProjectSteps()
        clearPracticals()
        clearDocuments()
        clearNews()
    }
}
