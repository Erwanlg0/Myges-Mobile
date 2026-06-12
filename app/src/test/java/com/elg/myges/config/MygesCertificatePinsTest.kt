package com.elg.myges.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MygesCertificatePinsTest {
    @Test
    fun apiAndAuthenticationHostsHaveCertificatePins() {
        assertEquals(setOf("api.kordis.fr", "authentication.kordis.fr"), MygesCertificatePins.pins.keys)
        MygesCertificatePins.pins.values.forEach { pins ->
            assertTrue(pins.size >= 2)
            assertEquals(pins.size, pins.distinct().size)
            assertTrue(pins.all { it.startsWith("sha256/") })
        }
    }

    @Test
    fun certificatePinnerBuildsFromConfiguredPins() {
        MygesCertificatePins.certificatePinner()
    }
}
