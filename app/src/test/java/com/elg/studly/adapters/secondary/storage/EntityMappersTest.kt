package com.elg.studly.adapters.secondary.storage

import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.DirectoryRole
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ProjectGroup
import com.elg.studly.domain.model.ProjectStep
import com.elg.studly.domain.model.StudentEvent
import com.elg.studly.domain.model.StudentProfile
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant
import com.elg.studly.adapters.time.*
import kotlinx.datetime.LocalDate

class EntityMappersTest {
    @Test
    fun profileRoundTripsThroughEntity() {
        val profile = StudentProfile("student-1", "Student One", "student@example.com", "Paris", "B3", "2026", "https://avatar")

        assertEquals(profile, profile.toEntity().toDomain())
    }

    @Test
    fun agendaEventRoundTripsThroughEntity() {
        val event = AgendaEvent(
            id = "agenda-1",
            title = "Algorithms",
            startsAt = Instant.parse("2026-06-12T08:00:00Z"),
            endsAt = Instant.parse("2026-06-12T10:00:00Z"),
            room = "A101",
            teacher = "Teacher",
            type = "Course",
            modality = "Présentiel",
            courseId = "course-1"
        )

        assertEquals(event, event.toEntity().toDomain())
    }

    @Test
    fun gradeRoundTripsThroughEntity() {
        val grade = Grade(
            id = "grade-1",
            courseName = "Algorithms",
            subject = "Exam",
            value = 15.5,
            scale = 20.0,
            coefficient = 2.0,
            average = 13.2,
            date = LocalDate.parse("2026-06-10"),
            period = "S2"
        )

        assertEquals(grade, grade.toEntity().toDomain())
    }

    @Test
    fun gradeEntityHandlesNullDateAndToeicScale() {
        val grade = GradeEntity("toeic", "English", "TOEIC", 950.0, 20.0, 1.0, null, null, null).toDomain()

        assertEquals(null, Grade("grade-1", "Course", "Exam", null, null, null, null, null, null).toEntity().dateIso)
        assertEquals(null, grade.date)
        assertEquals(990.0, grade.scale ?: 0.0, 0.0)
    }

    @Test
    fun absenceRoundTripsThroughEntity() {
        val absence = Absence(
            id = "absence-1",
            courseName = "Algorithms",
            startsAt = Instant.parse("2026-06-11T08:00:00Z"),
            endsAt = Instant.parse("2026-06-11T10:00:00Z"),
            justified = false,
            status = "pending",
            reason = "reason"
        )

        assertEquals(absence, absence.toEntity().toDomain())
    }

    @Test
    fun courseRoundTripsThroughEntity() {
        val course = Course("course-1", "Algorithms", "Teacher", "2026", "S2", "Syllabus", 3)

        assertEquals(course, course.toEntity().toDomain())
    }

    @Test
    fun projectRoundTripsThroughEntityWithSteps() {
        val project = Project(
            id = "project-1",
            name = "Project",
            courseName = "Algorithms",
            groupName = "Group 1",
            status = "open",
            deadline = Instant.parse("2026-06-30T23:59:00Z"),
            steps = listOf(
                ProjectStep("step-1", "Submit", Instant.parse("2026-06-30T23:59:00Z"), "todo")
            ),
            fileCount = 2
        )

        assertEquals(project, project.toEntity().toDomain(project.toStepEntities()))
    }

    @Test
    fun projectRoundTripsThroughEntityWithGroups() {
        val project = Project(
            id = "project-1",
            name = "Project",
            courseName = "Algorithms",
            groupName = "Group 1",
            status = "open",
            deadline = null,
            steps = emptyList(),
            fileCount = 2,
            groups = listOf(ProjectGroup("group-1", "Group 1", listOf("Alice", "Bob"), true)),
            startsAt = Instant.parse("2026-06-01T08:00:00Z")
        )

        assertEquals(project, project.toEntity().toDomain(project.toStepEntities(), project.toGroupEntities()))
    }

    @Test
    fun projectEntityHandlesNullDatesAndBlankGroupStudents() {
        val project = Project(
            id = "project-1",
            name = "Project",
            courseName = null,
            groupName = null,
            status = null,
            deadline = null,
            steps = listOf(ProjectStep("step-1", "Todo", null, null)),
            fileCount = 0,
            groups = listOf(ProjectGroup("group-1", "Group", listOf("Alice", "", "Bob"), false)),
            startsAt = null
        )

        val entity = project.toEntity()
        val stepEntity = project.toStepEntities().first()
        val groupEntity = ProjectGroupEntity("project-1", "group-1", "Group", "Alice\n\nBob", false)
        val domain = entity.toDomain(listOf(stepEntity), listOf(groupEntity))

        assertEquals(null, entity.deadlineEpochMillis)
        assertEquals(null, entity.startsAtEpochMillis)
        assertEquals(null, stepEntity.deadlineEpochMillis)
        assertEquals(listOf("Alice", "Bob"), domain.groups.first().students)
    }

