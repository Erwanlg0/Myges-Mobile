package com.elg.studly.adapters.secondary.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentDaoTest {
    @Test
    fun clearAllClearsEveryTable() = runTest {
        val dao = TestStudentDao().apply { seedAll("old") }

        dao.clearAll()

        assertNull(dao.profileRow)
        assertTrue(dao.agendaRows.isEmpty())
        assertTrue(dao.gradeRows.isEmpty())
        assertTrue(dao.absenceRows.isEmpty())
        assertTrue(dao.courseRows.isEmpty())
        assertTrue(dao.projectRows.isEmpty())
        assertTrue(dao.projectGroupRows.isEmpty())
        assertTrue(dao.projectStepRows.isEmpty())
        assertTrue(dao.practicalRows.isEmpty())
        assertTrue(dao.documentRows.isEmpty())
        assertTrue(dao.directoryRows.isEmpty())
        assertTrue(dao.newsRows.isEmpty())
        assertTrue(dao.eventRows.isEmpty())
    }

    @Test
    fun syncKeepsUnchangedRows() = runTest {
        val dao = TestStudentDao().apply { seedAll("same") }

        dao.syncProfile(profile("same"))
        dao.syncAgenda(dao.agendaRows)
        dao.syncGrades(dao.gradeRows)
        dao.syncAbsences(dao.absenceRows)
        dao.syncCourses(dao.courseRows)
        dao.syncProjectsAndPracticals(
            dao.projectRows,
            dao.projectGroupRows,
            dao.projectStepRows,
            dao.practicalRows
        )
        dao.syncDocuments(dao.documentRows)
        dao.syncDirectory(dao.directoryRows)
        dao.syncNews(dao.newsRows)
        dao.syncEvents(dao.eventRows)

        assertEquals("same", dao.profileRow?.id)
        assertEquals(listOf("same"), dao.agendaRows.map { it.id })
        assertEquals(listOf("same"), dao.gradeRows.map { it.id })
        assertEquals(listOf("same"), dao.absenceRows.map { it.id })
        assertEquals(listOf("same"), dao.courseRows.map { it.id })
        assertEquals(listOf("same"), dao.projectRows.map { it.id })
        assertEquals(listOf("same"), dao.projectGroupRows.map { it.id })
        assertEquals(listOf("same"), dao.projectStepRows.map { it.id })
        assertEquals(listOf("same"), dao.practicalRows.map { it.id })
        assertEquals(listOf("same"), dao.documentRows.map { it.id })
        assertEquals(listOf("same"), dao.directoryRows.map { it.id })
        assertEquals(listOf("same"), dao.newsRows.map { it.id })
        assertEquals(listOf("same"), dao.eventRows.map { it.id })
    }

    @Test
    fun syncReplacesChangedRows() = runTest {
        val dao = TestStudentDao().apply { seedAll("old") }

        dao.syncProfile(profile("new"))
        dao.syncAgenda(listOf(agenda("new")))
        dao.syncGrades(listOf(grade("new")))
        dao.syncAbsences(listOf(absence("new")))
        dao.syncCourses(listOf(course("new")))
        dao.syncProjectsAndPracticals(
            listOf(project("new")),
            listOf(projectGroup("new")),
            listOf(projectStep("new")),
            listOf(practical("new"))
        )
        dao.syncDocuments(listOf(document("new")))
        dao.syncDirectory(listOf(directoryPerson("new")))
        dao.syncNews(listOf(news("new")))
        dao.syncEvents(listOf(studentEvent("new")))

        assertEquals("new", dao.profileRow?.id)
        assertEquals(listOf("new"), dao.agendaRows.map { it.id })
        assertEquals(listOf("new"), dao.gradeRows.map { it.id })
        assertEquals(listOf("new"), dao.absenceRows.map { it.id })
        assertEquals(listOf("new"), dao.courseRows.map { it.id })
        assertEquals(listOf("new"), dao.projectRows.map { it.id })
        assertEquals(listOf("new"), dao.projectGroupRows.map { it.id })
        assertEquals(listOf("new"), dao.projectStepRows.map { it.id })
        assertEquals(listOf("new"), dao.practicalRows.map { it.id })
        assertEquals(listOf("new"), dao.documentRows.map { it.id })
        assertEquals(listOf("new"), dao.directoryRows.map { it.id })
        assertEquals(listOf("new"), dao.newsRows.map { it.id })
        assertEquals(listOf("new"), dao.eventRows.map { it.id })
    }

    @Test
    fun replaceGroupsForProjectOnlyDeletesTargetProjectGroups() = runTest {
        val dao = TestStudentDao().apply {
            projectGroupRows = listOf(
                projectGroup("old", projectId = "target"),
                projectGroup("kept", projectId = "other")
            )
        }

        dao.replaceGroupsForProject("target", listOf(projectGroup("new", projectId = "target")))

        assertEquals(listOf("kept", "new"), dao.projectGroupRows.map { it.id })
    }
}

