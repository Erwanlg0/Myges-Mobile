package com.elg.studly.adapters.secondary.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationGroupingTest {
    @Test
    fun notificationGroupKeyNormalizesRouteAndSubject() {
        assertEquals(
            "Studly.grades.algorithmique-avancée",
            notificationGroupKey("grades", "  Algorithmique   Avancée  ")
        )
    }

    @Test
    fun notificationGroupKeyFallsBackToGeneral() {
        assertEquals("Studly.documents.general", notificationGroupKey("documents", " "))
        assertEquals("Studly.documents.general", notificationGroupKey("documents", null))
    }

    @Test
    fun stableNotificationIdAlwaysReturnsPositiveIdentifier() {
        assertTrue(stableNotificationId("grade:123") > 0)
    }
}
