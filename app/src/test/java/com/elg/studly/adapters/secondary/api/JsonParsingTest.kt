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
        
        
        val main = grades[0]
        assertEquals("456", main.id)
        assertEquals("Algorithmique", main.courseName)
        assertEquals("", main.subject)
        assertEquals(14.25, main.value ?: 0.0, 0.0)
        assertEquals(2.0, main.coefficient ?: 0.0, 0.0)

        
        val cc1 = grades[1]
        assertEquals("456-cc-0", cc1.id)
        assertEquals("CC1", cc1.subject)
        assertEquals(16.5, cc1.value ?: 0.0, 0.0)

        
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
    fun gradesParseLetterMarkOnly() {
        val grades = json.parseToJsonElement(
            """
            {
              "response_code": 200,
              "result": [
                {
                  "rc_id": 353221,
                  "course": "T3 - mission entreprise",
                  "grades": [],
                  "exam": null,
                  "coef": "2.0",
                  "trimester_name": "Semestre 3",
                  "letter_mark": "A+"
                }
              ]
            }
            """.trimIndent()
        ).toGrades()

        assertEquals(1, grades.size)
        val main = grades[0]
        assertEquals("353221", main.id)
        assertEquals("T3 - mission entreprise", main.courseName)
        assertEquals(null, main.value)
        assertEquals("A+", main.gradeLetter)
    }

    @Test
    fun gradesParseLetterMarkFWhenNoNumericValue() {
        val grades = json.parseToJsonElement(
            """
            {
              "response_code": 200,
              "result": [
                {
                  "rc_id": 338004,
                  "course": "T1 - programme open lab",
                  "grades": [],
                  "exam": null,
                  "coef": "2.0",
                  "trimester_name": "Semestre 1",
                  "letter_mark": "F"
                }
              ]
            }
            """.trimIndent()
        ).toGrades()

        assertEquals(1, grades.size)
        val main = grades[0]
        assertEquals(null, main.value)
        assertEquals("F", main.gradeLetter)
    }

    @Test
    fun gradesKeepLetterMarkFWhenNumericValueIsPresent() {
        val grades = json.parseToJsonElement(
            """
            {
              "response_code": 200,
              "result": [
                {
                  "rc_id": 338004,
                  "course": "T1 - programme open lab",
                  "grades": [],
                  "exam": 5.0,
                  "coef": "2.0",
                  "trimester_name": "Semestre 1",
                  "letter_mark": "F"
                }
              ]
            }
            """.trimIndent()
        ).toGrades()

        assertEquals(2, grades.size)
        val main = grades[0]
        assertEquals(5.0, main.value)
        assertEquals("F", main.gradeLetter)
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
    fun absencesResolvePeriodByOwnYearNotFirstMatchingSemester() {
        
        val payload = json.parseToJsonElement(
            """
            {
              "result": [
                { "id": "a-2023", "date": 1697328000000, "course_name": "Algo", "justified": false },
                { "id": "a-2025", "date": 1760486400000, "course_name": "Algo", "justified": false }
              ]
            }
            """.trimIndent()
        )
        val periods = listOf("2025-2026 - Semestre 1", "2023-2024 - Semestre 1")

        val absences = payload.toAbsences(year = "2025", availablePeriods = periods)

        val byId = absences.associateBy { it.id }
        assertEquals("2023-2024 - Semestre 1", byId.getValue("a-2023").period)
        assertEquals("2025-2026 - Semestre 1", byId.getValue("a-2025").period)
    }

    @Test
    fun absencesUseApiYearAndTrimesterNotDateHeuristic() {
        val payload = json.parseToJsonElement(
            """
            {
              "result": [
                { "date": 1768546800000, "course_name": "Compil", "justified": false, "trimester": 22, "trimester_name": "Semestre 2", "year": 2025 }
              ]
            }
            """.trimIndent()
        )

        val absence = payload.toAbsences().first()

        assertEquals("2025-2026 - Semestre 2", absence.period)
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
                    { "rel": "url", "href": "https://api.kordis.fr/foo.pdf" }
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
        assertEquals("https://api.kordis.fr/foo.pdf", documents.first().downloadUrl)
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
        assertEquals("project:22843:file:20788", documents.first().id)
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
                            { "rel": "url", "href": "https://api.kordis.fr/bar.zip" }
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
        assertTrue(documents.first().id.startsWith("project:22843:step:"))
        assertTrue(documents.first().id.endsWith(":991"))
        assertEquals("rendu.zip", documents.first().title)
        assertEquals("rendu.zip", documents.first().fileName)
        assertEquals("application/zip", documents.first().mimeType)
        assertEquals("me/projectStepFiles/991", documents.first().downloadUrl)
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
    fun projectDocumentIdsDistinguishFileAndStepSourcesWithSameRemoteId() {
        val documents = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "project_id": 42,
                  "project_files": [
                    { "pf_id": 7, "pf_title": "Brief" }
                  ],
                  "steps": [
                    {
                      "psp_id": 9,
                      "files": [
                        { "psf_id": 7, "psf_name": "Submission" }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        ).toProjectDocuments()

        assertEquals(setOf("project:42:file:7", "project:42:step:9:7"), documents.map { it.id }.toSet())
        assertEquals(setOf("me/projectFiles/7", "me/projectStepFiles/7"), documents.map { it.downloadUrl }.toSet())
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
        assertTrue(news.first().id != "skolae_app_version")
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

    @Test
    fun classIdsParseIdsAndSelfLinks() {
        val classIds = json.parseToJsonElement(
            """
            {
              "result": [
                { "puid": "class-1" },
                { "links": [{ "rel": "self", "href": "https://example.com/classes/class-2" }] },
                { "id": "class-1" }
              ]
            }
            """.trimIndent()
        ).toClassIds()

        assertEquals(listOf("class-1", "class-2"), classIds)
    }

    @Test
    fun eventsParseHtmlDescriptionAndSubscriptionDates() {
        val events = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "event_id": 42,
                  "event_title": "Forum",
                  "event_type": "career",
                  "location": "Paris",
                  "organizer": "School",
                  "description": "<p>Hello&nbsp;world</p>",
                  "event_date": 1781222400000,
                  "start_subscription_date": 1781136000000,
                  "end_subscription_date": 1781308800000,
                  "is_participant_subscribed": "yes",
                  "links": [{ "href": "https://example.com/events/42" }]
                }
              ]
            }
            """.trimIndent()
        ).toEvents()

        assertEquals("42", events.first().id)
        assertEquals("Hello world", events.first().description)
        assertEquals(true, events.first().subscribed)
        assertEquals("https://example.com/events/42", events.first().detailUrl)
    }

    @Test
    fun projectMessagesParseAuthorFallbackAndMineFlag() {
        val messages = json.parseToJsonElement(
            """
            {
              "result": [
                {
                  "pm_id": 12,
                  "u_id": "me",
                  "firstname": "Alice",
                  "name": "Martin",
                  "message": "Bonjour",
                  "date": 1781222400000
                },
                {
                  "pm_id": 13
                }
              ]
            }
            """.trimIndent()
        ).toProjectMessages(currentUserId = "me")

        assertEquals(1, messages.size)
        assertEquals("Alice Martin", messages.first().author)
        assertEquals(true, messages.first().mine)
        assertEquals(Instant.ofEpochMilli(1781222400000), messages.first().sentAt)
    }

    @Test
    fun parsersHandleFallbackFieldsAndEmptyPayloads() {
        val profile = json.parseToJsonElement("{}").toProfile()
        assertEquals("profile", profile.id)
        assertEquals("profile", profile.displayName)

        val years = json.parseToJsonElement("[\"\", 2026, {}]").toYears()
        assertEquals(listOf("2026"), years)

        val agenda = json.parseToJsonElement(
            """
            {"items":[
              {"start":1781222400000,"rooms":["broken"],"campus":"Remote campus"},
              {"start":1781222400000,"modality":"NATION1"}
            ]}
            """.trimIndent()
        ).toAgendaEvents()
        assertEquals(2, agenda.size)
        assertEquals("Remote campus", agenda.first().address)
        assertEquals("242 rue du Faubourg Saint Antoine, 75012 Paris", agenda.last().address)
        assertEquals(3600, agenda.first().endsAt.epochSecond - agenda.first().startsAt.epochSecond)

        val absences = json.parseToJsonElement(
            """{"items":[{"date":"2026-04-01","semester":"x","justified":"yes"},{"id":"missing"}]}"""
        ).toAbsences()
        assertEquals(1, absences.size)
        assertEquals(true, absences.first().justified)
        assertEquals("2025-2026 - Semestre 2", absences.first().period)

        val courses = json.parseToJsonElement(
            """{"items":[{"id":"files","files":[{}]},{"id":"count","file_count":2},{"id":"flag","has_documents":true}]}"""
        ).toCourses()
        assertEquals(listOf(1, 2, 1), courses.map { it.fileCount })
        assertEquals(null, json.parseToJsonElement("{}").toCourseSyllabus())
    }

    @Test
    fun parsersHandleGroupsDocumentsDirectoryAndContentFallbacks() {
        val projects = json.parseToJsonElement(
            """
            {"items":[{
              "id":"project","year":"2026","project_group_logs":[{"user_id":"me","pgr_id":"group"}],
              "groups":[{"id":"group","students":[{"firstname":"Ada","name":"Lovelace"}]}],
              "steps":[{"id":"step","psp_limit_date":"2026-06-12","files":[{}]}]
            }]}
            """.trimIndent()
        ).toProjects("me")
        assertEquals(true, projects.first().groups.single().isMine)
        assertEquals("Ada Lovelace", projects.first().groups.single().students.single())
        assertEquals(1, projects.first().fileCount)
        assertTrue(json.parseToJsonElement("[]").toProjects().isEmpty())
        assertTrue(json.parseToJsonElement("[{\"name\":\"step\"}]").toNextProjectStepProjects().isEmpty())

        val practical = json.parseToJsonElement(
            """{"items":[{"id":"practical","project_files":[{"id":"project-file","psf_file_type":"text/plain"}],"steps":[{"files":[{"id":"step-file","group_id":"group"}]}]}]}"""
        )
        assertEquals(2, practical.toPracticalDocuments("2026").size)
        assertEquals(1, practical.toPracticals(fallbackYear = "2026").size)

        val documents = json.parseToJsonElement(
            """{"annualDocuments":[{"id":"annual","filename":"annual","extension":"pdf"}],"documents":[{"id":"document","filename":"document","extension":"application/zip"}]}"""
        ).toDocuments("2026")
        assertEquals(listOf("annual.pdf", "document.zip"), documents.map { it.fileName })

        val person = json.parseToJsonElement(
            """{"students":[{"student_id":"student","firstname":"Ada","name":"Lovelace","_links":{"photo":{"href":"photo"}}}]}"""
        ).toDirectoryPeople(com.elg.studly.domain.model.DirectoryRole.Student, "2026").single()
        assertEquals("Ada Lovelace", person.displayName)
        assertEquals("photo", person.avatarUrl)
    }

    @Test
    fun parsersHandleNewsEventsMessagesAndDateFormats() {
        val news = json.parseToJsonElement(
            """{"items":[{"id":"html","title":"News","content":"<p>One<br>Two &amp; &#33;</p>","photo":"default-logo.png"},{"id":"fallback","corporate_name":"Company","location":"Paris","offers":[{"offer":"Job"}]}]}"""
        ).toNews()
        assertEquals("One\nTwo & !", news.first().body)
        assertEquals(null, news.first().imageUrl)
        assertEquals("Company - Paris - Job", news.last().body)

        val event = json.parseToJsonElement(
            """{"items":[{"id":"event","description":"Plain","is_participant_subscribed":"no","event_date":"12/06/2026 12:30"}]}"""
        ).toEvents().single()
        assertEquals("Plain", event.description)
        assertEquals(false, event.subscribed)
        assertTrue(event.date != null)

        val messages = json.parseToJsonElement(
            """{"items":[{"id":"message","uid":"me","content":"Body","created_at":"2026-06-12"},{"id":"missing"},"invalid"]}"""
        ).toProjectMessages("me")
        assertEquals(1, messages.size)
        assertEquals("me", messages.single().author)
        assertEquals(true, messages.single().mine)
    }

    @Test
    fun documentAndDateVariantsUseSupportedMappings() {
        val extensions = listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip", "txt", "md", "csv", "html", "png", "jpg", "gif")
        val documents = json.parseToJsonElement(
            """{"items":[${extensions.joinToString { "{\"id\":\"$it\",\"filename\":\"$it\",\"extension\":\"$it\"}" }}]}"""
        ).toDocuments()
        assertEquals(extensions.size, documents.size)
        assertEquals("application/pdf", documents.first().mimeType)
        assertEquals("image/gif", documents.last().mimeType)

        val contentTypes = listOf(
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/zip", "text/plain", "text/markdown", "text/csv", "text/html", "image/png", "image/jpeg", "image/gif"
        )
        val projectDocuments = json.parseToJsonElement(
            """{"items":[{"id":"project","project_files":[${contentTypes.joinToString { "{\"id\":\"${it.substringAfter('/')}\",\"psf_file_type\":\"$it\"}" }}]}]}"""
        ).toProjectDocuments()
        assertEquals(listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip", "txt", "md", "csv", "html", "png", "jpg", "gif"), projectDocuments.map { it.fileName.substringAfterLast('.') })

        val dates = listOf("12/06/2026 12h30", "12/06/2026 12:30", "12/06/2026", "2026-06-12T12:30:00+02:00")
        val events = json.parseToJsonElement(
            """{"items":[${dates.joinToString { "{\"id\":\"$it\",\"event_date\":\"$it\"}" }}]}"""
        ).toEvents()
        assertTrue(events.all { it.date != null })
    }

    @Test
    fun practicalGroupsAndMissingOptionalValuesUseFallbacks() {
        val practical = json.parseToJsonElement(
            """
            {"items":[{
              "project_id":"practical","project_group_logs":[{"uid":"me","group_id":"group"}],
              "groups":[{"group_id":"group","group_name":"Team","students":[{"uid":"me"},{"email":"student@example.com"}]}],
              "steps":[{"psp_id":"step","psp_desc":"Step","psp_limit_date":"2026-06-12"}]
            }]}
            """.trimIndent()
        ).toPracticals("me", "2026").single()
        assertEquals("2026", practical.year)
        assertEquals(true, practical.groups.single().isMine)
        assertEquals(listOf("student@example.com"), practical.groups.single().students)
        assertEquals("step", practical.steps.single().id)

        val event = json.parseToJsonElement("""{"items":[{}]}""").toEvents().single()
        assertEquals("", event.title)
        assertEquals(false, event.subscribed)
        assertEquals(null, event.description)
    }

    @Test
    fun gradesHandleMixedComponentsAndPeriodFallbacks() {
        val grades = json.parseToJsonElement(
            """
            {"items":[{
              "rc_id":"course","course":"Course","grades":[{"value":"12,5","date":"12/06/2026"},null,"broken"],
              "exam":14,"date_exam":"2026-06-13","average":0,"coef":"n.c.","period":"Semestre 1"
            }]}
            """.trimIndent()
        ).toGrades("2025")
        assertEquals(3, grades.size)
        assertEquals(12.5, grades[1].value ?: 0.0, 0.0)
        assertEquals(14.0, grades.last().value ?: 0.0, 0.0)
        assertEquals("2025-2026 - Semestre 1", grades.first().period)
    }

    @Test
    fun htmlTextKeepsInvalidEntitiesAndNormalizesWhitespace() {
        assertEquals("A\nB & &#xZZ; &#999999999999; &unknown;", "<p>A</p><div>B&nbsp;&amp; &#xZZ; &#999999999999; &unknown;</div>".htmlToPlainText())
        assertEquals("&#x110000; &#1114112;", "&#x110000; &#1114112;".htmlToPlainText())
    }

    @Test
    fun helperAliasesAndInstantFormatsAreCovered() {
        val news = json.parseToJsonElement(
            """{"items":[
              {"id":"n1","title":"T","summary":"S","begin_date":1781222400},
              {"id":"n2","title":"T2","summary":"S2","picture":"avatar-default.png"}
            ]}"""
        ).toNews()
        assertEquals(Instant.ofEpochSecond(1781222400), news.first().publishedAt)
        assertEquals(null, news[1].imageUrl)

        val grades = json.parseToJsonElement(
            """{"items":[{"id":"g","course":{"name":"Algo"},"average":13.5,"period":"S1"}]}"""
        ).toGrades("2025")
        assertEquals("Algo", grades.first().courseName)
        assertEquals(13.5, grades.first().value ?: 0.0, 0.0)
        assertEquals("2025-2026 - S1", grades.first().period)

        val absence = json.parseToJsonElement(
            """{"items":[{"id":"a","date":1781222400000,"course_name":"C","justified":"1"}]}"""
        ).toAbsences().single()
        assertEquals(true, absence.justified)
    }

    @Test
    fun groupMembershipViaStudentsAndLinkFallbacks() {
        val projects = json.parseToJsonElement(
            """{"items":[{"id":"p","groups":[{"id":"g","group_name":"Team","project_group_students":[{"student_id":"me"}]}]}]}"""
        ).toProjects("me")
        assertEquals(true, projects.first().groups.single().isMine)

        val person = json.parseToJsonElement(
            """{"teachers":[{"teacher_id":"t","firstname":"A","name":"B","links":[{"rel":"photo","href":"pic"}]}]}"""
        ).toDirectoryPeople(com.elg.studly.domain.model.DirectoryRole.Teacher, "2026").single()
        assertEquals("pic", person.avatarUrl)
        assertEquals("A B", person.displayName)

        val document = json.parseToJsonElement(
            """{"items":[{"id":"d","filename":"report.pdf","extension":"pdf"}]}"""
        ).toDocuments().single()
        assertEquals("report.pdf", document.fileName)
    }

    @Test
    fun agendaUsesDisciplineFallbacksAndUnknownCampusDefaults() {
        val event = json.parseToJsonElement(
            """{"items":[{"start":"2026-06-12T08:00:00Z","discipline":{"name":"Algorithms","teacher":"Teacher","rc_id":"course"},"rooms":[{"campus":"DISTANTIEL"}]}]}"""
        ).toAgendaEvents().single()
        assertEquals("Algorithms", event.title)
        assertEquals("Teacher", event.teacher)
        assertEquals("course", event.courseId)
        assertEquals(null, event.address)
        assertEquals("11", event.colorId)
    }

    @Test
    fun projectDocumentsParseTopLevelDeliverables() {
        val documents = json.parseToJsonElement(
            """{"result":[{"project_id":42,"deliverables":[{"pf_id":7,"pf_title":"rendu.zip","psf_file_type":"application/zip"}]}]}"""
        ).toProjectDocuments()
        assertEquals(1, documents.size)
        assertEquals("project:42:file:7", documents.first().id)
        assertEquals("me/projectFiles/7", documents.first().downloadUrl)
        assertEquals(null, documents.first().groupId)
    }

    @Test
    fun profilePreservesExplicitAcademicYearAndSkipsBlankAliases() {
        val profile = json.parseToJsonElement(
            """{"id":"","uid":"p","displayName":"","fullName":"Ada Lovelace","academicYear":"2025-2026","student_id":"2023-ESGI-123"}"""
        ).toProfile()

        assertEquals("p", profile.id)
        assertEquals("Ada Lovelace", profile.displayName)
        assertEquals("2025-2026", profile.academicYear)
    }

    @Test
    fun flatCourseGradeDoesNotBecomeStructuredAverage() {
        val grades = json.parseToJsonElement(
            """{"items":[
              {"id":"flat","course":"Algorithmique","subject":"Quiz","grade":"17,5"},
              {"id":"structured","course":{"name":"Maths"},"average":13.5}
            ]}"""
        ).toGrades()

        assertEquals(2, grades.size)
        assertEquals("Quiz", grades.first().subject)
        assertEquals(17.5, grades.first().value ?: 0.0, 0.0)
        assertEquals(13.5, grades.last().value ?: 0.0, 0.0)
    }

    @Test
    fun gradeLocalDateParsesFrenchFormatWithoutTimezoneShift() {
        val defaultTimeZone = java.util.TimeZone.getDefault()
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Europe/Paris"))
            val grade = json.parseToJsonElement(
                """[{"id":"g","courseName":"Algo","grade":12,"date":"12/06/2026"}]"""
            ).toGrades().single()

            assertEquals(java.time.LocalDate.of(2026, 6, 12), grade.date)
        } finally {
            java.util.TimeZone.setDefault(defaultTimeZone)
        }
    }

    @Test
    fun absencesResolveAcademicPeriodInParisTimezone() {
        val absence = json.parseToJsonElement(
            """{"items":[{"id":"a","date":"2025-08-31T22:30:00Z"}]}"""
        ).toAbsences().single()

        assertEquals("2025-2026 - Semestre 1", absence.period)
    }

    @Test
    fun coursesParseLocationAliases() {
        val courses = json.parseToJsonElement(
            """{"courses":[
              {"id":"location","location":"Paris"},
              {"id":"campus","campus":"Lyon"},
              {"id":"room","room":"A101"},
              {"id":"site","site":"Bordeaux"},
              {"id":"address","address":"10 rue Exemple"}
            ]}"""
        ).toCourses()

        assertEquals(listOf("Paris", "Lyon", "A101", "Bordeaux", "10 rue Exemple"), courses.map { it.location })
    }

    @Test
    fun syllabusResultObjectKeepsPrimitiveArrays() {
        val syllabus = json.parseToJsonElement(
            """{"result":{
              "syllabus_name":"Kotlin",
              "skills":["Coroutines", "Flow"],
              "control_types":[{"evaluation_label":"Exam"}, "Project"],
              "seance_details":[1, "Workshop"]
            }}"""
        ).toCourseSyllabus()

        checkNotNull(syllabus)
        assertTrue(syllabus.contains("Kotlin"))
        assertTrue(syllabus.contains("Coroutines, Flow"))
        assertTrue(syllabus.contains("Exam, Project"))
        assertTrue(syllabus.contains("1, Workshop"))
    }

    @Test
    fun arrayWrappersIgnoreUnrelatedArraysAndPreserveEmptyPayloads() {
        val news = json.parseToJsonElement(
            """{"title":"Actual","metadata":[{"title":"Wrong"}]}"""
        ).toNews().single()

        assertEquals("Actual", news.title)
        assertTrue(json.parseToJsonElement("""{"metadata":[{"id":"wrong"}]}""").toCourses().isEmpty())
        assertTrue(json.parseToJsonElement("""{"result":{"projects":[]}}""").toProjects().isEmpty())
        assertEquals(
            "project",
            json.parseToJsonElement("""{"id":"project","groups":[{"id":"group"}]}""").toProjects().single().id
        )
    }

    @Test
    fun requestedLinkRelationDoesNotFallbackToFirstLink() {
        val profile = json.parseToJsonElement(
            """{"id":"p","links":[{"rel":"self","href":"wrong"}]}"""
        ).toProfile()

        assertEquals(null, profile.avatarUrl)
        assertTrue(
            json.parseToJsonElement("""{"classes":[{"links":[{"rel":"photo","href":"wrong"}]}]}""")
                .toClassIds().isEmpty()
        )
    }

    @Test
    fun newsWithoutRemoteIdsKeepDistinctStableIds() {
        val news = json.parseToJsonElement(
            """{"items":[{"type":"alert","title":"First"},{"type":"alert","title":"Second"}]}"""
        ).toNews()

        assertEquals(2, news.size)
        assertEquals(2, news.map { it.id }.distinct().size)
    }

    private fun parseStart(raw: String): Instant? =
        json.parseToJsonElement("""{"items":[{"start":$raw}]}""")
            .toAgendaEvents().single().startsAt

    @Test
    fun parseInstantSupportsEveryDateFormat() {
        val zone = java.time.ZoneId.systemDefault()

        assertEquals(Instant.ofEpochSecond(1700000000L), parseStart("\"1700000000\""))
        assertEquals(Instant.ofEpochMilli(1700000000000L), parseStart("\"1700000000000\""))
        assertEquals(Instant.ofEpochSecond(1700000000L), parseStart("1700000000"))
        assertEquals(Instant.ofEpochMilli(1700000000000L), parseStart("1700000000000"))

        assertEquals(
            java.time.LocalDateTime.parse("12/06/2026 08h30", java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h'mm")).atZone(zone).toInstant(),
            parseStart("\"12/06/2026 08h30\"")
        )
        assertEquals(
            java.time.LocalDateTime.parse("12/06/2026 08:30", java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")).atZone(zone).toInstant(),
            parseStart("\"12/06/2026 08:30\"")
        )
        assertEquals(
            java.time.LocalDate.parse("12/06/2026", java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay(zone).toInstant(),
            parseStart("\"12/06/2026\"")
        )
        assertEquals(
            java.time.LocalDateTime.parse("2026-06-12T08:30:00").atZone(zone).toInstant(),
            parseStart("\"2026-06-12T08:30:00\"")
        )

        assertEquals(Instant.parse("2026-06-12T08:00:00Z"), parseStart("\"2026-06-12T08:00:00Z\""))
        assertEquals(Instant.parse("2026-06-12T08:00:00Z"), parseStart("\"  2026-06-12T08:00:00Z  \""))
        assertEquals(
            java.time.LocalDate.parse("2026-06-12").atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
            parseStart("\"2026-06-12\"")
        )

        assertTrue(json.parseToJsonElement("""{"items":[{"start":"not-a-date"}]}""").toAgendaEvents().isEmpty())
        assertTrue(json.parseToJsonElement("""{"items":[{"start":null}]}""").toAgendaEvents().isEmpty())
    }
}