private class TestStudentDao : StudentDao() {
    var profileRow: StudentProfileEntity? = null
    var agendaRows = emptyList<AgendaEventEntity>()
    var gradeRows = emptyList<GradeEntity>()
    var absenceRows = emptyList<AbsenceEntity>()
    var courseRows = emptyList<CourseEntity>()
    var projectRows = emptyList<ProjectEntity>()
    var projectGroupRows = emptyList<ProjectGroupEntity>()
    var projectStepRows = emptyList<ProjectStepEntity>()
    var practicalRows = emptyList<PracticalEntity>()
    var documentRows = emptyList<AcademicDocumentEntity>()
    var directoryRows = emptyList<DirectoryPersonEntity>()
    var newsRows = emptyList<NewsEntity>()
    var eventRows = emptyList<StudentEventEntity>()

    fun seedAll(id: String) {
        profileRow = profile(id)
        agendaRows = listOf(agenda(id))
        gradeRows = listOf(grade(id))
        absenceRows = listOf(absence(id))
        courseRows = listOf(course(id))
        projectRows = listOf(project(id))
        projectGroupRows = listOf(projectGroup(id))
        projectStepRows = listOf(projectStep(id))
        practicalRows = listOf(practical(id))
        documentRows = listOf(document(id))
        directoryRows = listOf(directoryPerson(id))
        newsRows = listOf(news(id))
        eventRows = listOf(studentEvent(id))
    }

