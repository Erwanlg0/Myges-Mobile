package com.elg.studly.adapters.secondary.repository

import android.content.Context
import com.elg.studly.adapters.secondary.api.MyGesApiService
import com.elg.studly.adapters.secondary.storage.AbsenceEntity
import com.elg.studly.adapters.secondary.storage.AcademicDocumentEntity
import com.elg.studly.adapters.secondary.storage.AgendaEventEntity
import com.elg.studly.adapters.secondary.storage.CourseEntity
import com.elg.studly.adapters.secondary.storage.DirectoryPersonEntity
import com.elg.studly.adapters.secondary.storage.GradeEntity
import com.elg.studly.adapters.secondary.storage.NewsEntity
import com.elg.studly.adapters.secondary.storage.PracticalEntity
import com.elg.studly.adapters.secondary.storage.ProjectEntity
import com.elg.studly.adapters.secondary.storage.ProjectGroupEntity
import com.elg.studly.adapters.secondary.storage.ProjectStepEntity
import com.elg.studly.adapters.secondary.storage.StudentDao
import com.elg.studly.adapters.secondary.storage.StudentEventEntity
import com.elg.studly.adapters.secondary.storage.StudentProfileEntity
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NotificationPreferences
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ProjectGroup
import com.elg.studly.domain.model.ProjectStep
import com.elg.studly.domain.model.ReminderTarget
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.UserSettings
import android.net.Uri
import androidx.core.content.FileProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import okhttp3.Protocol
import okhttp3.Request
import retrofit2.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant

class OfflineFirstStudentDataRepositoryTest {
    @Test
    fun syncAllPersistsRemotePayloadAndPurgesExpiredDocumentCache() = runTest {
        val cacheDir = createTempDir()
        val documentCache = File(cacheDir, "documents").apply { mkdirs() }
        val expired = File(documentCache, "expired.pdf").apply {
            writeText("expired")
            setLastModified(Instant.now().minus(Duration.ofDays(31)).toEpochMilli())
        }
        val current = File(documentCache, "current.pdf").apply {
            writeText("current")
            setLastModified(Instant.now().minus(Duration.ofDays(2)).toEpochMilli())
        }
        val api = RepositoryApi()
        val dao = RepositoryDao()
        val repository = repository(cacheDir, api, dao, RepositoryNotificationScheduler())

        repository.syncAll()

        assertEquals("student-1", dao.profileState.value?.id)
        assertEquals(listOf("agenda-1"), dao.agendaState.value.map { it.id })
        assertEquals(listOf("grade-1"), dao.gradeState.value.map { it.id })
        assertEquals(listOf("absence-1"), dao.absenceState.value.map { it.id })
        assertEquals(listOf("course-1"), dao.courseState.value.map { it.id })
        assertEquals("Algorithms\n\nDetailed syllabus", dao.courseState.value.first().syllabus)
        assertEquals(listOf("project-1"), dao.projectState.value.map { it.id })
        assertEquals(setOf("step-1", "upcoming-step-1"), dao.projectStepState.value.map { it.id }.toSet())
        assertEquals(listOf("practical-1"), dao.practicalState.value.map { it.id })
        assertEquals(setOf("annual-doc-1", "course-doc-1", "project-doc-1", "syllabus-course-1"), dao.documentState.value.map { it.id }.toSet())
        assertEquals("me/course-1/files/course-doc-1", dao.documentState.value.first { it.id == "course-doc-1" }.downloadUrl)
        assertEquals(setOf("skolae_app_version", "news-1", "banner-1", "partner-1", "speed-1"), dao.newsState.value.map { it.id }.toSet())
        assertEquals(listOf("course-1"), api.courseFileRequests)
        assertFalse(expired.exists())
        assertTrue(current.exists())
    }

    @Test
    fun syncAllNotifiesOnlyNewRemoteItemsWhenPreviousLocalIdsExist() = runTest {
        val dao = RepositoryDao().apply {
            agendaState.value = listOf(agendaEntity("old-agenda"))
            gradeState.value = listOf(gradeEntity("old-grade"))
            absenceState.value = listOf(absenceEntity("old-absence"))
            projectState.value = listOf(projectEntity("old-project"))
            documentState.value = listOf(documentEntity("old-document"))
        }
        val notifications = RepositoryNotificationScheduler()
        val repository = repository(createTempDir(), RepositoryApi(), dao, notifications)

        repository.syncAll()

        assertEquals(listOf("agenda-1"), notifications.agendaChanges)
        assertEquals(listOf("grade-1"), notifications.grades)
        assertEquals(listOf("absence-1"), notifications.absences)
        assertEquals(listOf("project-1"), notifications.projects)
        assertEquals(setOf("annual-doc-1", "course-doc-1", "project-doc-1", "syllabus-course-1"), notifications.documents.toSet())
    }

