package com.sudutcorp.sudutbulat.core.util

import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OutletMapUtilsTest {

    @Test
    fun orDefaultMedanHandlesNullZeroAndInvalidCoordinates() {
        val nullResolved = (null as GeoPoint?).orDefaultMedan()
        assertEquals(DEFAULT_MEDAN_LAT, nullResolved.latitude, 1e-6)
        assertEquals(DEFAULT_MEDAN_LNG, nullResolved.longitude, 1e-6)

        val zeroResolved = GeoPoint(0.0, 0.0).orDefaultMedan()
        assertEquals(DEFAULT_MEDAN_LAT, zeroResolved.latitude, 1e-6)
        assertEquals(DEFAULT_MEDAN_LNG, zeroResolved.longitude, 1e-6)

        assertFalse(isValidOutletCoordinate(Double.NaN, 98.67))
        assertFalse(isValidOutletCoordinate(95.0, 98.67))
        assertFalse(isValidOutletCoordinate(3.59, 190.0))
        assertFalse(isValidOutletCoordinate(0.0, 0.0))
    }

    @Test
    fun orDefaultMedanPreservesValidOutletCoordinate() {
        val sudirman = GeoPoint(3.5778, 98.6749)
        val resolved = sudirman.orDefaultMedan()
        assertEquals(3.5778, resolved.latitude, 1e-6)
        assertEquals(98.6749, resolved.longitude, 1e-6)
        assertTrue(isValidOutletCoordinate(resolved.latitude, resolved.longitude))
        assertEquals("3.5778, 98.6749", formatCoordinate(resolved.latitude, resolved.longitude))
    }

    @Test
    fun googleMapsUrisUseResolvedCoordinateAndEncodedLabel() {
        val appUri = buildGoogleMapsAppUri(3.5778, 98.6749, "Outlet Sudirman")
        assertEquals("geo:3.577800,98.674900?q=3.577800,98.674900(Outlet+Sudirman)", appUri)

        val webUrl = buildGoogleMapsWebUrl(0.0, 0.0)
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=3.595200,98.672200",
            webUrl
        )
    }
}
