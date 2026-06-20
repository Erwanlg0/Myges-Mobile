package com.elg.studly.adapters.primary.viewmodel

import com.elg.studly.application.usecase.ObserveSessionUseCase
import com.elg.studly.application.usecase.ObserveSettingsUseCase
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
import kotlin.time.Instant
import com.elg.studly.adapters.time.*

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
        val observeSession = mockk<ObserveSessionUseCase> {
            every { this@mockk.invoke() } returns sessionFlow
        }
        val observeSettings = mockk<ObserveSettingsUseCase> {
            every { this@mockk.invoke() } returns MutableStateFlow(mockk<UserSettings>(relaxed = true))
        }

        val viewModel = AppViewModel(observeSession, observeSettings)
        
        val job = backgroundScope.launch { viewModel.session.collect {} }
        val expectedSession = Session("user", "token", null, kotlin.time.Clock.System.now(), false, kotlin.time.Clock.System.now(), kotlin.time.Clock.System.now())
        sessionFlow.value = expectedSession
        
        advanceUntilIdle()
        
        assertEquals(expectedSession, viewModel.session.value)
    }

    @Test
    fun settingsFlowIsExposed() = runTest(dispatcher) {
        val expectedSettings = mockk<UserSettings>(relaxed = true)
        val settingsFlow = MutableStateFlow<UserSettings>(expectedSettings)
        val observeSession = mockk<ObserveSessionUseCase> {
            every { this@mockk.invoke() } returns MutableStateFlow(null)
        }
        val observeSettings = mockk<ObserveSettingsUseCase> {
            every { this@mockk.invoke() } returns settingsFlow
        }

        val viewModel = AppViewModel(observeSession, observeSettings)
        val job = backgroundScope.launch { viewModel.settings.collect {} }
        
        advanceUntilIdle()
        
        assertEquals(expectedSettings, viewModel.settings.value)
    }
}