    @Test
    fun syncAllKeepsCachedDataWhenNetworkFailsBeforeRemoteSnapshot() = runTest {
        val dao = RepositoryDao().apply {
            profileState.value = StudentProfileEntity("cached-profile", "Cached Student", null, null, null, null, null)
            agendaState.value = listOf(agendaEntity("cached-agenda"))
            gradeState.value = listOf(gradeEntity("cached-grade"))
            documentState.value = listOf(documentEntity("cached-document"))
        }
        val api = RepositoryApi().apply {
            profileFailure = IOException("offline")
        }
        val repository = repository(createTempDir(), api, dao, RepositoryNotificationScheduler())

        val failure = runCatching { repository.syncAll() }.exceptionOrNull()

        assertEquals(AppError.Network, (failure as AppException).error)
        assertEquals("cached-profile", dao.profileState.value?.id)
        assertEquals(listOf("cached-agenda"), dao.agendaState.value.map { it.id })
        assertEquals(listOf("cached-grade"), dao.gradeState.value.map { it.id })
        assertEquals(listOf("cached-document"), dao.documentState.value.map { it.id })
    }

    @Test
    fun observesStoredEntitiesAndSyncsRequestedFeatureOnly() = runTest {
        val dao = RepositoryDao().apply {
            agendaState.value = listOf(agendaEntity("agenda"))
            gradeState.value = listOf(gradeEntity("grade"))
            absenceState.value = listOf(absenceEntity("absence"))
            courseState.value = listOf(CourseEntity("course", "Course", null, null, null, null, 0, null))
            projectState.value = listOf(projectEntity("project"))
            practicalState.value = listOf(PracticalEntity("practical", "Practical", null, null, null, null, null, null, null))
            documentState.value = listOf(documentEntity("document"))
        }
        val repository = repository(createTempDir(), RepositoryApi(), dao, RepositoryNotificationScheduler())

        assertEquals("agenda", repository.observeAgenda().first().single().id)
        assertEquals("grade", repository.observeGrades().first().single().id)
        assertEquals("absence", repository.observeAbsences().first().single().id)
        assertEquals("course", repository.observeCourses().first().single().id)
        assertEquals("project", repository.observeProjects().first().single().id)
        assertEquals("practical", repository.observePracticals().first().single().id)
        assertEquals("document", repository.observeDocuments().first().single().id)

        repository.syncAll(force = true, features = setOf(SyncFeature.Agenda))

        assertEquals(listOf("agenda-1"), dao.agendaState.value.map { it.id })
        assertEquals(listOf("grade"), dao.gradeState.value.map { it.id })
    }

    @Test
    fun observesDashboardAndEmptyFeatureCollections() = runTest {
        val repository = repository(createTempDir(), RepositoryApi(), RepositoryDao(), RepositoryNotificationScheduler())

        assertEquals(null, repository.observeDashboard().first().profile)
        assertTrue(repository.observeDirectory().first().isEmpty())
        assertTrue(repository.observeNews().first().isEmpty())
        assertTrue(repository.observeEvents().first().isEmpty())
    }

    @Test
    fun projectMessagesAndGroupCommandsUseApiResponses() = runTest {
        val api = RepositoryApi().apply {
            projectMessagesResponse = jsonElement(
                """{"result":[{"id":"message","uid":"student","message":"Hello","date":"2026-06-12"}]}"""
            )
        }
        val dao = RepositoryDao().apply {
            profileState.value = StudentProfileEntity("student", "Student", null, null, null, null, null)
            projectGroupState.value = listOf(
                ProjectGroupEntity("project", "group", "Group", "Student\nOther", false),
                ProjectGroupEntity("project", "other", "Other", "Student\nOther", true)
            )
        }
        val repository = repository(createTempDir(), api, dao, RepositoryNotificationScheduler())

        assertEquals("message", repository.projectMessages("group").single().id)
        repository.sendProjectMessage("group", "Hello")
        repository.joinGroup("course", "project", "group")
        assertEquals(true, dao.projectGroupState.value.first { it.id == "group" }.isMine)
        assertEquals(false, dao.projectGroupState.value.first { it.id == "other" }.isMine)
        repository.leaveGroup("course", "project", "group")

        assertEquals(listOf("{\"message\":\"Hello\"}"), api.sentMessages)
        assertEquals(false, dao.projectGroupState.value.first { it.id == "group" }.isMine)

        api.groupResponse = Response.error(401, "denied".toResponseBody())
        val failure = runCatching { repository.joinGroup("course", "project", "group") }.exceptionOrNull() as AppException
        assertEquals(AppError.Unauthorized, failure.error)

        api.groupResponse = Response.error(500, "{\"message\":\"unavailable\"}".toResponseBody())
        val remoteFailure = runCatching { repository.leaveGroup("course", "project", "group") }.exceptionOrNull() as AppException
        assertEquals(AppError.Remote(500, "unavailable"), remoteFailure.error)
    }

