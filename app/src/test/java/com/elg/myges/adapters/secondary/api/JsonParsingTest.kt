package com.elg.myges.adapters.secondary.api

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class JsonParsingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun agendaParsesEpochMillisWindowItems() {
        val events = json.parseToJsonElement(
            """
            {
              "data": [
                {
                  "id": "course-1",
                  "title": "Math",
                  "start": 1781222400000,
                  "end": 1781226000000,
                  "room": "A101",
                  "teacher": "Mme Martin"
                }
              ]
            }
            """.trimIndent()
        ).toAgendaEvents()

        assertEquals(1, events.size)
        assertEquals("course-1", events.first().id)
        assertEquals(Instant.ofEpochMilli(1781222400000), events.first().startsAt)
        assertEquals(Instant.ofEpochMilli(1781226000000), events.first().endsAt)
    }

    @Test
    fun agendaParsesKordisResultPayload() {
        val events = json.parseToJsonElement(
            """
            {
              "response_code": 200,
              "result": [
                {
                  "reservation_id": 123,
                  "name": "Cours",
                  "start_date": 1781222400000,
                  "end_date": 1781226000000,
                  "teacher": "Mme Martin",
                  "modality": "Présentiel",
                  "rooms": [
                    { "name": "A101" }
                  ],
                  "discipline": {
                    "name": "Algorithmique",
                    "rc_id": 456
                  }
                }
              ]
            }
            """.trimIndent()
        ).toAgendaEvents()

        assertEquals(1, events.size)
        assertEquals("123", events.first().id)
        assertEquals("Cours", events.first().title)
        assertEquals("A101", events.first().room)
        assertEquals("456", events.first().courseId)
    }

    @Test
    fun agendaIgnoresItemsWithoutStartDate() {
        val events = json.parseToJsonElement(
            """
            {
              "data": [
                {
                  "id": "broken",
                  "title": "No date"
                }
              ]
            }
            """.trimIndent()
        ).toAgendaEvents()

        assertTrue(events.isEmpty())
    }

    @Test
    fun gradesParseSimpleKordisPayload() {
        val grades = json.parseToJsonElement(
            """
            [
              {
                "id": "grade-1",
                "courseName": "Algorithmique",
                "subject": "Partiel",
                "grade": "16,5",
                "bareme": 20,
                "coef": 2,
                "date": "2026-06-12"
              }
            ]
            """.trimIndent()
        ).toGrades()

        assertEquals(1, grades.size)
        assertEquals("Algorithmique", grades.first().courseName)
        assertEquals(16.5, grades.first().value ?: 0.0, 0.0)
        assertEquals(20.0, grades.first().scale ?: 0.0, 0.0)
        assertEquals(2.0, grades.first().coefficient ?: 0.0, 0.0)
    }

    @Test
    fun gradesParseKordisResultWithNumericNestedGrades() {
        val grades = json.parseToJsonElement(
            """
            {
              "response_code": 200,
              "result": [
                {
                  "rc_id": 456,
                  "course": "Algorithmique",
                  "coef": "2",
                  "ccaverage": 14.5,
                  "trimester_name": "Semestre 1",
                  "grades": [16.5, 12.0]
                }
              ]
            }
            """.trimIndent()
        ).toGrades()

        assertEquals(2, grades.size)
        assertEquals("456-grade-0", grades.first().id)
        assertEquals("Algorithmique", grades.first().courseName)
        assertEquals("Semestre 1", grades.first().subject)
        assertEquals(16.5, grades.first().value ?: 0.0, 0.0)
        assertEquals(2.0, grades.first().coefficient ?: 0.0, 0.0)
        assertEquals(14.5, grades.first().average ?: 0.0, 0.0)
    }
}
