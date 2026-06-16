package com.elg.myges.adapters.secondary.storage

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
    fun replaceSyncedDataUpsertsNewEntitiesAndDeletesStaleOnes() = runBlocking {
        dao.replaceSyncedData(
            profile = profile("student-1", "Student One"),
            agenda = listOf(agenda("agenda-old", "Old")),
            grades = listOf(grade("grade-old", "Old")),
            absences = listOf(absence("absence-old", "Old")),
            courses = listOf(course("course-old", "Old")),
            projects = listOf(project("project-old", "Old")),
            projectGroups = listOf(projectGroup("project-old", "group-old", "Old")),
            projectSteps = listOf(projectStep("project-old", "step-old", "Old")),
            practicals = listOf(practical("practical-old", "Old")),
            documents = listOf(document("document-old", "Old")),
            directoryPeople = listOf(directoryPerson("person-old", "Old")),
            news = listOf(news("news-old", "Old"))
        )

        dao.replaceSyncedData(
            profile = profile("student-1", "Student Updated"),
            agenda = listOf(agenda("agenda-new", "New")),
            grades = listOf(grade("grade-new", "New")),
            absences = listOf(absence("absence-new", "New")),
            courses = listOf(course("course-new", "New")),
            projects = listOf(project("project-new", "New")),
            projectGroups = listOf(projectGroup("project-new", "group-new", "New")),
            projectSteps = listOf(projectStep("project-new", "step-new", "New")),
            practicals = listOf(practical("practical-new", "New")),
            documents = listOf(document("document-new", "New")),
            directoryPeople = listOf(directoryPerson("person-new", "New")),
            news = listOf(news("news-new", "New"))
        )

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
        dao.replaceSyncedData(
            profile = profile("student-1", "Student One"),
            agenda = listOf(agenda("agenda-1", "Agenda")),
            grades = listOf(grade("grade-1", "Grade")),
            absences = listOf(absence("absence-1", "Absence")),
            courses = listOf(course("course-1", "Course")),
            projects = listOf(project("project-1", "Project")),
            projectGroups = listOf(projectGroup("project-1", "group-1", "Group")),
            projectSteps = listOf(projectStep("project-1", "step-1", "Step")),
            practicals = listOf(practical("practical-1", "Practical")),
            documents = listOf(document("document-1", "Document")),
            directoryPeople = listOf(directoryPerson("person-1", "Person")),
            news = listOf(news("news-1", "News"))
        )

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