    override fun observeProfile(): Flow<StudentProfileEntity?> = flowOf(profileRow)
    override fun observeAgenda(): Flow<List<AgendaEventEntity>> = flowOf(agendaRows)
    override fun observeGrades(): Flow<List<GradeEntity>> = flowOf(gradeRows)
    override fun observeAbsences(): Flow<List<AbsenceEntity>> = flowOf(absenceRows)
    override fun observeCourses(): Flow<List<CourseEntity>> = flowOf(courseRows)
    override fun observeProjects(): Flow<List<ProjectEntity>> = flowOf(projectRows)
    override fun observeProjectSteps(): Flow<List<ProjectStepEntity>> = flowOf(projectStepRows)
    override fun observeProjectGroups(): Flow<List<ProjectGroupEntity>> = flowOf(projectGroupRows)
    override fun observePracticals(): Flow<List<PracticalEntity>> = flowOf(practicalRows)
    override fun observeDocuments(): Flow<List<AcademicDocumentEntity>> = flowOf(documentRows)
    override fun observeDirectory(): Flow<List<DirectoryPersonEntity>> = flowOf(directoryRows)
    override fun observeNews(): Flow<List<NewsEntity>> = flowOf(newsRows)
    override fun observeEvents(): Flow<List<StudentEventEntity>> = flowOf(eventRows)
    override suspend fun documentInlineContent(id: String): String? = documentRows.firstOrNull { it.id == id }?.inlineContent
    override suspend fun agendaIds(): List<String> = agendaRows.map { it.id }
    override suspend fun gradeIds(): List<String> = gradeRows.map { it.id }
    override suspend fun absenceIds(): List<String> = absenceRows.map { it.id }
    override suspend fun projectIds(): List<String> = projectRows.map { it.id }
    override suspend fun documentIds(): List<String> = documentRows.map { it.id }
    override suspend fun profile(): StudentProfileEntity? = profileRow
    override suspend fun agenda(): List<AgendaEventEntity> = agendaRows
    override suspend fun grades(): List<GradeEntity> = gradeRows
    override suspend fun absences(): List<AbsenceEntity> = absenceRows
    override suspend fun courses(): List<CourseEntity> = courseRows
    override suspend fun projects(): List<ProjectEntity> = projectRows
    override suspend fun projectSteps(): List<ProjectStepEntity> = projectStepRows
    override suspend fun projectGroups(): List<ProjectGroupEntity> = projectGroupRows
    override suspend fun practicals(): List<PracticalEntity> = practicalRows
    override suspend fun documents(): List<AcademicDocumentEntity> = documentRows
    override suspend fun directoryPeople(): List<DirectoryPersonEntity> = directoryRows
    override suspend fun news(): List<NewsEntity> = newsRows
    override suspend fun events(): List<StudentEventEntity> = eventRows
    override suspend fun updateEventSubscribed(eventId: String, subscribed: Boolean) {
        eventRows = eventRows.map { if (it.id == eventId) it.copy(subscribed = subscribed) else it }
    }
    override suspend fun upsertProfile(profile: StudentProfileEntity) {
        profileRow = profile
    }
    override suspend fun upsertAgenda(events: List<AgendaEventEntity>) {
        agendaRows = upsert(agendaRows, events, AgendaEventEntity::id)
    }
    override suspend fun upsertGrades(grades: List<GradeEntity>) {
        gradeRows = upsert(gradeRows, grades, GradeEntity::id)
    }
    override suspend fun upsertAbsences(absences: List<AbsenceEntity>) {
        absenceRows = upsert(absenceRows, absences, AbsenceEntity::id)
    }
    override suspend fun upsertCourses(courses: List<CourseEntity>) {
        courseRows = upsert(courseRows, courses, CourseEntity::id)
    }
    override suspend fun upsertProjects(projects: List<ProjectEntity>) {
        projectRows = upsert(projectRows, projects, ProjectEntity::id)
    }
    override suspend fun upsertProjectSteps(steps: List<ProjectStepEntity>) {
        projectStepRows = upsert(projectStepRows, steps) { it.projectId to it.id }
    }
    override suspend fun upsertProjectGroups(groups: List<ProjectGroupEntity>) {
        projectGroupRows = upsert(projectGroupRows, groups) { it.projectId to it.id }
    }
    override suspend fun deleteGroupsForProject(projectId: String) {
        projectGroupRows = projectGroupRows.filterNot { it.projectId == projectId }
    }
    override suspend fun upsertPracticals(practicals: List<PracticalEntity>) {
        practicalRows = upsert(practicalRows, practicals, PracticalEntity::id)
    }
    override suspend fun upsertDocuments(documents: List<AcademicDocumentEntity>) {
        documentRows = upsert(documentRows, documents, AcademicDocumentEntity::id)
    }
    override suspend fun upsertDirectoryPeople(people: List<DirectoryPersonEntity>) {
        directoryRows = upsert(directoryRows, people, DirectoryPersonEntity::id)
    }
    override suspend fun upsertNews(news: List<NewsEntity>) {
        newsRows = upsert(newsRows, news, NewsEntity::id)
    }
    override suspend fun upsertEvents(events: List<StudentEventEntity>) {
        eventRows = upsert(eventRows, events, StudentEventEntity::id)
    }
    override suspend fun deleteAgenda(events: List<AgendaEventEntity>) {
        agendaRows = delete(agendaRows, events, AgendaEventEntity::id)
    }
    override suspend fun deleteGrades(grades: List<GradeEntity>) {
        gradeRows = delete(gradeRows, grades, GradeEntity::id)
    }
    override suspend fun deleteAbsences(absences: List<AbsenceEntity>) {
        absenceRows = delete(absenceRows, absences, AbsenceEntity::id)
    }
    override suspend fun deleteCourses(courses: List<CourseEntity>) {
        courseRows = delete(courseRows, courses, CourseEntity::id)
    }
    override suspend fun deleteProjects(projects: List<ProjectEntity>) {
        projectRows = delete(projectRows, projects, ProjectEntity::id)
    }
    override suspend fun deleteProjectSteps(steps: List<ProjectStepEntity>) {
        projectStepRows = delete(projectStepRows, steps) { it.projectId to it.id }
    }
    override suspend fun deleteProjectGroups(groups: List<ProjectGroupEntity>) {
        projectGroupRows = delete(projectGroupRows, groups) { it.projectId to it.id }
    }
    override suspend fun deletePracticals(practicals: List<PracticalEntity>) {
        practicalRows = delete(practicalRows, practicals, PracticalEntity::id)
    }
    override suspend fun deleteDocuments(documents: List<AcademicDocumentEntity>) {
        documentRows = delete(documentRows, documents, AcademicDocumentEntity::id)
    }
    override suspend fun deleteDirectoryPeople(people: List<DirectoryPersonEntity>) {
        directoryRows = delete(directoryRows, people, DirectoryPersonEntity::id)
    }
    override suspend fun deleteNews(news: List<NewsEntity>) {
        newsRows = delete(newsRows, news, NewsEntity::id)
    }
    override suspend fun deleteEvents(events: List<StudentEventEntity>) {
        eventRows = delete(eventRows, events, StudentEventEntity::id)
    }
    override suspend fun clearProfile() {
        profileRow = null
    }
    override suspend fun clearAgenda() {
        agendaRows = emptyList()
    }
    override suspend fun clearGrades() {
        gradeRows = emptyList()
    }
    override suspend fun clearAbsences() {
        absenceRows = emptyList()
    }
    override suspend fun clearCourses() {
        courseRows = emptyList()
    }
    override suspend fun clearProjects() {
        projectRows = emptyList()
    }
    override suspend fun clearProjectSteps() {
        projectStepRows = emptyList()
    }
    override suspend fun clearProjectGroups() {
        projectGroupRows = emptyList()
    }
    override suspend fun clearPracticals() {
        practicalRows = emptyList()
    }
    override suspend fun clearDocuments() {
        documentRows = emptyList()
    }
    override suspend fun clearDirectoryPeople() {
        directoryRows = emptyList()
    }
    override suspend fun clearNews() {
        newsRows = emptyList()
    }
    override suspend fun clearEvents() {
        eventRows = emptyList()
    }
}

