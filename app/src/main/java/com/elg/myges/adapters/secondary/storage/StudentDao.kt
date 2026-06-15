package com.elg.myges.adapters.secondary.storage

import androidx.room.Dao
import androidx.room.Delete
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

    @Query("SELECT * FROM project_groups ORDER BY name ASC")
    abstract fun observeProjectGroups(): Flow<List<ProjectGroupEntity>>

    @Query("SELECT * FROM practicals ORDER BY startsAtEpochMillis ASC, name ASC")
    abstract fun observePracticals(): Flow<List<PracticalEntity>>

    @Query("SELECT * FROM documents ORDER BY updatedAtEpochMillis DESC, title ASC")
    abstract fun observeDocuments(): Flow<List<AcademicDocumentEntity>>

    @Query("SELECT * FROM directory_people ORDER BY role ASC, displayName ASC")
    abstract fun observeDirectory(): Flow<List<DirectoryPersonEntity>>

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

    @Query("SELECT * FROM student_profile LIMIT 1")
    abstract suspend fun profile(): StudentProfileEntity?

    @Query("SELECT * FROM agenda_events")
    abstract suspend fun agenda(): List<AgendaEventEntity>

    @Query("SELECT * FROM grades")
    abstract suspend fun grades(): List<GradeEntity>

    @Query("SELECT * FROM absences")
    abstract suspend fun absences(): List<AbsenceEntity>

    @Query("SELECT * FROM courses")
    abstract suspend fun courses(): List<CourseEntity>

    @Query("SELECT * FROM projects")
    abstract suspend fun projects(): List<ProjectEntity>

    @Query("SELECT * FROM project_steps")
    abstract suspend fun projectSteps(): List<ProjectStepEntity>

    @Query("SELECT * FROM project_groups")
    abstract suspend fun projectGroups(): List<ProjectGroupEntity>

    @Query("SELECT * FROM practicals")
    abstract suspend fun practicals(): List<PracticalEntity>

    @Query("SELECT * FROM documents")
    abstract suspend fun documents(): List<AcademicDocumentEntity>

    @Query("SELECT * FROM directory_people")
    abstract suspend fun directoryPeople(): List<DirectoryPersonEntity>

    @Query("SELECT * FROM news")
    abstract suspend fun news(): List<NewsEntity>

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
    abstract suspend fun upsertProjectGroups(groups: List<ProjectGroupEntity>)

    @Upsert
    abstract suspend fun upsertPracticals(practicals: List<PracticalEntity>)

    @Upsert
    abstract suspend fun upsertDocuments(documents: List<AcademicDocumentEntity>)

    @Upsert
    abstract suspend fun upsertDirectoryPeople(people: List<DirectoryPersonEntity>)

    @Upsert
    abstract suspend fun upsertNews(news: List<NewsEntity>)

    @Delete
    abstract suspend fun deleteAgenda(events: List<AgendaEventEntity>)

    @Delete
    abstract suspend fun deleteGrades(grades: List<GradeEntity>)

    @Delete
    abstract suspend fun deleteAbsences(absences: List<AbsenceEntity>)

    @Delete
    abstract suspend fun deleteCourses(courses: List<CourseEntity>)

    @Delete
    abstract suspend fun deleteProjects(projects: List<ProjectEntity>)

    @Delete
    abstract suspend fun deleteProjectSteps(steps: List<ProjectStepEntity>)

    @Delete
    abstract suspend fun deleteProjectGroups(groups: List<ProjectGroupEntity>)

    @Delete
    abstract suspend fun deletePracticals(practicals: List<PracticalEntity>)

    @Delete
    abstract suspend fun deleteDocuments(documents: List<AcademicDocumentEntity>)

    @Delete
    abstract suspend fun deleteDirectoryPeople(people: List<DirectoryPersonEntity>)

    @Delete
    abstract suspend fun deleteNews(news: List<NewsEntity>)

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

    @Query("DELETE FROM project_groups")
    abstract suspend fun clearProjectGroups()

    @Query("DELETE FROM practicals")
    abstract suspend fun clearPracticals()

    @Query("DELETE FROM documents")
    abstract suspend fun clearDocuments()

    @Query("DELETE FROM directory_people")
    abstract suspend fun clearDirectoryPeople()

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
        projectGroups: List<ProjectGroupEntity>,
        projectSteps: List<ProjectStepEntity>,
        practicals: List<PracticalEntity>,
        documents: List<AcademicDocumentEntity>,
        directoryPeople: List<DirectoryPersonEntity>,
        news: List<NewsEntity>
    ) {
        replaceProfile(profile)
        replaceAgenda(agenda)
        replaceGrades(grades)
        replaceAbsences(absences)
        replaceCourses(courses)
        replaceProjects(projects)
        replaceProjectGroups(projectGroups)
        replaceProjectSteps(projectSteps)
        replacePracticals(practicals)
        replaceDocuments(documents)
        replaceDirectoryPeople(directoryPeople)
        replaceNews(news)
    }

    @Transaction
    open suspend fun clearAll() {
        clearProfile()
        clearAgenda()
        clearGrades()
        clearAbsences()
        clearCourses()
        clearProjects()
        clearProjectGroups()
        clearProjectSteps()
        clearPracticals()
        clearDocuments()
        clearDirectoryPeople()
        clearNews()
    }

    private suspend fun replaceProfile(profile: StudentProfileEntity) {
        val current = profile()
        if (current?.id != profile.id) clearProfile()
        if (current != profile) upsertProfile(profile)
    }

    private suspend fun replaceAgenda(incoming: List<AgendaEventEntity>) {
        val plan = entitySyncPlan(agenda(), incoming, AgendaEventEntity::id)
        if (plan.deletes.isNotEmpty()) deleteAgenda(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertAgenda(plan.upserts)
    }

    private suspend fun replaceGrades(incoming: List<GradeEntity>) {
        val plan = entitySyncPlan(grades(), incoming, GradeEntity::id)
        if (plan.deletes.isNotEmpty()) deleteGrades(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertGrades(plan.upserts)
    }

    private suspend fun replaceAbsences(incoming: List<AbsenceEntity>) {
        val plan = entitySyncPlan(absences(), incoming, AbsenceEntity::id)
        if (plan.deletes.isNotEmpty()) deleteAbsences(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertAbsences(plan.upserts)
    }

    private suspend fun replaceCourses(incoming: List<CourseEntity>) {
        val plan = entitySyncPlan(courses(), incoming, CourseEntity::id)
        if (plan.deletes.isNotEmpty()) deleteCourses(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertCourses(plan.upserts)
    }

    private suspend fun replaceProjects(incoming: List<ProjectEntity>) {
        val plan = entitySyncPlan(projects(), incoming, ProjectEntity::id)
        if (plan.deletes.isNotEmpty()) deleteProjects(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertProjects(plan.upserts)
    }

    private suspend fun replaceProjectSteps(incoming: List<ProjectStepEntity>) {
        val plan = entitySyncPlan(projectSteps(), incoming) { "${it.projectId}:${it.id}" }
        if (plan.deletes.isNotEmpty()) deleteProjectSteps(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertProjectSteps(plan.upserts)
    }

    private suspend fun replaceProjectGroups(incoming: List<ProjectGroupEntity>) {
        val plan = entitySyncPlan(projectGroups(), incoming) { "${it.projectId}:${it.id}" }
        if (plan.deletes.isNotEmpty()) deleteProjectGroups(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertProjectGroups(plan.upserts)
    }

    private suspend fun replacePracticals(incoming: List<PracticalEntity>) {
        val plan = entitySyncPlan(practicals(), incoming, PracticalEntity::id)
        if (plan.deletes.isNotEmpty()) deletePracticals(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertPracticals(plan.upserts)
    }

    private suspend fun replaceDocuments(incoming: List<AcademicDocumentEntity>) {
        val plan = entitySyncPlan(documents(), incoming, AcademicDocumentEntity::id)
        if (plan.deletes.isNotEmpty()) deleteDocuments(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertDocuments(plan.upserts)
    }

    private suspend fun replaceDirectoryPeople(incoming: List<DirectoryPersonEntity>) {
        val plan = entitySyncPlan(directoryPeople(), incoming, DirectoryPersonEntity::id)
        if (plan.deletes.isNotEmpty()) deleteDirectoryPeople(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertDirectoryPeople(plan.upserts)
    }

    private suspend fun replaceNews(incoming: List<NewsEntity>) {
        val plan = entitySyncPlan(news(), incoming, NewsEntity::id)
        if (plan.deletes.isNotEmpty()) deleteNews(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertNews(plan.upserts)
    }
}
