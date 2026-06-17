package com.elg.studly.application.usecase

import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CompleteOAuthLoginUseCaseTest {
    @Test
    fun completeOAuthLoginStoresTokenAndSchedulesSync() = runTest {
        val sessionRepository = RecordingSessionRepository()
        val notificationScheduler = RecordingNotificationScheduler()
        val useCase = CompleteOAuthLoginUseCase(sessionRepository, notificationScheduler)
        val expiresAt = Instant.parse("2026-06-12T12:00:00Z")

        useCase("oauth-token", expiresAt, true)

        assertEquals("oauth-token", sessionRepository.accessToken)
        assertEquals(expiresAt, sessionRepository.expiresAt)
        assertTrue(sessionRepository.enableBiometric)
        assertTrue(notificationScheduler.channelsEnsured)
        assertTrue(notificationScheduler.syncScheduled)
    }
}

private class RecordingSessionRepository : SessionRepository {
    var accessToken: String? = null
    var expiresAt: Instant? = null
    var enableBiometric: Boolean = false
    override val session: StateFlow<Session?> = MutableStateFlow(null)
    override val hasLockedBiometricSession: StateFlow<Boolean> = MutableStateFlow(false)

    override fun currentSession(): Session? = null
    override fun invalidateSession() = Unit

    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        this.accessToken = accessToken
        this.expiresAt = expiresAt
        this.enableBiometric = enableBiometric
    }

    override suspend fun unlockWithBiometrics() = Unit
    override suspend fun logout() = Unit
}

private class RecordingNotificationScheduler : NotificationScheduler {
    var channelsEnsured = false
    var syncScheduled = false

    override fun ensureChannels() {
        channelsEnsured = true
    }

    override suspend fun scheduleStudentSync() {
        syncScheduled = true
    }

    override suspend fun runStudentSyncNow() = Unit
    override suspend fun cancelStudentSync() = Unit
    override suspend fun showSyncFailure() = Unit
    override suspend fun showNewGrade(grade: Grade) = Unit
    override suspend fun showNewAbsence(absence: Absence) = Unit
    override suspend fun showAgendaChange(event: AgendaEvent) = Unit
    override suspend fun showProjectDeadline(project: Project) = Unit
    override suspend fun showNewDocument(document: AcademicDocument) = Unit
    override suspend fun scheduleEventReminders(events: List<AgendaEvent>, leadMinutes: Int) = Unit
}
