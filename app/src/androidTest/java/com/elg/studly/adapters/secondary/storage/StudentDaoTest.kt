package com.elg.studly.adapters.secondary.storage

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StudentDaoTest {
    private lateinit var database: MygesDatabase
    private lateinit var dao: StudentDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MygesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.studentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun syncUpsertsNewEntitiesAndDeletesStaleOnes() = runBlocking {
        syncAll("old", "Old")
        syncAll("new", "Student Updated")

        assertEquals("Student Updated", dao.profile()?.displayName)
        assertEquals(listOf("agenda-new"), dao.agenda().map { it.id })
        assertEquals(listOf("grade-new"), dao.grades().map { it.id })
        assertEquals(listOf("absence-new"), dao.absences().map { it.id })
        assertEquals(listOf("course-new"), dao.courses().map { it.id })
        assertEquals(listOf("project-new"), dao.projects().map { it.id })
        assertEquals(listOf("group-new"), dao.projectGroups().map { it.id })
        assertEquals(listOf("step-new"), dao.projectSteps().map { it.id })
        assertEquals(listOf("practical-new"), dao.practicals().map { it.id })
        assertEquals(listOf("document-new"), dao.documents().map { it.id })
        assertEquals(listOf("person-new"), dao.directoryPeople().map { it.id })
        assertEquals(listOf("news-new"), dao.news().map { it.id })
    }

    @Test
    fun clearAllRemovesEveryCachedTable() = runBlocking {
        syncAll("1", "One")

        dao.clearAll()

        assertEquals(null, dao.profile())
        assertEquals(emptyList<AgendaEventEntity>(), dao.agenda())
        assertEquals(emptyList<GradeEntity>(), dao.grades())
        assertEquals(emptyList<AbsenceEntity>(), dao.absences())
        assertEquals(emptyList<CourseEntity>(), dao.courses())
        assertEquals(emptyList<ProjectEntity>(), dao.projects())
        assertEquals(emptyList<ProjectGroupEntity>(), dao.projectGroups())
        assertEquals(emptyList<ProjectStepEntity>(), dao.projectSteps())
        assertEquals(emptyList<PracticalEntity>(), dao.practicals())
        assertEquals(emptyList<AcademicDocumentEntity>(), dao.documents())
        assertEquals(emptyList<DirectoryPersonEntity>(), dao.directoryPeople())
        assertEquals(emptyList<NewsEntity>(), dao.news())
    }

    private suspend fun syncAll(suffix: String, title: String) {
        dao.syncProfile(profile("student-1", title))
        dao.syncAgenda(listOf(agenda("agenda-$suffix", title)))
        dao.syncGrades(listOf(grade("grade-$suffix", title)))
        dao.syncAbsences(listOf(absence("absence-$suffix", title)))
        dao.syncCourses(listOf(course("course-$suffix", title)))
        dao.syncProjectsAndPracticals(
            projects = listOf(project("project-$suffix", title)),
            projectGroups = listOf(projectGroup("project-$suffix", "group-$suffix", title)),
            projectSteps = listOf(projectStep("project-$suffix", "step-$suffix", title)),
            practicals = listOf(practical("practical-$suffix", title))
        )
        dao.syncDocuments(listOf(document("document-$suffix", title)))
        dao.syncDirectory(listOf(directoryPerson("person-$suffix", title)))
        dao.syncNews(listOf(news("news-$suffix", title)))
    }

    private fun profile(id: String, name: String) = StudentProfileEntity(id, name, null, null, null, null, null)
    private fun agenda(id: String, title: String) = AgendaEventEntity(id, title, 1L, 2L, null, null, null, null, null)
    private fun grade(id: String, title: String) = GradeEntity(id, title, title, 10.0, 20.0, null, null, null, null)
    private fun absence(id: String, title: String) = AbsenceEntity(id, title, 1L, 2L, false, null, null)
    private fun course(id: String, title: String) = CourseEntity(id, title, null, null, null, null, 0, null)
    private fun project(id: String, title: String) = ProjectEntity(id, title, null, null, null, null, 0, null, null)
    private fun projectGroup(projectId: String, id: String, title: String) = ProjectGroupEntity(projectId, id, title, "", false)
    private fun projectStep(projectId: String, id: String, title: String) = ProjectStepEntity(projectId, id, title, null, null)
    private fun practical(id: String, title: String) = PracticalEntity(id, title, null, null, null, null, null, null)
    private fun document(id: String, title: String) = AcademicDocumentEntity(id, title, null, null, null, "$title.pdf", null, null, null, null, null)
    private fun directoryPerson(id: String, title: String) = DirectoryPersonEntity(id, title, null, "Student", null, null, null)
    private fun news(id: String, title: String) = NewsEntity(id, title, null, null)
}
