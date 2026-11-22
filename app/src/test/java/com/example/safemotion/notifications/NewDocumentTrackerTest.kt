package com.example.safemotion.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class NewDocumentTrackerTest {
    @Test
    fun oldDocumentsAreSilentAndNewOnesNotifyEvenInFirstSnapshot() {
        val tracker = NewDocumentTracker(1_000)
        assertEquals(listOf("new"), tracker.newIds(mapOf("old" to 900L, "new" to 1_001L)))
        assertEquals(emptyList<String>(), tracker.newIds(mapOf("old" to 900L, "new" to 1_001L)))
    }

    @Test
    fun anInvitationCanNotifyAgainAfterLeavingPendingState() {
        val tracker = NewDocumentTracker(1_000)
        assertEquals(emptyList<String>(), tracker.newIds(mapOf("invitation" to 900L)))
        tracker.newIds(emptyMap())
        assertEquals(listOf("invitation"), tracker.newIds(mapOf("invitation" to 1_100L)))
    }

    @Test
    fun pendingServerTimestampNotifiesWhenResolved() {
        val tracker = NewDocumentTracker(1_000)
        assertEquals(emptyList<String>(), tracker.newIds(mapOf("alert" to null)))
        assertEquals(listOf("alert"), tracker.newIds(mapOf("alert" to 1_050L)))
    }
}
