package com.elg.myges.adapters.secondary.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationGroupingTest {
    @Test
    fun notificationGroupKeyNormalizesRouteAndSubject() {
        assertEquals(
            "myges.grades.algorithmique-avancée",
            notificationGroupKey("grades", "  Algorithmique   Avancée  ")
        )
    }

    @Test
    fun notificationGroupKeyFallsBackToGeneral() {
        assertEquals("myges.documents.general", notificationGroupKey("documents", " "))
        assertEquals("myges.documents.general", notificationGroupKey("documents", null))
    }

    @Test
    fun stableNotificationIdAlwaysReturnsPositiveIdentifier() {
        assertTrue(stableNotificationId("grade:123") > 0)
    }
}
