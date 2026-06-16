package com.elg.studly.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SessionTest {
    @Test
    fun sessionRequiresRefreshAfterConfiguredRefreshDate() {
        val now = Instant.now()
        val session = Session(
            username = "Kordis",
            accessToken = "token",
            refreshToken = null,
            expiresAt = now.plusSeconds(2 * 24 * 60 * 60),
            biometricEnabled = true,
            issuedAt = now.minusSeconds(5 * 24 * 60 * 60),
            refreshAfter = now.minusSeconds(1)
        )

        assertTrue(session.requiresRefresh)
        assertFalse(session.isExpired)
    }

    @Test
    fun sessionExpiresAfterConfiguredExpiryDate() {
        val now = Instant.now()
        val session = Session(
            username = "Kordis",
            accessToken = "token",
            refreshToken = null,
            expiresAt = now.minusSeconds(1),
            biometricEnabled = true,
            issuedAt = now.minusSeconds(7 * 24 * 60 * 60),
            refreshAfter = now.minusSeconds(2 * 24 * 60 * 60)
        )

        assertTrue(session.isExpired)
    }
}
