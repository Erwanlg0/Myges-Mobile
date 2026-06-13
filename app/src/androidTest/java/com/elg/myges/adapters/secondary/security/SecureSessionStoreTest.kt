package com.elg.myges.adapters.secondary.security

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.myges.domain.model.Session
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SecureSessionStoreTest {
    private lateinit var context: Context
    private lateinit var store: SecureSessionStore

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        store = SecureSessionStore(context)
        store.clear()
    }

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun savedSessionRoundTripsWithoutPersistingPlainToken() {
        val session = Session(
            username = "Kordis",
            accessToken = "secret-access-token",
            refreshToken = "secret-refresh-token",
            expiresAt = Instant.parse("2026-06-20T12:00:00Z"),
            biometricEnabled = true,
            issuedAt = Instant.parse("2026-06-13T12:00:00Z"),
            refreshAfter = Instant.parse("2026-06-18T12:00:00Z")
        )

        store.save(session)

        assertEquals(session, store.read())
        val persistedValues = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
            .all
            .values
            .map { it.toString() }
        assertFalse(persistedValues.any { it == "secret-access-token" || it == "secret-refresh-token" })
    }

    @Test
    fun clearRemovesStoredSession() {
        store.save(
            Session(
                username = "Kordis",
                accessToken = "token",
                refreshToken = null,
                expiresAt = null,
                biometricEnabled = false,
                issuedAt = Instant.parse("2026-06-13T12:00:00Z"),
                refreshAfter = Instant.parse("2026-06-18T12:00:00Z")
            )
        )

        store.clear()

        assertNull(store.read())
    }
}
