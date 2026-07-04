package com.elg.studly.adapters.secondary.repository

import com.elg.studly.adapters.secondary.security.SecureSessionStore
import com.elg.studly.domain.model.AppError
import com.elg.studly.domain.model.AppException
import com.elg.studly.domain.model.Session
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.days
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant
import com.elg.studly.adapters.time.*
import java.time.temporal.ChronoUnit

class MygesSessionRepositoryTest {

    @Test
    fun initializesWithNoSessionWhenStoreIsEmpty() = runTest {
        val secureStore = mockk<SecureSessionStore> {
            every { read() } returns null
            every { clear() } returns Unit
        }
        val repository = MygesSessionRepository(secureStore)

        assertEquals(null, repository.session.first())
        assertFalse(repository.hasLockedBiometricSession.first())
    }

    @Test
    fun initializesWithSessionWhenStoreHasValidSession() = runTest {
        val session = Session(
            username = "user",
            accessToken = "token",
            refreshToken = null,
            expiresAt = kotlin.time.Clock.System.now().plusSeconds(3600),
            biometricEnabled = false,
            issuedAt = kotlin.time.Clock.System.now(),
            refreshAfter = kotlin.time.Clock.System.now().plusSeconds(1800)
        )
        val secureStore = mockk<SecureSessionStore> {
            every { read() } returns session
        }
        val repository = MygesSessionRepository(secureStore)

        assertEquals(session, repository.session.first())
        assertFalse(repository.hasLockedBiometricSession.first())
    }

    @Test
    fun clearsStoreAndInitializesEmptyWhenSessionExpired() = runTest {
        val session = Session(
            username = "user",
            accessToken = "token",
            refreshToken = null,
            expiresAt = kotlin.time.Clock.System.now().minusSeconds(3600),
            biometricEnabled = false,
            issuedAt = kotlin.time.Clock.System.now(),
            refreshAfter = kotlin.time.Clock.System.now().plusSeconds(1800)
        )
        val secureStore = mockk<SecureSessionStore> {
            every { read() } returns session
            every { clear() } returns Unit
        }
        val repository = MygesSessionRepository(secureStore)

        assertEquals(null, repository.session.first())
        verify { secureStore.clear() }
    }

    @Test
    fun biometricSessionIsLockedOnInit() = runTest {
        val session = Session(
            username = "user",
            accessToken = "token",
            refreshToken = null,
            expiresAt = kotlin.time.Clock.System.now().plusSeconds(3600),
            biometricEnabled = true,
            issuedAt = kotlin.time.Clock.System.now(),
            refreshAfter = kotlin.time.Clock.System.now().plusSeconds(1800)
        )
        val secureStore = mockk<SecureSessionStore> {
            every { read() } returns session
        }
        val repository = MygesSessionRepository(secureStore)

        assertEquals(null, repository.session.first())
        assertTrue(repository.hasLockedBiometricSession.first())
    }

    @Test
    fun authenticateWithTokenSavesAndExposesSession() = runTest {
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returns null
        }
        val repository = MygesSessionRepository(secureStore)

        val expiresAt = kotlin.time.Clock.System.now().plusSeconds(3600)
        repository.authenticateWithToken("new_token", expiresAt, false)

        val newSession = repository.session.first()
        assertEquals("new_token", newSession?.accessToken)
        assertEquals(expiresAt, newSession?.expiresAt)
        assertFalse(newSession?.biometricEnabled ?: true)
        
