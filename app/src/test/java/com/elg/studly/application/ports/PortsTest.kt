package com.elg.studly.application.ports

import android.net.Uri
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DashboardSummary
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.StudentEvent
import com.elg.studly.domain.model.SyncFeature
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PortsTest {
    @Test
    fun studentDataRepositoryProjectMessagesDefaultsToEmptyList() = runTest {
        assertEquals(emptyList<Any>(), TestStudentDataRepository().projectMessages("group-1"))
    }

    @Test
    fun studentDataRepositorySendProjectMessageDefaultsToUnit() = runTest {
        assertEquals(Unit, TestStudentDataRepository().sendProjectMessage("group-1", "hello"))
    }

    @Test
    fun studentDataRepositorySyncAllDefaultsToNoForceAndAllFeatures() = runTest {
        val repository = TestStudentDataRepository()

        repository.syncAll()

        assertEquals(false, repository.force)
        assertEquals(null, repository.features)
    }

    @Test
    fun studentDataRepositoryDownloadDocumentDefaultProgressDoesNothing() = runTest {
        val repository = TestStudentDataRepository()

        assertSame(repository.uri, repository.downloadDocument(AcademicDocument("doc", "Doc", null, null, null, "doc.pdf", null, null)))
    }
}

private class TestStudentDataRepository : StudentDataRepository {
    var force: Boolean? = null
    var features: Set<SyncFeature>? = emptySet()
    val uri: Uri = mockk()

    override fun observeDashboard(): Flow<DashboardSummary> = error("unused")
    override fun observeAgenda(): Flow<List<AgendaEvent>> = flowOf(emptyList())
    override fun observeGrades(): Flow<List<Grade>> = flowOf(emptyList())
    override fun observeAbsences(): Flow<List<Absence>> = flowOf(emptyList())
    override fun observeCourses(): Flow<List<Course>> = flowOf(emptyList())
    override fun observeProjects(): Flow<List<Project>> = flowOf(emptyList())
    override fun observePracticals(): Flow<List<Practical>> = flowOf(emptyList())
    override fun observeDocuments(): Flow<List<AcademicDocument>> = flowOf(emptyList())
    override fun observeDirectory(): Flow<List<DirectoryPerson>> = flowOf(emptyList())
    override fun observeNews(): Flow<List<NewsItem>> = flowOf(emptyList())
    override fun observeEvents(): Flow<List<StudentEvent>> = flowOf(emptyList())
    override suspend fun syncAll(force: Boolean, features: Set<SyncFeature>?) {
        this.force = force
        this.features = features
    }
    override suspend fun clearCache() = Unit
    override suspend fun downloadDocument(document: AcademicDocument, onProgress: (Float?) -> Unit): Uri = uri
    override suspend fun joinGroup(courseId: String, projectId: String, groupId: String) = Unit
    override suspend fun leaveGroup(courseId: String, projectId: String, groupId: String) = Unit
    override suspend fun subscribeEvent(eventId: String) = Unit
    override suspend fun unsubscribeEvent(eventId: String) = Unit
}