    @Test
    fun agendaWindowsAndAcademicYearCandidatesUseExpectedBounds() {
        val day = java.time.LocalDate.of(2026, 6, 12)

        assertEquals(java.time.LocalDate.of(2023, 9, 1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli(), AgendaWindow.firstSync(day).start)
        assertEquals(day.minusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli(), AgendaWindow.subsequentSync(day).start)
        assertEquals(day.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli(), AgendaWindow.fromToday(day).start)
        assertEquals(listOf("2026", "2025", "2024", "2023", "2022", "2021", "2020", "2019", "2018", "2017", "2016", "2026-2027", "2020-2021"), academicYearCandidates(listOf("2020-2021"), "2026-2027", 2025))
    }

    @Test
    fun documentHelpersNormalizeNamesUrlsMimeTypesAndErrors() {
        val repository = repository(createTempDir(), RepositoryApi(), RepositoryDao(), RepositoryNotificationScheduler())

        with(repository) {
            assertEquals("me/annualDocuments/document", AcademicDocument("document", "Document", null, null, null, "doc", null, null).normalizedDownloadUrl())
            assertEquals("me/course/files/document", AcademicDocument("document", "Document", null, null, null, "doc", "https://ges-dl.example/file?courseId=course", null).normalizedDownloadUrl())
            assertEquals("remote", AcademicDocument("document", "Document", null, null, null, "doc", "remote", null).normalizedDownloadUrl())
            assertEquals("unsafe_name_.pdf", "unsafe name?.pdf".sanitizedFileName())
            assertEquals("document", "".sanitizedFileName())
            assertEquals("document.pdf", "document".withExtension("application/pdf".toMediaType()))
            assertEquals("document.pdf", "document.pdf".withExtension("text/plain".toMediaType()))
            assertEquals("zip", "application/zip".toMediaType().toFileExtension())
            assertEquals(null, "application/octet-stream".toMediaType().toFileExtension())
            assertEquals("report name.pdf", "attachment; filename*=UTF-8''report%20name.pdf".contentDispositionFileName())
            assertEquals("report.pdf", "attachment; filename=\"report.pdf\"".contentDispositionFileName())
            assertEquals(null, "attachment".contentDispositionFileName())
            assertEquals(AppError.Network, IOException().toRepositoryException().error)
            assertEquals(AppError.Unexpected("broken"), IllegalStateException("broken").toRepositoryException().error)
        }
    }

    @Test
    fun projectAndSyllabusHelpersMergeRemoteData() {
        val repository = repository(createTempDir(), RepositoryApi(), RepositoryDao(), RepositoryNotificationScheduler())
        val base = Project("project", "", null, null, null, null, listOf(ProjectStep("one", "One", null, null)), 1)
        val upcoming = Project("project", "Project", "Course", "Group", "open", Instant.now(), listOf(ProjectStep("two", "Two", null, null)), 2, "2026", "course", listOf(ProjectGroup("group", "Group", listOf("Student"), true)))
        val added = upcoming.copy(id = "added")

        with(repository) {
            val merged = listOf(base).withNextProjectSteps(listOf(upcoming, added))
            assertEquals(setOf("one", "two"), merged.first { it.id == "project" }.steps.map { it.id }.toSet())
            assertEquals(setOf("project", "added"), merged.map { it.id }.toSet())
            assertEquals(2, listOf(base, upcoming).mergeProjects().single().fileCount)
            assertEquals(true, mergeGroups(emptyList(), upcoming.groups).single().isMine)
            assertEquals("syllabus-course", syllabusDocuments(listOf(Course("course", "Course", null, "2026", null, "Text", 0))).single().id)
        }
    }

    @Test
    fun dueFeaturesSkipFreshFeaturesUnlessForced() = runTest {
        val api = RepositoryApi()
        val dao = RepositoryDao()
        val settings = RepositorySettingsRepository().apply { lastFetched = Instant.now() }
        val repository = repository(createTempDir(), api, dao, RepositoryNotificationScheduler(), settings)

        repository.syncAll()
        assertEquals(null, dao.profileState.value)

        repository.syncAll(force = true)
        assertEquals("student-1", dao.profileState.value?.id)
    }

    @Test
    fun directoryAggregatesTeachersStudentsAndRecoversClassStudents() = runTest {
        val api = RepositoryApi().apply {
            teachersJson = """{"result":[{"teacher_id":"t1","firstname":"Ada","name":"Lovelace"}]}"""
            studentsJson = """{"result":[{"student_id":"s1","firstname":"Alan","name":"Turing"}]}"""
            classesJson = """{"result":[{"puid":"class-1"}]}"""
            classStudentsJson = """{"result":[{"student_id":"s2","firstname":"Grace","name":"Hopper"}]}"""
            classStudentsSingleFailure = IOException("legacy endpoint")
        }
        val dao = RepositoryDao()
        val repository = repository(createTempDir(), api, dao, RepositoryNotificationScheduler())

        repository.syncAll(force = true, features = setOf(SyncFeature.Directory))

        val ids = dao.directoryState.value.map { it.id }
        assertTrue(ids.any { it.endsWith("t1") })
        assertTrue(ids.any { it.endsWith("s1") })
        assertTrue(ids.any { it.endsWith("s2") })
    }

    @Test
    fun toRepositoryExceptionParsesHttpErrorBodies() {
        val repository = repository(createTempDir(), RepositoryApi(), RepositoryDao(), RepositoryNotificationScheduler())
        fun httpError(code: Int, body: String) =
            HttpException(Response.error<ResponseBody>(code, body.toResponseBody("application/json".toMediaType())))

        with(repository) {
            assertEquals(AppError.Unauthorized, httpError(403, "{}").toRepositoryException().error)
            assertEquals(AppError.Remote(400, "boom"), httpError(400, """{"error":"boom"}""").toRepositoryException().error)
            assertEquals(AppError.Remote(400, "bad field"), httpError(400, """{"detail":"bad field"}""").toRepositoryException().error)
            assertEquals(AppError.Remote(422, """{"foo":"bar"}"""), httpError(422, """{"foo":"bar"}""").toRepositoryException().error)
            assertEquals(AppError.Remote(500, "<html>nope</html>"), httpError(500, "<html>nope</html>").toRepositoryException().error)
        }
    }

    @Test
    fun toFileExtensionMapsAllSupportedMediaTypes() {
        val repository = repository(createTempDir(), RepositoryApi(), RepositoryDao(), RepositoryNotificationScheduler())
        val expected = mapOf(
            "application/pdf" to "pdf", "application/zip" to "zip", "text/plain" to "txt",
            "text/markdown" to "md", "text/csv" to "csv", "text/html" to "html",
            "image/png" to "png", "image/jpeg" to "jpg", "image/gif" to "gif",
            "application/msword" to "doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "docx",
            "application/vnd.ms-excel" to "xls",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to "xlsx",
            "application/vnd.ms-powerpoint" to "ppt",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" to "pptx"
        )
        with(repository) {
            expected.forEach { (mime, ext) ->
                assertEquals(ext, mime.toMediaType().toFileExtension())
            }
        }
    }

    @Test
    fun downloadDocumentHandlesInlineRemoteAndUnavailablePaths() = runTest {
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns mockk<Uri>()
        try {
            val api = RepositoryApi()
            val dao = RepositoryDao()
            val repository = repository(createTempDir(), api, dao, RepositoryNotificationScheduler())

            val inlineDoc = AcademicDocument("inline", "Notes", null, null, "text/plain", "notes.txt", null, null, null, null, "Hello body")
            val progress = mutableListOf<Float?>()
            repository.downloadDocument(inlineDoc) { progress += it }
            assertEquals(listOf(0f, 1f), progress)

            val remoteDoc = AcademicDocument("remote", "Doc", null, null, null, "doc", "remote", null)
            api.downloadResponse = Response.success(
                "data".toResponseBody("application/pdf".toMediaType()),
                rawResponse("https://example.com/file.pdf")
            )
            repository.downloadDocument(remoteDoc) {}

            api.downloadResponse = Response.success(
                "x".toResponseBody("application/pdf".toMediaType()),
                rawResponse("https://cas.example.com/login")
            )
            assertEquals(
                AppError.DocumentUnavailable,
                (runCatching { repository.downloadDocument(remoteDoc) {} }.exceptionOrNull() as AppException).error
            )

            api.downloadResponse = Response.success(
                "<html></html>".toResponseBody("text/html".toMediaType()),
                rawResponse("https://example.com/file")
            )
            assertEquals(
                AppError.DocumentUnavailable,
                (runCatching { repository.downloadDocument(remoteDoc) {} }.exceptionOrNull() as AppException).error
            )

            api.downloadResponse = Response.success(null, rawResponse("https://example.com/file"))
            assertEquals(
                AppError.EmptyResponse,
                (runCatching { repository.downloadDocument(remoteDoc) {} }.exceptionOrNull() as AppException).error
            )

            api.downloadResponse = Response.error(404, "missing".toResponseBody("text/plain".toMediaType()))
            assertEquals(
                AppError.Remote(404, "missing"),
                (runCatching { repository.downloadDocument(remoteDoc) {} }.exceptionOrNull() as AppException).error
            )
        } finally {
            unmockkStatic(FileProvider::class)
        }
    }

    private fun rawResponse(url: String, headers: Map<String, String> = emptyMap()): okhttp3.Response =
        okhttp3.Response.Builder()
            .request(Request.Builder().url(url).build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .apply { headers.forEach { (key, value) -> header(key, value) } }
            .build()

    private fun repository(
        cacheDir: File,
        api: RepositoryApi,
        dao: RepositoryDao,
        notificationScheduler: RepositoryNotificationScheduler,
        settings: RepositorySettingsRepository = RepositorySettingsRepository()
    ): OfflineFirstStudentDataRepository {
        val context = mockk<Context> {
            every { this@mockk.cacheDir } returns cacheDir
            every { packageName } returns "com.elg.studly"
        }
        return OfflineFirstStudentDataRepository(
            context = context,
            api = api,
            dao = dao,
            settingsRepository = settings,
            notificationScheduler = notificationScheduler
        )
    }
}

private class RepositoryApi : MyGesApiService {
    val courseFileRequests = mutableListOf<String>()
    var profileFailure: Throwable? = null
    var groupResponse: Response<ResponseBody> = Response.success(null)
    var projectMessagesResponse: JsonElement = jsonElement("""{"result":[]}""")
    val sentMessages = mutableListOf<String>()
    var teachersJson = """{"result":[]}"""
    var studentsJson = """{"result":[]}"""
    var classesJson = """{"result":[]}"""
    var classStudentsJson = """{"result":[]}"""
    var classStudentsSingleFailure: Throwable? = null
    var downloadResponse: Response<ResponseBody>? = null

    override suspend fun profile(): JsonElement {
        profileFailure?.let { throw it }
        return jsonElement(
            """{"result":{"id":"student-1","displayName":"Student One","email":"student@example.com","academicYear":"2026"}}"""
        )
    }

    override suspend fun minimumVersion(): JsonElement = jsonElement(
        """{"result":{"type":"skolae_app_version","label":"Minimum app version","value":"3.5.0"}}"""
    )

    override suspend fun years(): JsonElement = jsonElement("""{"result":["2026"]}""")

    override suspend fun trimesterYears(): JsonElement = jsonElement(
        """{"result":[{"tri_describe":"Semestre 1","tri_id":21,"tri_name":"S1","year":2026}]}"""
    )

    override suspend fun cvec(): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun internalRules(): JsonElement = jsonElement("""{"result":[]}""")

    override suspend fun suggestions(): JsonElement = jsonElement("""{"result":[]}""")

    override suspend fun submitSuggestion(suggestion: JsonElement): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun agenda(start: Long?, end: Long?): JsonElement = jsonElement(
        """{"result":[{"id":"agenda-1","title":"Math","start_date":"2026-06-12T08:00:00Z","end_date":"2026-06-12T10:00:00Z","room":"A101","teacher":"Teacher","type":"Course","rc_id":"course-1"}]}"""
    )

    override suspend fun courses(year: String): JsonElement = jsonElement(
        """{"result":[{"id":"course-1","name":"Algorithms","teacher":"Teacher","year":"2026","has_documents":true}]}"""
    )

    override suspend fun classes(year: String): JsonElement = jsonElement(classesJson)

    override suspend fun students(year: String): JsonElement = jsonElement(studentsJson)

    override suspend fun teachers(year: String): JsonElement = jsonElement(teachersJson)

    override suspend fun classStudents(puid: String, year: String): JsonElement = jsonElement(classStudentsJson)

    override suspend fun classStudents(puid: String): JsonElement {
        classStudentsSingleFailure?.let { throw it }
        return jsonElement(classStudentsJson)
    }

    override suspend fun courseFiles(rcId: String): JsonElement {
        courseFileRequests += rcId
        return jsonElement(
            """{"result":[{"oc_id":"course-doc-1","title":"Course file","fileName":"course.pdf","extension":"pdf","links":[{"rel":"url","href":"https://example.com/doc.pdf"}]}]}"""
        )
    }

    override suspend fun courseFile(rcId: String, ocId: String): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun syllabus(rcId: String): JsonElement = jsonElement(
        """{"result":{"syllabus_name":"Algorithms","detail_plan":"Detailed syllabus"}}"""
    )

    override suspend fun grades(year: String): JsonElement = jsonElement(
        """{"result":[{"id":"grade-1","courseName":"Algorithms","subject":"Exam","value":15.5,"scale":20,"date":"2026-06-10"}]}"""
    )

    override suspend fun absences(year: String): JsonElement = jsonElement(
        """{"result":[{"id":"absence-1","courseName":"Algorithms","start":"2026-06-11T08:00:00Z","end":"2026-06-11T10:00:00Z","justified":false,"status":"pending"}]}"""
    )

    override suspend fun annualDocuments(year: String): JsonElement = jsonElement(
        """{"result":[{"id":"annual-doc-1","title":"Certificate","fileName":"certificate.pdf","extension":"pdf","year":"2026","updatedAt":"2026-06-01T12:00:00Z"}]}"""
    )

    override suspend fun annualDocument(id: String): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun projects(year: String): JsonElement = jsonElement(
        """{"result":[{"id":"project-1","name":"Project","courseName":"Algorithms","deadline":"2026-06-30T23:59:00Z","steps":[{"id":"step-1","title":"Submit","deadline":"2026-06-30T23:59:00Z"}],"project_files":[{"pf_id":"project-doc-1","pf_title":"Project brief","pf_file":"brief.pdf","extension":"pdf"}]}]}"""
    )

    override suspend fun project(projectId: String): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun nextProjectSteps(): JsonElement = jsonElement(
        """{"result":[{"pro_id":"project-1","pro_name":"Project","course_name":"Algorithms","psp_id":"upcoming-step-1","psp_desc":"Oral","psp_limit_date":"2026-06-25T12:00:00Z"}]}"""
    )

    override suspend fun projectFile(pfId: String): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun projectStepFile(psfId: String): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun courseProjects(rcId: String): JsonElement = jsonElement("""{"result":[]}""")

    override suspend fun projectGroup(
        rcId: String,
        projectId: String,
        projectGroupId: String
    ): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun practicals(year: String): JsonElement = jsonElement(
        """{"result":[{"id":"practical-1","name":"Lab","courseName":"Algorithms","start":"2026-06-13T08:00:00Z","end":"2026-06-13T10:00:00Z","room":"B201"}]}"""
    )

    override suspend fun coursePracticals(rcId: String): JsonElement = jsonElement("""{"result":[]}""")

    override suspend fun news(): JsonElement = jsonElement(
        """{"result":[{"id":"news-1","title":"News","body":"Body","publishedAt":"2026-06-01T08:00:00Z"}]}"""
    )

    override suspend fun newsBanners(): JsonElement = jsonElement(
        """{"result":[{"id":"banner-1","title":"Banner","body":"Important","publishedAt":"2026-06-02T08:00:00Z"}]}"""
    )

    override suspend fun partners(): JsonElement = jsonElement(
        """{"result":[{"partner_id":"partner-1","name":"Partner","content":"Student offer"}]}"""
    )

    override suspend fun notificationDelays(): JsonElement = jsonElement("""{"result":[]}""")

    override suspend fun notificationDelay(notificationTypeId: String): JsonElement = jsonElement("""{"result":{}}""")

    override suspend fun speedMeetingAppointments(): JsonElement = jsonElement(
        """{"result":[{"ss_id":"speed-1","title":"Speed meeting","corporate_name":"Company","location":"ONLINE","appointment_start":"2026-06-03T08:00:00Z"}]}"""
    )

    override suspend fun events(): JsonElement = jsonElement("""{"result":[]}""")

    override suspend fun download(url: String): Response<ResponseBody> {
        return downloadResponse ?: error("unused")
    }

    override suspend fun joinGroup(rcId: String, projectId: String, groupId: String): Response<ResponseBody> {
        return groupResponse
    }

    override suspend fun leaveGroup(rcId: String, projectId: String, groupId: String): Response<ResponseBody> {
        return groupResponse
    }

    override suspend fun projectGroupMessages(projectGroupId: String): JsonElement = projectMessagesResponse

    override suspend fun sendProjectGroupMessage(
        projectGroupId: String,
        message: JsonElement
    ): Response<ResponseBody> {
        sentMessages += message.toString()
        return groupResponse
    }
}

private class RepositoryDao : StudentDao() {
    val profileState = MutableStateFlow<StudentProfileEntity?>(null)
    val agendaState = MutableStateFlow(emptyList<AgendaEventEntity>())
    val gradeState = MutableStateFlow(emptyList<GradeEntity>())
    val absenceState = MutableStateFlow(emptyList<AbsenceEntity>())
    val courseState = MutableStateFlow(emptyList<CourseEntity>())
    val projectState = MutableStateFlow(emptyList<ProjectEntity>())
    val projectGroupState = MutableStateFlow(emptyList<ProjectGroupEntity>())
    val projectStepState = MutableStateFlow(emptyList<ProjectStepEntity>())
    val practicalState = MutableStateFlow(emptyList<PracticalEntity>())
    val documentState = MutableStateFlow(emptyList<AcademicDocumentEntity>())
    val directoryState = MutableStateFlow(emptyList<DirectoryPersonEntity>())
    val newsState = MutableStateFlow(emptyList<NewsEntity>())
    val eventState = MutableStateFlow(emptyList<StudentEventEntity>())

    override fun observeProfile(): Flow<StudentProfileEntity?> = profileState
    override fun observeAgenda(): Flow<List<AgendaEventEntity>> = agendaState
    override fun observeGrades(): Flow<List<GradeEntity>> = gradeState
    override fun observeAbsences(): Flow<List<AbsenceEntity>> = absenceState
    override fun observeCourses(): Flow<List<CourseEntity>> = courseState
    override fun observeProjects(): Flow<List<ProjectEntity>> = projectState
    override fun observeProjectGroups(): Flow<List<ProjectGroupEntity>> = projectGroupState
    override fun observeProjectSteps(): Flow<List<ProjectStepEntity>> = projectStepState
    override fun observePracticals(): Flow<List<PracticalEntity>> = practicalState
    override fun observeDocuments(): Flow<List<AcademicDocumentEntity>> = documentState
    override fun observeDirectory(): Flow<List<DirectoryPersonEntity>> = directoryState
    override fun observeNews(): Flow<List<NewsEntity>> = newsState
    override fun observeEvents(): Flow<List<StudentEventEntity>> = eventState
    override suspend fun agendaIds(): List<String> = agendaState.value.map { it.id }
    override suspend fun gradeIds(): List<String> = gradeState.value.map { it.id }
    override suspend fun absenceIds(): List<String> = absenceState.value.map { it.id }
    override suspend fun projectIds(): List<String> = projectState.value.map { it.id }
    override suspend fun documentIds(): List<String> = documentState.value.map { it.id }
    override suspend fun profile(): StudentProfileEntity? = profileState.value
    override suspend fun agenda(): List<AgendaEventEntity> = agendaState.value
    override suspend fun grades(): List<GradeEntity> = gradeState.value
    override suspend fun absences(): List<AbsenceEntity> = absenceState.value
    override suspend fun courses(): List<CourseEntity> = courseState.value
    override suspend fun projects(): List<ProjectEntity> = projectState.value
    override suspend fun projectGroups(): List<ProjectGroupEntity> = projectGroupState.value
    override suspend fun projectSteps(): List<ProjectStepEntity> = projectStepState.value
    override suspend fun practicals(): List<PracticalEntity> = practicalState.value
    override suspend fun documents(): List<AcademicDocumentEntity> = documentState.value
    override suspend fun documentInlineContent(id: String): String? =
        documentState.value.firstOrNull { it.id == id }?.inlineContent
    override suspend fun directoryPeople(): List<DirectoryPersonEntity> = directoryState.value
    override suspend fun news(): List<NewsEntity> = newsState.value
    override suspend fun events(): List<StudentEventEntity> = eventState.value
    override suspend fun upsertProfile(profile: StudentProfileEntity) {
        profileState.value = profile
    }
    override suspend fun upsertAgenda(events: List<AgendaEventEntity>) {
        agendaState.value = events
    }
    override suspend fun upsertGrades(grades: List<GradeEntity>) {
        gradeState.value = grades
    }
    override suspend fun upsertAbsences(absences: List<AbsenceEntity>) {
        absenceState.value = absences
    }
    override suspend fun upsertCourses(courses: List<CourseEntity>) {
        courseState.value = courses
    }
    override suspend fun upsertProjects(projects: List<ProjectEntity>) {
        projectState.value = projects
    }
    override suspend fun upsertProjectGroups(groups: List<ProjectGroupEntity>) {
        projectGroupState.value = groups
    }
    override suspend fun deleteGroupsForProject(projectId: String) {
        projectGroupState.value = projectGroupState.value.filterNot { it.projectId == projectId }
    }
    override suspend fun upsertProjectSteps(steps: List<ProjectStepEntity>) {
        projectStepState.value = steps
    }
    override suspend fun upsertPracticals(practicals: List<PracticalEntity>) {
        practicalState.value = practicals
    }
    override suspend fun upsertDocuments(documents: List<AcademicDocumentEntity>) {
        documentState.value = documents
    }
    override suspend fun upsertDirectoryPeople(people: List<DirectoryPersonEntity>) {
        directoryState.value = people
    }
    override suspend fun upsertNews(news: List<NewsEntity>) {
        newsState.value = news
    }
    override suspend fun upsertEvents(events: List<StudentEventEntity>) {
        eventState.value = events
    }
    override suspend fun deleteAgenda(events: List<AgendaEventEntity>) = Unit
    override suspend fun deleteGrades(grades: List<GradeEntity>) = Unit
    override suspend fun deleteAbsences(absences: List<AbsenceEntity>) = Unit
    override suspend fun deleteCourses(courses: List<CourseEntity>) = Unit
    override suspend fun deleteProjects(projects: List<ProjectEntity>) = Unit
    override suspend fun deleteProjectGroups(groups: List<ProjectGroupEntity>) = Unit
    override suspend fun deleteProjectSteps(steps: List<ProjectStepEntity>) = Unit
    override suspend fun deletePracticals(practicals: List<PracticalEntity>) = Unit
    override suspend fun deleteDocuments(documents: List<AcademicDocumentEntity>) = Unit
    override suspend fun deleteDirectoryPeople(people: List<DirectoryPersonEntity>) = Unit
    override suspend fun deleteNews(news: List<NewsEntity>) = Unit
    override suspend fun deleteEvents(events: List<StudentEventEntity>) = Unit
    override suspend fun clearProfile() {
        profileState.value = null
    }
    override suspend fun clearAgenda() {
        agendaState.value = emptyList()
    }
    override suspend fun clearGrades() {
        gradeState.value = emptyList()
    }
    override suspend fun clearAbsences() {
        absenceState.value = emptyList()
    }
    override suspend fun clearCourses() {
        courseState.value = emptyList()
    }
    override suspend fun clearProjects() {
        projectState.value = emptyList()
    }
    override suspend fun clearProjectGroups() {
        projectGroupState.value = emptyList()
    }
    override suspend fun clearProjectSteps() {
        projectStepState.value = emptyList()
    }
    override suspend fun clearPracticals() {
        practicalState.value = emptyList()
    }
    override suspend fun clearDocuments() {
        documentState.value = emptyList()
    }
    override suspend fun clearDirectoryPeople() {
        directoryState.value = emptyList()
    }
    override suspend fun clearNews() {
        newsState.value = emptyList()
    }
    override suspend fun clearEvents() {
        eventState.value = emptyList()
    }
}

private class RepositorySettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(
        UserSettings(
            languageTag = null,
            notifications = NotificationPreferences(true, true, true, true, true),
            calendarSyncEnabled = false,
            lastSyncAt = null
        )
    )

    override suspend fun setLanguageTag(languageTag: String?) = Unit
    override suspend fun setCalendarSyncEnabled(enabled: Boolean) = Unit
    override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
    override suspend fun setGradeNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setAbsenceNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setAgendaNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setProjectNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setDocumentNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setThemeMode(themeMode: com.elg.studly.domain.model.ThemeMode) = Unit
    override suspend fun setDynamicColorEnabled(enabled: Boolean) = Unit
    override suspend fun setAgendaColorMode(mode: com.elg.studly.domain.model.AgendaColorMode) = Unit
    override suspend fun setRefreshInterval(feature: SyncFeature, minutes: Int) = Unit
    override suspend fun setClassReminderLeadMinutes(minutes: Int) = Unit
    override suspend fun setDeadlineReminderLeadMinutes(minutes: Int) = Unit
    var lastFetched: Instant? = null
    override suspend fun lastFetchedAt(feature: SyncFeature): Instant? = lastFetched
    override suspend fun markFeatureFetched(feature: SyncFeature) = Unit
    override suspend fun markSynced() = Unit
    override suspend fun clearSyncMetadata() = Unit
}

private class RepositoryNotificationScheduler : NotificationScheduler {
    val agendaChanges = mutableListOf<String>()
    val grades = mutableListOf<String>()
    val absences = mutableListOf<String>()
    val projects = mutableListOf<String>()
    val documents = mutableListOf<String>()