        verify { secureStore.save(any()) }
    }

    @Test
    fun authenticateWithTokenUsesDefaultExpiry() = runTest {
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returns null
        }
        val repository = MygesSessionRepository(secureStore)

        repository.authenticateWithToken("new_token", null, true)

        val newSession = repository.session.first()
        assertEquals("new_token", newSession?.accessToken)
        assertTrue(newSession?.biometricEnabled ?: false)
        assertEquals(false, repository.hasLockedBiometricSession.first())
    }

    @Test
    fun authenticateWithBlankTokenThrowsUnauthorized() = runTest {
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returns null
        }
        val repository = MygesSessionRepository(secureStore)

        val failure = runCatching {
            repository.authenticateWithToken(" ", null, false)
        }.exceptionOrNull()

        assertEquals(AppError.Unauthorized, (failure as AppException).error)
    }

    @Test
    fun authenticateStoreFailureBecomesUnexpectedError() = runTest {
        val secureStore = mockk<SecureSessionStore> {
            every { read() } returns null
            every { save(any()) } throws IllegalStateException("disk")
        }
        val repository = MygesSessionRepository(secureStore)

        val failure = runCatching {
            repository.authenticateWithToken("token", null, false)
        }.exceptionOrNull()

        assertEquals(AppError.Unexpected("disk"), (failure as AppException).error)
    }

    @Test
    fun unlockWithBiometricsExposesSession() = runTest {
        val session = Session(
            username = "user",
            accessToken = "token",
            refreshToken = null,
            expiresAt = kotlin.time.Clock.System.now() + 10.days,
            biometricEnabled = true,
            issuedAt = kotlin.time.Clock.System.now(),
            refreshAfter = kotlin.time.Clock.System.now() + 5.days
        )
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returns session
        }
        val repository = MygesSessionRepository(secureStore)

        assertTrue(repository.hasLockedBiometricSession.first())
        assertEquals(null, repository.session.first())

        repository.unlockWithBiometrics()

        assertFalse(repository.hasLockedBiometricSession.first())
        assertEquals(session, repository.session.first())
    }

    @Test
    fun unlockWithBiometricsWithoutStoredSessionFails() = runTest {
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returns null
        }
        val repository = MygesSessionRepository(secureStore)

        val failure = runCatching { repository.unlockWithBiometrics() }.exceptionOrNull()

        assertEquals(AppError.Unauthorized, (failure as AppException).error)
    }

    @Test
    fun unlockWithBiometricsClearsExpiredStoredSession() = runTest {
        val session = Session(
            username = "user",
            accessToken = "token",
            refreshToken = null,
            expiresAt = kotlin.time.Clock.System.now().minusSeconds(1),
            biometricEnabled = true,
            issuedAt = kotlin.time.Clock.System.now(),
            refreshAfter = kotlin.time.Clock.System.now().plusSeconds(1800)
        )
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returnsMany listOf(null, session)
        }
        val repository = MygesSessionRepository(secureStore)

        val failure = runCatching { repository.unlockWithBiometrics() }.exceptionOrNull()

        assertEquals(AppError.Unauthorized, (failure as AppException).error)
        assertEquals(null, repository.session.first())
        verify { secureStore.clear() }
    }

    @Test
    fun invalidateSessionClearsSession() = runTest {
        val session = Session(
            username = "user",
            accessToken = "token",
            refreshToken = null,
            expiresAt = kotlin.time.Clock.System.now().plusSeconds(3600),
            biometricEnabled = false,
            issuedAt = kotlin.time.Clock.System.now(),
            refreshAfter = kotlin.time.Clock.System.now().plusSeconds(1800)
        )
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returns session
        }
        val repository = MygesSessionRepository(secureStore)

        repository.invalidateSession()

        assertEquals(null, repository.currentSession())
        assertEquals(null, repository.session.first())
        assertFalse(repository.hasLockedBiometricSession.first())
        verify { secureStore.clear() }
    }

    @Test
    fun readFailureClearsStoreAndInitializesEmpty() = runTest {
        val secureStore = mockk<SecureSessionStore> {
            every { read() } throws IllegalStateException("disk")
            every { clear() } returns Unit
        }
        val repository = MygesSessionRepository(secureStore)

        assertEquals(null, repository.session.first())
        verify { secureStore.clear() }
    }

    @Test
    fun logoutClearsSession() = runTest {
        val session = Session(
            username = "user",
            accessToken = "token",
            refreshToken = null,
            expiresAt = kotlin.time.Clock.System.now().plusSeconds(3600),
            biometricEnabled = false,
            issuedAt = kotlin.time.Clock.System.now(),
            refreshAfter = kotlin.time.Clock.System.now().plusSeconds(1800)
        )
        val secureStore = mockk<SecureSessionStore>(relaxed = true) {
            every { read() } returns session
        }
        val repository = MygesSessionRepository(secureStore)

        assertEquals(session, repository.session.first())

        repository.logout()

        assertEquals(null, repository.session.first())
        verify { secureStore.clear() }
    }
}
