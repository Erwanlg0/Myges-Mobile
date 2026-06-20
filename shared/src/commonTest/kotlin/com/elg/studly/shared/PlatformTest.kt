package com.elg.studly.shared

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlatformTest {
    @Test
    fun platformNameIsSet() {
        assertTrue(platformName().isNotBlank())
    }

    // ponytail: smoke-test that kotlinx-datetime links on every target —
    // it's the dependency the Phase 2 java.time migration hinges on.
    @Test
    fun datetimeLinks() {
        assertEquals(2026, LocalDate(2026, 1, 1).year)
    }
}