private fun profile(id: String) = StudentProfileEntity(id, id, null, null, null, null, null)
private fun agenda(id: String) = AgendaEventEntity(id, id, 1L, 2L, null, null, null, null, null)
private fun grade(id: String) = GradeEntity(id, id, id, 10.0, 20.0, null, null, null, null)
private fun absence(id: String) = AbsenceEntity(id, id, 1L, 2L, false, null, null)
private fun course(id: String) = CourseEntity(id, id, null, null, null, null, 0, null)
private fun project(id: String) = ProjectEntity(id, id, null, null, null, null, 0, null, null)
private fun projectGroup(id: String, projectId: String = id) = ProjectGroupEntity(projectId, id, id, "", false)
private fun projectStep(id: String, projectId: String = id) = ProjectStepEntity(projectId, id, id, null, null)
private fun practical(id: String) = PracticalEntity(id, id, null, null, null, null, null, null)
private fun document(id: String) = AcademicDocumentEntity(id, id, null, null, null, "$id.pdf", null, null, null, null, null)
private fun directoryPerson(id: String) = DirectoryPersonEntity(id, id, null, "student", null, null, null)
private fun news(id: String) = NewsEntity(id, id, null, null)
private fun studentEvent(id: String) = StudentEventEntity(id, id, null, null, null, null, null, null, null, false)

private fun <T, K> upsert(current: List<T>, incoming: List<T>, key: (T) -> K): List<T> {
    val incomingKeys = incoming.map(key).toSet()
    return current.filter { key(it) !in incomingKeys } + incoming
}

private fun <T, K> delete(current: List<T>, outgoing: List<T>, key: (T) -> K): List<T> {
    val outgoingKeys = outgoing.map(key).toSet()
    return current.filter { key(it) !in outgoingKeys }
}
