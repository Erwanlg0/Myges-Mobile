package com.elg.studly.adapters.secondary.api

import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.domain.model.Session
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant
import com.elg.studly.adapters.time.*

class MygesAuthInterceptorTest {
    @Test
    fun interceptorAddsBearerSchemeForRawToken() {
        val sessionRepository = RecordingSessionRepository(sampleSession("raw-token"))
        val interceptor = MygesAuthInterceptor("agent", "https://api.kordis.fr/", sessionRepository)
        val chain = mockChain(200)

        interceptor.intercept(chain)

        verify {
            chain.proceed(withArg { request ->
                assertEquals("Bearer raw-token", request.header("Authorization"))
                assertEquals("application/json", request.header("Accept"))
                assertEquals("agent", request.header("User-Agent"))
            })
        }
    }

    @Test
    fun interceptorKeepsExistingAuthorizationScheme() {
        val sessionRepository = RecordingSessionRepository(sampleSession("bearer token"))
        val interceptor = MygesAuthInterceptor("agent", "https://api.kordis.fr/", sessionRepository)
        val chain = mockChain(200)

        interceptor.intercept(chain)

        verify {
            chain.proceed(withArg { request ->
                assertEquals("Bearer token", request.header("Authorization"))
            })
        }
    }

    @Test
    fun interceptorInvalidatesSessionWhenRefreshDatePassed() {
        val session = sampleSession(
            token = "token",
            refreshAfter = kotlin.time.Clock.System.now().minusSeconds(1)
        )
        val sessionRepository = RecordingSessionRepository(session)
        val interceptor = MygesAuthInterceptor("agent", "https://api.kordis.fr/", sessionRepository)
        val chain = mockChain(200)

        val response = interceptor.intercept(chain)

        assertEquals(401, response.code)
        assertEquals(1, sessionRepository.invalidations)
        verify(exactly = 0) { chain.proceed(any()) }
    }

    @Test
    fun interceptorDoesNotAttachTokenForForeignHost() {
        val session = sampleSession(
            token = "token",
            refreshAfter = kotlin.time.Clock.System.now().minusSeconds(1)
        )
        val sessionRepository = RecordingSessionRepository(session)
        val interceptor = MygesAuthInterceptor("agent", "https://api.kordis.fr/", sessionRepository)
        val chain = mockChain(200, url = "https://files.example.com/document.pdf")

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(0, sessionRepository.invalidations)
        verify {
            chain.proceed(withArg { request ->
                assertNull(request.header("Authorization"))
                assertEquals("agent", request.header("User-Agent"))
            })
        }
    }

    @Test
    fun interceptorInvalidatesSessionOnRemoteUnauthorized() {
        val sessionRepository = RecordingSessionRepository(sampleSession("token"))
        val interceptor = MygesAuthInterceptor("agent", "https://api.kordis.fr/", sessionRepository)
        val chain = mockChain(401)

        interceptor.intercept(chain)

        assertEquals(1, sessionRepository.invalidations)
    }
}

private fun mockChain(statusCode: Int, url: String = "https://api.kordis.fr/me/profile"): Interceptor.Chain {
    val chain = mockk<Interceptor.Chain>()
    val request = Request.Builder()
        .url(url)
        .build()
    val requestSlot = slot<Request>()
    every { chain.request() } returns request
    every { chain.proceed(capture(requestSlot)) } answers {
        Response.Builder()
            .request(requestSlot.captured)
            .protocol(Protocol.HTTP_1_1)
            .code(statusCode)
            .message("HTTP $statusCode")
            .body("".toResponseBody(null))
            .build()
    }
    return chain
}

private class RecordingSessionRepository(
    private var current: Session?
) : SessionRepository {
    var invalidations = 0
    override val session: StateFlow<Session?> = MutableStateFlow(current)
    override val hasLockedBiometricSession: StateFlow<Boolean> = MutableStateFlow(false)

    override fun currentSession(): Session? = current

    override fun invalidateSession() {
        invalidations++
        current = null
    }

    override suspend fun authenticateWithToken(accessToken: String, expiresAt: Instant?, enableBiometric: Boolean) = Unit
    override suspend fun unlockWithBiometrics() = Unit
    override suspend fun logout() = Unit
}

private fun sampleSession(
    token: String,
    refreshAfter: Instant = kotlin.time.Clock.System.now().plusSeconds(3600)
): Session {
    val now = kotlin.time.Clock.System.now()
    return Session(
        username = "Kordis",
        accessToken = token,
        refreshToken = null,
        expiresAt = now.plusSeconds(7200),
        biometricEnabled = false,
        issuedAt = now,
        refreshAfter = refreshAfter
    )
}
