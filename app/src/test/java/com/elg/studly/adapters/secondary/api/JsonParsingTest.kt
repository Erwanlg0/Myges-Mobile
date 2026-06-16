package com.elg.studly.adapters.secondary.api

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
                "email": "student@example.com",
                "_links": {
                  "photo": {
                    "href": "me/profile/photo"
                  }
                }
              }
            }
            """.trimIndent()
        ).toProfile()

        assertEquals("123", profile.id)
        assertEquals("Erwan Luce", profile.displayName)
        assertEquals("2023, 2024, 2025, 2026", profile.academicYear)
        assertEquals("me/profile/photo", profile.avatarUrl)
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
    fun yearsParseTrimesterYearsPayload() {
        val years = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "school_name": "ESGI",
                  "tri_describe": "Semestre 1",
                  "tri_id": 21,
                  "tri_name": "S1",
                  "year": 2026
                },
                {
                  "school_name": "ESGI",
                  "tri_describe": "Semestre 2",
                  "tri_id": 22,
                  "tri_name": "S2",
                  "year": 2026
                }
              ]
            }
            """.trimIndent()
        ).toYears()

        assertEquals(listOf("2026"), years)
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
                    { "name": "A101", "campus": "ERARD" }
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
        assertEquals("19-21 rue Erard, 75011 Paris", events.first().address)
        assertEquals("4", events.first().colorId)
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

        assertEquals(3, grades.size)
        
        // Main Card
        val main = grades[0]
        assertEquals("456", main.id)
        assertEquals("Algorithmique", main.courseName)
        assertEquals("", main.subject)
        assertEquals(14.25, main.value ?: 0.0, 0.0)
        assertEquals(2.0, main.coefficient ?: 0.0, 0.0)

        // CC 1 Component
        val cc1 = grades[1]
        assertEquals("456-cc-0", cc1.id)
        assertEquals("CC1", cc1.subject)
        assertEquals(16.5, cc1.value ?: 0.0, 0.0)

        // CC 2 Component
        val cc2 = grades[2]
        assertEquals("456-cc-1", cc2.id)
        assertEquals("CC2", cc2.subject)
        assertEquals(12.0, cc2.value ?: 0.0, 0.0)
    }

    @Test
    fun gradesAverageCombinesCcAverageAndExamWhenBothExist() {
        val grades = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "rc_id": 456,
                  "course": "Algorithmique",
                  "grades": [15, 5],
                  "exam": 15,
                  "trimester_name": "Semestre 1",
                  "year": 2025
                }
              ]
            }
            """.trimIndent()
        ).toGrades("2025")

        assertEquals(4, grades.size)
        assertEquals(12.5, grades[0].value ?: 0.0, 0.0)
        assertEquals("456-exam", grades[3].id)
        assertEquals(15.0, grades[3].value ?: 0.0, 0.0)
    }

    @Test
    fun gradesParseSingleCourseObjectNotWrappedInArray() {
        val grades = json.parseToJsonElement(
            """
            {
              "rc_id": 789,
              "course": "Maths",
              "grades": [20, 18],
              "exam": 5,
              "trimester_name": "Semestre 1"
            }
            """.trimIndent()
        ).toGrades()

        // Main + CC1 + CC2 + Examen
        assertEquals(4, grades.size)
        assertEquals("", grades[0].subject)
        assertEquals("CC1", grades[1].subject)
        assertEquals(20.0, grades[1].value ?: 0.0, 0.0)
        assertEquals("CC2", grades[2].subject)
        assertEquals(18.0, grades[2].value ?: 0.0, 0.0)
        assertEquals("Examen", grades[3].subject)
        assertEquals(5.0, grades[3].value ?: 0.0, 0.0)
    }

    @Test
    fun gradesParseKordisCourseWithNoGrades() {
        val grades = json.parseToJsonElement(
            """
            {
              "response_code": 200,
              "result": [
                {
                  "rc_id": 328196,
                  "course": "B1 - nosql, application aux graphes avec graphql",
                  "grades": [],
                  "exam": null,
                  "coef": "2.0",
                  "trimester_name": "Semestre 1"
                }
              ]
            }
            """.trimIndent()
        ).toGrades()

        assertEquals(1, grades.size)
        val main = grades[0]
        assertEquals("328196", main.id)
        assertEquals("B1 - nosql, application aux graphes avec graphql", main.courseName)
        assertEquals(null, main.value)
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
        assertEquals("attestation.pdf", documents.first().fileName)
        assertEquals("application/pdf", documents.first().mimeType)
        assertEquals(Instant.ofEpochMilli(1781222400000), documents.first().updatedAt)
    }

    @Test
    fun syllabusParsesKordisPayloadIntoReadableText() {
        val syllabus = json.parseToJsonElement(
            """
            {
              "result": {
                "syllabus_name": "Algorithmique",
                "teaching_goals": "Comprendre les structures de données",
                "detail_plan": "Listes chaînées",
                "skills": "Analyser un algorithme",
                "evaluation_type": "Projet"
              }
            }
            """.trimIndent()
        ).toCourseSyllabus()

        checkNotNull(syllabus)
        assertTrue(syllabus.contains("Algorithmique"))
        assertTrue(syllabus.contains("Comprendre les structures de données"))
        assertTrue(syllabus.contains("Listes chaînées"))
        assertTrue(syllabus.contains("Projet"))
    }

    @Test
    fun syllabusParsesKordisArrayPayloadIntoReadableText() {
        val syllabus = json.parseToJsonElement(
            """
            {
              "result": [{
                "syllabus_name": "Scripting Python",
                "teaching_goals": "Learn Python",
                "skills": [{"comp_label": "Optimize software"}],
                "control_types": [{"evaluation_label": "Continuous assessment"}],
                "seance_details": [{"content": "Functions and classes"}]
              }]
            }
            """.trimIndent()
        ).toCourseSyllabus()

        checkNotNull(syllabus)
        assertTrue(syllabus.contains("Scripting Python"))
        assertTrue(syllabus.contains("Learn Python"))
        assertTrue(syllabus.contains("Optimize software"))
        assertTrue(syllabus.contains("Continuous assessment"))
        assertTrue(syllabus.contains("Functions and classes"))
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
        assertEquals("fiche listes chainées.pdf", documents.first().fileName)
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
        assertEquals("me/projectFiles/20788", documents.first().downloadUrl)
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
                  "groups": [
                    {
                      "group_name": "Groupe 4"
                    }
                  ],
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
        assertEquals("Groupe 4", projects.first().groupName)
        assertEquals(Instant.ofEpochMilli(1774653214127), projects.first().deadline)
        assertEquals("778", projects.first().steps.first().id)
        assertEquals("Rendu final", projects.first().steps.first().title)
        assertEquals(1, projects.first().fileCount)
    }

    @Test
    fun projectsDeadlineIsMaximumOfStepDeadlines() {
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
                      "psp_desc": "Etape 1",
                      "psp_limit_date": 1774653214127
                    },
                    {
                      "psp_id": 779,
                      "psp_desc": "Etape 2",
                      "psp_limit_date": 1774653214128
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toProjects()

        assertEquals(1, projects.size)
        assertEquals(Instant.ofEpochMilli(1774653214128), projects.first().deadline)
    }

    @Test
    fun nextProjectStepsParseKordisPayloadAsProjects() {
        val projects = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "pro_name": "Projet final ReactJS",
                  "pro_id": 22843,
                  "psp_desc": "Rendu final",
                  "psp_type": "Livrable",
                  "group_id": 3321,
                  "psp_id": 778,
                  "type": "project",
                  "course_name": "React",
                  "psp_limit_date": 1774653214127
                }
              ]
            }
            """.trimIndent()
        ).toNextProjectStepProjects()

        assertEquals(1, projects.size)
        assertEquals("22843", projects.first().id)
        assertEquals("Projet final ReactJS", projects.first().name)
        assertEquals("React", projects.first().courseName)
        assertEquals("3321", projects.first().groupName)
        assertEquals(Instant.ofEpochMilli(1774653214127), projects.first().deadline)
        assertEquals("778", projects.first().steps.first().id)
        assertEquals("Rendu final", projects.first().steps.first().title)
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
        assertEquals("rendu.zip", documents.first().title)
        assertEquals("rendu.zip", documents.first().fileName)
        assertEquals("application/zip", documents.first().mimeType)
        assertEquals("https://ges-dl.kordis.fr/private/step-file", documents.first().downloadUrl)
        assertEquals(Instant.ofEpochMilli(1774653214127), documents.first().updatedAt)
    }

    @Test
    fun projectStepDocumentsUseEndpointWhenLinkIsMissing() {
        val documents = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "project_id": 22843,
                  "steps": [
                    {
                      "psp_desc": "Rendu final",
                      "files": [
                        {
                          "psf_id": 991,
                          "psf_name": "rendu.zip"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toProjectDocuments()

        assertEquals("me/projectStepFiles/991", documents.first().downloadUrl)
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
    @Test
    fun newsParsesSingleMinimumVersionPayload() {
        val news = json.parseToJsonElement(
            """
            {
              "result": {
                "type": "skolae_app_version",
                "label": "Version minimum requise",
                "value": "3.5.0"
              }
            }
            """.trimIndent()
        ).toNews()

        assertEquals(1, news.size)
        assertEquals("skolae_app_version", news.first().id)
        assertEquals("Version minimum requise", news.first().title)
        assertEquals("3.5.0", news.first().body)
    }

    @Test
    fun newsParsesSpeedMeetingAppointmentsAsStudentNews() {
        val news = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "appointment_start": 1781222400000,
                  "appointment_end": 1781226000000,
                  "corporate_name": "KEMEO",
                  "location": "ONLINE",
                  "organizer": "ESGI",
                  "ss_id": 134062,
                  "title": "Speed meeting",
                  "offers": [
                    {
                      "offer": "DÃ©veloppeur PHP h/f",
                      "contract": "Contrat d'apprentissage"
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toNews()

        assertEquals(1, news.size)
        assertEquals("Speed meeting", news.first().title)
        assertEquals(Instant.ofEpochMilli(1781222400000), news.first().publishedAt)
        assertTrue(news.first().body.orEmpty().contains("KEMEO"))
        assertTrue(news.first().body.orEmpty().contains("DÃ©veloppeur PHP h/f"))
    }

    @Test
    fun newsParsesPartnersPayloadAsStudentNews() {
        val news = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "partner_id": 1,
                  "name": "Microsoft Campus",
                  "content": "Offres campus",
                  "link": "https://example.com"
                }
              ]
            }
            """.trimIndent()
        ).toNews()

        assertEquals(1, news.size)
        assertEquals("Microsoft Campus", news.first().title)
        assertEquals("Offres campus", news.first().body)
    }
}
