package com.elg.studly.adapters.primary.viewmodel

import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.domain.model.Session
import com.elg.studly.domain.model.UserSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

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
    fun sessionFlowIsExposed() = runTest(dispatcher) {
        val sessionFlow = MutableStateFlow<Session?>(null)
        val sessionRepository = mockk<SessionRepository> {
            every { session } returns sessionFlow
        }
        val settingsRepository = mockk<SettingsRepository> {
            every { settings } returns MutableStateFlow(mockk<UserSettings>(relaxed = true))
        }

        val viewModel = AppViewModel(sessionRepository, settingsRepository)
        
        val job = backgroundScope.launch { viewModel.session.collect {} }
        val expectedSession = Session("user", "token", null, Instant.now(), false, Instant.now(), Instant.now())
        sessionFlow.value = expectedSession
        
        advanceUntilIdle()
        
        assertEquals(expectedSession, viewModel.session.value)
    }

    @Test
    fun settingsFlowIsExposed() = runTest(dispatcher) {
        val expectedSettings = mockk<UserSettings>(relaxed = true)
        val settingsFlow = MutableStateFlow<UserSettings>(expectedSettings)
        val sessionRepository = mockk<SessionRepository> {
            every { session } returns MutableStateFlow(null)
        }
        val settingsRepository = mockk<SettingsRepository> {
            every { settings } returns settingsFlow
        }

        val viewModel = AppViewModel(sessionRepository, settingsRepository)
        val job = backgroundScope.launch { viewModel.settings.collect {} }
        
        advanceUntilIdle()
        
        assertEquals(expectedSettings, viewModel.settings.value)
    }
}
