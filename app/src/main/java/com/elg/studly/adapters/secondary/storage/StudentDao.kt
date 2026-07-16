package com.elg.studly.adapters.secondary.storage

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

    @Query("SELECT id, title, category, year, mimeType, fileName, downloadUrl, updatedAtEpochMillis, ownerId, groupId, NULL AS inlineContent FROM documents ORDER BY updatedAtEpochMillis DESC, title ASC")
    abstract fun observeDocuments(): Flow<List<AcademicDocumentEntity>>

    @Query("SELECT inlineContent FROM documents WHERE id = :id")
    abstract suspend fun documentInlineContent(id: String): String?

    @Query("SELECT * FROM directory_people ORDER BY role ASC, displayName ASC")
    abstract fun observeDirectory(): Flow<List<DirectoryPersonEntity>>

    @Query("SELECT * FROM news ORDER BY publishedAtEpochMillis DESC, title ASC")
    abstract fun observeNews(): Flow<List<NewsEntity>>

    @Query("SELECT * FROM events ORDER BY dateEpochMillis DESC, title ASC")
    abstract fun observeEvents(): Flow<List<StudentEventEntity>>

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

    @Query("SELECT * FROM events")
    abstract suspend fun events(): List<StudentEventEntity>

    @Query("UPDATE events SET subscribed = :subscribed WHERE id = :eventId")
    abstract suspend fun updateEventSubscribed(eventId: String, subscribed: Boolean)

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

    @Query("DELETE FROM project_groups WHERE projectId = :projectId")
    abstract suspend fun deleteGroupsForProject(projectId: String)

    @Transaction
    open suspend fun replaceGroupsForProject(projectId: String, groups: List<ProjectGroupEntity>) {
        deleteGroupsForProject(projectId)
        upsertProjectGroups(groups)
    }

    @Upsert
    abstract suspend fun upsertPracticals(practicals: List<PracticalEntity>)

    @Upsert
    abstract suspend fun upsertDocuments(documents: List<AcademicDocumentEntity>)

    @Upsert
    abstract suspend fun upsertDirectoryPeople(people: List<DirectoryPersonEntity>)

    @Upsert
    abstract suspend fun upsertNews(news: List<NewsEntity>)

    @Upsert
    abstract suspend fun upsertEvents(events: List<StudentEventEntity>)

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

    @Delete
    abstract suspend fun deleteEvents(events: List<StudentEventEntity>)

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

    @Query("DELETE FROM events")
    abstract suspend fun clearEvents()

    @Transaction
    open suspend fun syncProfile(profile: StudentProfileEntity) = replaceProfile(profile)

    @Transaction
    open suspend fun syncAgenda(agenda: List<AgendaEventEntity>) = replaceAgenda(agenda)

    @Transaction
    open suspend fun syncAgendaWindow(
        agenda: List<AgendaEventEntity>,
        startEpochMillis: Long,
        endEpochMillis: Long
    ) = replaceAgenda(
        agenda,
        this.agenda().filter { event ->
            event.endsAtEpochMillis >= startEpochMillis && event.startsAtEpochMillis <= endEpochMillis
        }
    )

    @Transaction
    open suspend fun syncGrades(grades: List<GradeEntity>) = replaceGrades(grades)

    @Transaction
    open suspend fun syncAbsences(absences: List<AbsenceEntity>) = replaceAbsences(absences)

    @Transaction
    open suspend fun syncCourses(courses: List<CourseEntity>) = replaceCourses(courses)

    @Transaction
    open suspend fun syncProjectsAndPracticals(
        projects: List<ProjectEntity>,
        projectGroups: List<ProjectGroupEntity>,
        projectSteps: List<ProjectStepEntity>,
        practicals: List<PracticalEntity>
    ) {
        replaceProjects(projects)
        replaceProjectGroups(projectGroups)
        replaceProjectSteps(projectSteps)
        replacePracticals(practicals)
    }

    @Transaction
    open suspend fun syncDocuments(documents: List<AcademicDocumentEntity>) = replaceDocuments(documents)

    @Transaction
    open suspend fun syncDirectory(directoryPeople: List<DirectoryPersonEntity>) = replaceDirectoryPeople(directoryPeople)

    @Transaction
    open suspend fun syncNews(news: List<NewsEntity>) = replaceNews(news)

    @Transaction
    open suspend fun syncEvents(events: List<StudentEventEntity>) = replaceEvents(events)

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
        clearEvents()
    }

    private suspend fun replaceProfile(profile: StudentProfileEntity) {
        val current = profile()
        if (current?.id != profile.id) clearProfile()
        if (current != profile) upsertProfile(profile)
    }

    private suspend fun replaceAgenda(incoming: List<AgendaEventEntity>) =
        replaceAgenda(incoming, agenda())

    private suspend fun replaceAgenda(incoming: List<AgendaEventEntity>, current: List<AgendaEventEntity>) {
        val plan = entitySyncPlan(current, incoming, AgendaEventEntity::id)
        if (plan.deletes.isNotEmpty()) deleteAgenda(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertAgenda(plan.upserts)
    }

    private suspend fun replaceGrades(incoming: List<GradeEntity>) {
        val existingGrades = grades().associateBy { it.id }
        val adjustedIncoming = incoming.map { grade ->
            if (grade.value == null && grade.gradeLetter == "F") {
                val existing = existingGrades[grade.id]
                val wasNonF = existing != null && (existing.value != null || (existing.gradeLetter != null && existing.gradeLetter != "F"))
                if (wasNonF) {
                    grade
                } else {
                    grade.copy(gradeLetter = null)
                }
            } else {
                grade
            }
        }
        val plan = entitySyncPlan(grades(), adjustedIncoming, GradeEntity::id)
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

    private suspend fun replaceEvents(incoming: List<StudentEventEntity>) {
        val plan = entitySyncPlan(events(), incoming, StudentEventEntity::id)
        if (plan.deletes.isNotEmpty()) deleteEvents(plan.deletes)
        if (plan.upserts.isNotEmpty()) upsertEvents(plan.upserts)
    }
}
