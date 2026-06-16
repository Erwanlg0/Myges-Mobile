package com.elg.studly.adapters.secondary.storage

import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ProjectStep
import com.elg.studly.domain.model.StudentProfile
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

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
    fun newsRoundTripsThroughEntity() {
        val news = NewsItem("news-1", "News", "Body", Instant.parse("2026-06-01T08:00:00Z"))

        assertEquals(news, news.toEntity().toDomain())
    }
}
