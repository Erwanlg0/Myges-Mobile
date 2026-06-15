package com.elg.myges.adapters.primary.viewmodel

import android.net.Uri
import com.elg.myges.application.ports.NotificationScheduler
import com.elg.myges.application.ports.SessionRepository
import com.elg.myges.application.usecase.CompleteOAuthLoginUseCase
import com.elg.myges.application.usecase.ObserveLockedBiometricSessionUseCase
import com.elg.myges.application.usecase.UnlockWithBiometricsUseCase
import com.elg.myges.config.AppConfig
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.AppError
import com.elg.myges.domain.model.AppException
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completeOAuthCallbackStoresBearerTokenAndBiometricPreference() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val notificationScheduler = AuthRecordingNotificationScheduler()
        val viewModel = authViewModel(sessionRepository, notificationScheduler)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(
            oauthUri("access_token=token-1&token_type=Bearer&expires_in=3600")
        )
        advanceUntilIdle()

        assertEquals("bearer token-1", sessionRepository.accessToken)
        assertFalse(sessionRepository.enableBiometric)
        assertNotNull(sessionRepository.expiresAt)
        assertEquals(true, notificationScheduler.channelsEnsured)
        assertEquals(true, notificationScheduler.syncScheduled)
        assertFalse(viewModel.state.value.loading)
        assertEquals(null, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackWithoutAccessTokenExposesUnauthorizedError() = runTest(dispatcher) {
        val viewModel = authViewModel()
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("error=access_denied"))
        advanceUntilIdle()

        assertEquals(AppError.Unauthorized, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun lockedBiometricSessionIsReflectedInState() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        sessionRepository.lockedBiometricSession.value = true
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.hasBiometricSession)
        stateCollection.cancel()
    }

    @Test
    fun unlockWithBiometricsDelegatesToSessionRepository() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)

        viewModel.unlockWithBiometrics()
        advanceUntilIdle()

        assertEquals(1, sessionRepository.unlockCount)
    }

    @Test
    fun unlockFailureIsExposedAsUiError() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository().apply {
            unlockFailure = AppException(AppError.Unauthorized)
        }
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.unlockWithBiometrics()
        advanceUntilIdle()

        assertEquals(AppError.Unauthorized, viewModel.state.value.error)
        assertFalse(viewModel.state.value.loading)
        stateCollection.cancel()
    }

    private fun collectState(viewModel: AuthViewModel): Job {
        return kotlinx.coroutines.CoroutineScope(dispatcher).launch {
            viewModel.state.collect()
        }
    }

    private fun authViewModel(
        sessionRepository: AuthRecordingSessionRepository = AuthRecordingSessionRepository(),
        notificationScheduler: AuthRecordingNotificationScheduler = AuthRecordingNotificationScheduler()
    ): AuthViewModel {
        return AuthViewModel(
            ObserveLockedBiometricSessionUseCase(sessionRepository),
            CompleteOAuthLoginUseCase(sessionRepository, notificationScheduler),
            UnlockWithBiometricsUseCase(sessionRepository),
            mockk<AppConfig> {
                every { oauthAuthorizeUrl } returns "https://authentication.example/oauth"
            }
        )
    }

    private fun oauthUri(fragment: String): Uri {
        return mockk {
            every { getQueryParameter(any()) } returns null
            every { encodedFragment } returns fragment
        }
    }
}

private class AuthRecordingSessionRepository : SessionRepository {
    var accessToken: String? = null
    var expiresAt: Instant? = null
    var enableBiometric = false
    var unlockCount = 0
    var unlockFailure: Throwable? = null
    override val session: Flow<Session?> = MutableStateFlow(null)
    val lockedBiometricSession = MutableStateFlow(false)
    override val hasLockedBiometricSession: Flow<Boolean> = lockedBiometricSession

    override fun currentSession(): Session? = null
    override fun invalidateSession() = Unit

    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        this.accessToken = accessToken
        this.expiresAt = expiresAt
        this.enableBiometric = enableBiometric
    }

    override suspend fun unlockWithBiometrics() {
        unlockFailure?.let { throw it }
        unlockCount += 1
    }

    override suspend fun logout() = Unit
}

private class AuthRecordingNotificationScheduler : NotificationScheduler {
    var channelsEnsured = false
    var syncScheduled = false

    override fun ensureChannels() {
        channelsEnsured = true
    }

    override suspend fun scheduleStudentSync() {
        syncScheduled = true
    }

    override suspend fun cancelStudentSync() = Unit
    override suspend fun showSyncFailure() = Unit
    override suspend fun showNewGrade(grade: Grade) = Unit
    override suspend fun showNewAbsence(absence: Absence) = Unit
    override suspend fun showAgendaChange(event: AgendaEvent) = Unit
    override suspend fun showProjectDeadline(project: Project) = Unit
    override suspend fun showNewDocument(document: AcademicDocument) = Unit
}
