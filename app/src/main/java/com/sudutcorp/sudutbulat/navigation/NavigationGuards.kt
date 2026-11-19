package com.sudutcorp.sudutbulat.navigation

import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import java.util.WeakHashMap

internal class NavigationThrottle(
    private val cooldownMs: Long = DEFAULT_NAV_COOLDOWN_MS,
    private val nowMs: () -> Long = { SystemClock.elapsedRealtime() }
) {
    private var lastActionAtMs: Long = Long.MIN_VALUE

    fun tryAcquire(): Boolean {
        val now = nowMs()
        if (lastActionAtMs != Long.MIN_VALUE && (now - lastActionAtMs) < cooldownMs) {
            return false
        }
        lastActionAtMs = now
        return true
    }

    companion object {
        const val DEFAULT_NAV_COOLDOWN_MS = 400L
    }
}

private val navThrottles = WeakHashMap<NavHostController, NavigationThrottle>()

private fun NavHostController.throttle(): NavigationThrottle =
    navThrottles.getOrPut(this) { NavigationThrottle() }

private fun NavHostController.isCurrentEntryResumed(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

/**
 * Navigates to [route] only if the current destination is still RESUMED and outside
 * the rapid-click cooldown window, preventing duplicate screens from stacking on fast taps.
 */
fun <T : Any> NavHostController.navigateSingle(
    route: T,
    beforeNavigate: () -> Unit = {},
    builder: NavOptionsBuilder.() -> Unit = {}
): Boolean {
    if (!isCurrentEntryResumed() || !throttle().tryAcquire()) return false
    beforeNavigate()
    navigate(route) {
        launchSingleTop = true
        builder()
    }
    return true
}

/**
 * Safely pops one backstack entry only if the current destination is RESUMED,
 * outside the rapid-click cooldown window, and a previous entry exists.
 */
fun NavHostController.popBackStackSafe(): Boolean {
    if (previousBackStackEntry == null) return false
    if (!isCurrentEntryResumed() || !throttle().tryAcquire()) return false
    return popBackStack()
}
