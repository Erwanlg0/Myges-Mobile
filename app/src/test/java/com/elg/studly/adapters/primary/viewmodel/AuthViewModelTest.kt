package com.elg.studly.adapters.primary.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.usecase.CompleteOAuthLoginUseCase
import com.elg.studly.config.AppConfig
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NotificationPreferences
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ReminderTarget
import com.elg.studly.domain.model.Session
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.UserSettings
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
        assertEquals("https://authentication.example/oauth", viewModel.state.value.authorizationUrl)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackReadsQueryParameters() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(queryOauthUri("access_token" to "token-1", "token_type" to "Bearer"))
        advanceUntilIdle()

        assertEquals("bearer token-1", sessionRepository.accessToken)
        assertEquals(null, sessionRepository.expiresAt)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackKeepsTokenWithWhitespaceOrBlankType() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("access_token=already%20typed&token_type="))
        advanceUntilIdle()

        assertEquals("already typed", sessionRepository.accessToken)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackKeepsTokenWithoutType() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1"))
        advanceUntilIdle()

        assertEquals("token-1", sessionRepository.accessToken)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackAcceptsProviderWithoutReturnedState() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1", state = null))
        advanceUntilIdle()

        assertEquals("token-1", sessionRepository.accessToken)
    }

    @Test
    fun completeOAuthCallbackWithInvalidExpiresInStoresNoExpiry() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1&expires_in=soon"))
        advanceUntilIdle()

        assertEquals(null, sessionRepository.expiresAt)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackWithNegativeExpiresInStoresNoExpiry() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1&expires_in=-1"))
        advanceUntilIdle()

        assertEquals("token-1", sessionRepository.accessToken)
        assertEquals(null, sessionRepository.expiresAt)
    }

    @Test
    fun completeOAuthCallbackWithOverflowingExpiresInStoresNoExpiry() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1&expires_in=${Long.MAX_VALUE}"))
        advanceUntilIdle()

        assertEquals("token-1", sessionRepository.accessToken)
        assertEquals(null, sessionRepository.expiresAt)
    }

    @Test
    fun oauthStateSurvivesViewModelRecreation() = runTest(dispatcher) {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = authViewModel(savedStateHandle = savedStateHandle)
        val authorizationUrl = firstViewModel.beginOAuthLogin()
        val oauthState = authorizationUrl.substringAfter("state=").substringBefore('&')
        val sessionRepository = AuthRecordingSessionRepository()
        val recreatedViewModel = authViewModel(sessionRepository, savedStateHandle = savedStateHandle)

        recreatedViewModel.completeOAuthCallback(oauthUri("access_token=token-1", oauthState))
        advanceUntilIdle()

        assertEquals("token-1", sessionRepository.accessToken)
    }

    @Test
    fun oauthStateCanOnlyBeUsedOnce() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1"))
        advanceUntilIdle()
        viewModel.completeOAuthCallback(oauthUri("access_token=token-1"))
        advanceUntilIdle()

        assertEquals(1, sessionRepository.authenticateCount)
        assertEquals(AppError.LoginFailed, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun mismatchedOAuthStateIsRejected() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository()
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1", "other-state"))
        advanceUntilIdle()

        assertEquals(0, sessionRepository.authenticateCount)
        assertEquals(AppError.LoginFailed, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackFailureIsExposedAsUiError() = runTest(dispatcher) {
        val sessionRepository = AuthRecordingSessionRepository().apply {
            authenticateFailure = AppException(AppError.Unauthorized)
        }
        val viewModel = authViewModel(sessionRepository)
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("access_token=token-1"))
        advanceUntilIdle()

        assertEquals(AppError.Unauthorized, viewModel.state.value.error)
        assertFalse(viewModel.state.value.loading)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackWithoutFragmentExposesUnauthorizedError() = runTest(dispatcher) {
        val viewModel = authViewModel()
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri(null))
        advanceUntilIdle()

        assertEquals(AppError.LoginFailed, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackWithoutAccessTokenExposesUnauthorizedError() = runTest(dispatcher) {
        val viewModel = authViewModel()
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("error=access_denied"))
        advanceUntilIdle()

        assertEquals(AppError.LoginFailed, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackWithBlankAccessTokenExposesUnauthorizedError() = runTest(dispatcher) {
        val viewModel = authViewModel()
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(queryOauthUri("access_token" to ""))
        advanceUntilIdle()

        assertEquals(AppError.LoginFailed, viewModel.state.value.error)
        stateCollection.cancel()
    }

    @Test
    fun completeOAuthCallbackReadsEmptyFragmentParameter() = runTest(dispatcher) {
        val viewModel = authViewModel()
        val stateCollection = collectState(viewModel)

        viewModel.completeOAuthCallback(oauthUri("access_token"))
        advanceUntilIdle()

        assertEquals(AppError.LoginFailed, viewModel.state.value.error)
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
        notificationScheduler: AuthRecordingNotificationScheduler = AuthRecordingNotificationScheduler(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("oauth_state" to TEST_OAUTH_STATE))
    ): AuthViewModel {
        return AuthViewModel(
            sessionRepository,
            CompleteOAuthLoginUseCase(sessionRepository, AuthStubSettingsRepository(), notificationScheduler),
            mockk<AppConfig> {
                every { oauthAuthorizeUrl } returns "https://authentication.example/oauth"
            },
            savedStateHandle
        )
    }

    private fun oauthUri(fragment: String?, state: String? = TEST_OAUTH_STATE): Uri {
        return mockk {
            every { getQueryParameter(any()) } returns null
            every { encodedFragment } returns if (fragment != null && state != null) "$fragment&state=$state" else fragment
        }
    }

    private fun queryOauthUri(vararg parameters: Pair<String, String>): Uri {
        val values = parameters.toMap() + ("state" to TEST_OAUTH_STATE)
        return mockk {
            every { getQueryParameter(any()) } answers { values[firstArg()] }
            every { encodedFragment } returns null
        }
    }

    private companion object {
        const val TEST_OAUTH_STATE = "oauth-state"
    }
}

private class AuthRecordingSessionRepository : SessionRepository {
    var accessToken: String? = null
    var expiresAt: Instant? = null
    var enableBiometric = false
    var unlockCount = 0
    var authenticateCount = 0
    var authenticateFailure: Throwable? = null
    var unlockFailure: Throwable? = null
    override val session: Flow<Session?> = MutableStateFlow(null)
    val lockedBiometricSession = MutableStateFlow(false)
    override val hasLockedBiometricSession: Flow<Boolean> = lockedBiometricSession

    override fun currentSession(): Session? = null
    override fun invalidateSession() = Unit

    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) {
        authenticateFailure?.let { throw it }
        authenticateCount += 1
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

    override suspend fun scheduleStudentSync(intervalMinutes: Long) {
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
    override suspend fun scheduleReminders(targets: List<ReminderTarget>, classLeadMinutes: Int, deadlineLeadMinutes: Int) = Unit
}

private class AuthStubSettingsRepository : SettingsRepository {
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
    override suspend fun lastFetchedAt(feature: SyncFeature): Instant? = null
    override suspend fun markFeatureFetched(feature: SyncFeature) = Unit
    override suspend fun markSynced() = Unit
    override suspend fun clearSyncMetadata() = Unit
    override suspend fun setShowGradeLetters(enabled: Boolean) = Unit
    override suspend fun setEstimateGrades(enabled: Boolean) = Unit
}