    @Test
    fun practicalRoundTripsThroughEntity() {
        val practical = Practical(
            id = "practical-1",
            name = "Lab",
            courseName = "Algorithms",
            startsAt = Instant.parse("2026-06-13T08:00:00Z"),
            endsAt = Instant.parse("2026-06-13T10:00:00Z"),
            room = "B201",
            status = "open"
        )

        assertEquals(practical, practical.toEntity().toDomain())
    }

    @Test
    fun practicalRoundTripsThroughEntityWithStepsAndGroups() {
        val practical = Practical(
            id = "practical-1",
            name = "Lab",
            courseName = "Algorithms",
            startsAt = null,
            endsAt = null,
            room = null,
            status = "open",
            groups = listOf(ProjectGroup("group-1", "Group 1", listOf("Alice", "Bob"), false)),
            steps = listOf(ProjectStep("step-1", "Submit", Instant.parse("2026-06-30T23:59:00Z"), "todo"))
        )

        assertEquals(practical, practical.toEntity().toDomain(practical.toStepEntities(), practical.toGroupEntities()))
    }

    @Test
    fun practicalEntityHandlesNullDatesAndStepDeadlines() {
        val practical = Practical(
            id = "practical-1",
            name = "Lab",
            courseName = null,
            startsAt = null,
            endsAt = null,
            room = null,
            status = null,
            steps = listOf(ProjectStep("step-1", "Todo", null, null))
        )

        assertEquals(practical, practical.toEntity().toDomain(practical.toStepEntities(), practical.toGroupEntities()))
        assertEquals(null, practical.toStepEntities().first().deadlineEpochMillis)
    }

    @Test
    fun documentRoundTripsThroughEntity() {
        val document = AcademicDocument(
            id = "document-1",
            title = "Certificate",
            category = "annual",
            year = "2026",
            mimeType = "application/pdf",
            fileName = "certificate.pdf",
            downloadUrl = "https://example.com/certificate.pdf",
            updatedAt = Instant.parse("2026-06-01T12:00:00Z")
        )

        assertEquals(document, document.toEntity().toDomain())
    }

    @Test
    fun documentEntityHandlesNullUpdatedAt() {
        val document = AcademicDocument("document-1", "Doc", null, null, null, "doc.pdf", null, null)

        assertEquals(document, document.toEntity().toDomain())
    }

    @Test
    fun directoryPersonRoundTripsThroughEntity() {
        val person = DirectoryPerson("person-1", "Alice", "alice@example.com", DirectoryRole.Teacher, "2026", "B3", "https://avatar")

        assertEquals(person, person.toEntity().toDomain())
    }

    @Test
    fun directoryPersonFallsBackToStudentRole() {
        val entity = DirectoryPersonEntity("person-1", "Alice", null, "unknown", null, null, null)

        assertEquals(DirectoryRole.Student, entity.toDomain().role)
    }

    @Test
    fun newsRoundTripsThroughEntity() {
        val news = NewsItem("news-1", "News", "Body", Instant.parse("2026-06-01T08:00:00Z"))

        assertEquals(news, news.toEntity().toDomain())
    }

    @Test
    fun newsEntityHandlesNullPublishedAt() {
        val news = NewsItem("news-1", "News", null, null)

        assertEquals(news, news.toEntity().toDomain())
    }

    @Test
    fun eventRoundTripsThroughEntity() {
        val event = StudentEvent(
            id = "event-1",
            title = "Conference",
            type = "event",
            location = "Paris",
            organizer = "School",
            description = "Talk",
            date = Instant.parse("2026-06-01T08:00:00Z"),
            subscriptionStart = Instant.parse("2026-05-01T08:00:00Z"),
            subscriptionEnd = Instant.parse("2026-05-30T08:00:00Z"),
            subscribed = true,
            detailUrl = "https://example.com"
        )

        assertEquals(event, event.toEntity().toDomain())
    }

    @Test
    fun eventEntityHandlesNullDates() {
        val event = StudentEvent("event-1", "Event", null, null, null, null, null, null, null, false)

        assertEquals(event, event.toEntity().toDomain())
    }

    @Test
    fun entityDefaultsAndFieldsAreExposed() {
        val group = ProjectGroupEntity("project-1", "group-1", "Group", "Alice", true)
        val practical = PracticalEntity("practical-1", "Lab", null, null, null, null, null, null)
        val event = StudentEventEntity("event-1", "Event", null, null, null, null, null, null, null, false)
        val news = NewsEntity("news-1", "News", null, null)

        assertEquals("project-1", group.projectId)
        assertEquals(null, practical.courseId)
        assertEquals(null, event.detailUrl)
        assertEquals(null, news.html)
        assertEquals(null, news.imageUrl)
    }
}