    override fun ensureChannels() = Unit
    override suspend fun scheduleStudentSync(intervalMinutes: Long) = Unit
    override suspend fun runStudentSyncNow() = Unit
    override suspend fun cancelStudentSync() = Unit
    override suspend fun scheduleReminders(targets: List<ReminderTarget>, classLeadMinutes: Int, deadlineLeadMinutes: Int) = Unit
    override suspend fun showSyncFailure() = Unit
    override suspend fun showNewGrade(grade: Grade) {
        grades += grade.id
    }
    override suspend fun showNewAbsence(absence: Absence) {
        absences += absence.id
    }
    override suspend fun showAgendaChange(event: AgendaEvent) {
        agendaChanges += event.id
    }
    override suspend fun showProjectDeadline(project: Project) {
        projects += project.id
    }
    override suspend fun showNewDocument(document: AcademicDocument) {
        documents += document.id
    }
}

private fun jsonElement(value: String): JsonElement {
    return Json.parseToJsonElement(value)
}

private fun agendaEntity(id: String) = AgendaEventEntity(id, "Old", 1L, 2L, null, null, null, null, null)
private fun gradeEntity(id: String) = GradeEntity(id, "Old", "Old", 10.0, 20.0, null, null, null, null)
private fun absenceEntity(id: String) = AbsenceEntity(id, "Old", 1L, 2L, false, null, null)
private fun projectEntity(id: String) = ProjectEntity(id, "Old", null, null, null, null, 0, null, null)
private fun documentEntity(id: String) = AcademicDocumentEntity(id, "Old", null, null, null, "old.pdf", null, null, null, null, null)
