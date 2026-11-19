package com.sudutcorp.sudutbulat.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationGuardsTest {

    @Test
    fun `first click passes and rapid clicks within cooldown are rejected`() {
        var fakeNow = 1_000L
        val throttle = NavigationThrottle(cooldownMs = 400L, nowMs = { fakeNow })

        // First tap succeeds
        assertTrue(throttle.tryAcquire())

        // 4 rapid taps within 350ms are all dropped
        fakeNow = 1_050L
        assertFalse(throttle.tryAcquire())
        fakeNow = 1_120L
        assertFalse(throttle.tryAcquire())
        fakeNow = 1_250L
        assertFalse(throttle.tryAcquire())
        fakeNow = 1_399L
        assertFalse(throttle.tryAcquire())

        // Tap at exactly cooldown (400ms later) succeeds again
        fakeNow = 1_400L
        assertTrue(throttle.tryAcquire())

        // Immediate subsequent tap is dropped again
        fakeNow = 1_500L
        assertFalse(throttle.tryAcquire())
    }
}
