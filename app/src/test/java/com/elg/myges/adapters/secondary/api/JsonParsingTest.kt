package com.elg.myges.adapters.secondary.api

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class JsonParsingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun profileParsesKordisResultPayload() {
        val profile = json.parseToJsonElement(
            """
            {
              "response_code": 0,
              "result": {
                "uid": 123,
                "student_id": "2023-ESGI-123",
                "firstname": "Erwan",
                "name": "Luce",
                "email": "student@example.com"
              }
            }
            """.trimIndent()
        ).toProfile()

        assertEquals("123", profile.id)
        assertEquals("Erwan Luce", profile.displayName)
        assertEquals("2023", profile.academicYear)
    }

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

    @Test
    fun absencesParseKordisDatePayload() {
        val absences = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "date": 1781222400000,
                  "course_name": "Algorithmique",
                  "justified": false,
                  "type": "Absence"
                }
              ]
            }
            """.trimIndent()
        ).toAbsences()

        assertEquals(1, absences.size)
        assertEquals("Algorithmique", absences.first().courseName)
        assertEquals(Instant.ofEpochMilli(1781222400000), absences.first().startsAt)
        assertEquals("Absence", absences.first().status)
    }

    @Test
    fun documentsParseKordisAnnualDocumentPayload() {
        val documents = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "document_id": 42,
                  "category": "Annuel",
                  "extension": "pdf",
                  "filename": "attestation.pdf",
                  "last_update": 1781222400000,
                  "name": "Attestation"
                }
              ]
            }
            """.trimIndent()
        ).toDocuments()

        assertEquals(1, documents.size)
        assertEquals("42", documents.first().id)
        assertEquals("application/pdf", documents.first().mimeType)
        assertEquals(Instant.ofEpochMilli(1781222400000), documents.first().updatedAt)
    }

    @Test
    fun documentsParseKordisCourseFilePayload() {
        val documents = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "oc_id": 593267,
                  "label": "fiche listes chainées",
                  "extension": ".pdf",
                  "type": "cours",
                  "update_date": 1776702686256,
                  "links": [
                    { "rel": "url", "href": "https://ges-dl.kordis.fr/private/file" }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toDocuments()

        assertEquals(1, documents.size)
        assertEquals("593267", documents.first().id)
        assertEquals("fiche listes chainées", documents.first().title)
        assertEquals("application/pdf", documents.first().mimeType)
        assertEquals("https://ges-dl.kordis.fr/private/file", documents.first().downloadUrl)
    }

    @Test
    fun projectDocumentsParseEmbeddedKordisProjectFiles() {
        val documents = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "project_id": 22843,
                  "name": "Projet final ReactJS",
                  "year": 2025,
                  "project_files": [
                    {
                      "pf_id": 20788,
                      "pf_title": "Consignes du projet",
                      "pf_file": "consignes.md",
                      "pf_crea_date": 1774653214127
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toProjectDocuments()

        assertEquals(1, documents.size)
        assertEquals("20788", documents.first().id)
        assertEquals("Consignes du projet", documents.first().title)
        assertEquals("2025", documents.first().year)
        assertEquals("consignes.md", documents.first().fileName)
    }

    @Test
    fun projectsParseKordisStepDeadlinesAndFiles() {
        val projects = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "project_id": 22843,
                  "name": "Projet final ReactJS",
                  "course_name": "React",
                  "steps": [
                    {
                      "psp_id": 778,
                      "psp_desc": "Rendu final",
                      "psp_limit_date": 1774653214127,
                      "files": [
                        {
                          "psf_id": 991,
                          "psf_name": "rendu.zip",
                          "psf_file_type": "zip",
                          "psf_end_upload": 1774653214127
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toProjects()

        assertEquals(1, projects.size)
        assertEquals(Instant.ofEpochMilli(1774653214127), projects.first().deadline)
        assertEquals("778", projects.first().steps.first().id)
        assertEquals("Rendu final", projects.first().steps.first().title)
        assertEquals(1, projects.first().fileCount)
    }

    @Test
    fun projectDocumentsParseKordisStepFiles() {
        val documents = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "project_id": 22843,
                  "name": "Projet final ReactJS",
                  "year": 2025,
                  "steps": [
                    {
                      "psp_desc": "Rendu final",
                      "files": [
                        {
                          "psf_id": 991,
                          "psf_name": "rendu.zip",
                          "psf_file_type": "application/zip",
                          "psf_end_upload": 1774653214127,
                          "links": [
                            { "rel": "url", "href": "https://ges-dl.kordis.fr/private/step-file" }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toProjectDocuments()

        assertEquals(1, documents.size)
        assertEquals("991", documents.first().id)
        assertEquals("Rendu final", documents.first().title)
        assertEquals("rendu.zip", documents.first().fileName)
        assertEquals("application/zip", documents.first().mimeType)
        assertEquals("https://ges-dl.kordis.fr/private/step-file", documents.first().downloadUrl)
        assertEquals(Instant.ofEpochMilli(1774653214127), documents.first().updatedAt)
    }

    @Test
    fun practicalsParseKordisStepDates() {
        val practicals = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "project_id": 22843,
                  "name": "TP Android",
                  "course_name": "Mobile",
                  "project_create_date": 1774000000000,
                  "steps": [
                    {
                      "psp_limit_date": 1774653214127
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toPracticals()

        assertEquals(1, practicals.size)
        assertEquals(Instant.ofEpochMilli(1774000000000), practicals.first().startsAt)
        assertEquals(Instant.ofEpochMilli(1774653214127), practicals.first().endsAt)
    }

    @Test
    fun newsParsesKordisPaginatedContentPayload() {
        val news = json.parseToJsonElement(
            """
            {
              "result": {
                "content": [
                  {
                    "ne_id": 12,
                    "title": "Actualité",
                    "summary": "Résumé",
                    "date": 1781222400000
                  }
                ]
              }
            }
            """.trimIndent()
        ).toNews()

        assertEquals(1, news.size)
        assertEquals("12", news.first().id)
        assertEquals("Résumé", news.first().body)
        assertEquals(Instant.ofEpochMilli(1781222400000), news.first().publishedAt)
    }
}
