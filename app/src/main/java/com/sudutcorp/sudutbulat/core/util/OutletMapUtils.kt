package com.sudutcorp.sudutbulat.core.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.GeoPoint
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.math.abs

const val DEFAULT_MEDAN_LAT: Double = 3.5952
const val DEFAULT_MEDAN_LNG: Double = 98.6722
const val OPEN_FREE_MAP_STYLE_URL: String = "https://tiles.openfreemap.org/styles/liberty"

val DEFAULT_MEDAN_GEOPOINT: GeoPoint = GeoPoint(DEFAULT_MEDAN_LAT, DEFAULT_MEDAN_LNG)

private const val ZERO_COORD_EPSILON = 1e-6

fun isValidOutletCoordinate(latitude: Double, longitude: Double): Boolean {
    if (latitude.isNaN() || longitude.isNaN()) return false
    if (latitude < -90.0 || latitude > 90.0) return false
    if (longitude < -180.0 || longitude > 180.0) return false
    if (abs(latitude) < ZERO_COORD_EPSILON && abs(longitude) < ZERO_COORD_EPSILON) return false
    return true
}

fun GeoPoint?.orDefaultMedan(): GeoPoint {
    if (this == null) return DEFAULT_MEDAN_GEOPOINT
    return if (isValidOutletCoordinate(latitude, longitude)) this else DEFAULT_MEDAN_GEOPOINT
}

fun resolveOutletCoordinate(latitude: Double, longitude: Double): Pair<Double, Double> =
    if (isValidOutletCoordinate(latitude, longitude)) {
        latitude to longitude
    } else {
        DEFAULT_MEDAN_LAT to DEFAULT_MEDAN_LNG
    }

fun formatCoordinate(latitude: Double, longitude: Double): String {
    val (lat, lng) = resolveOutletCoordinate(latitude, longitude)
    return String.format(Locale.US, "%.4f, %.4f", lat, lng)
}

fun buildGoogleMapsAppUri(latitude: Double, longitude: Double, label: String): String {
    val (lat, lng) = resolveOutletCoordinate(latitude, longitude)
    val cleanLabel = label.trim().ifBlank { "Outlet SudutBulat" }
    val encodedLabel = URLEncoder.encode(cleanLabel, StandardCharsets.UTF_8.name())
    return String.format(Locale.US, "geo:%.6f,%.6f?q=%.6f,%.6f(%s)", lat, lng, lat, lng, encodedLabel)
}

fun buildGoogleMapsWebUrl(latitude: Double, longitude: Double): String {
    val (lat, lng) = resolveOutletCoordinate(latitude, longitude)
    return String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f", lat, lng)
}

fun openGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String): Boolean {
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(buildGoogleMapsAppUri(latitude, longitude, label))).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(appIntent)
        true
    } catch (_: ActivityNotFoundException) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(buildGoogleMapsWebUrl(latitude, longitude))).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(webIntent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}

fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

/**
 * Fetches the best available device coordinate via Android's built-in [LocationManager].
 * Invokes [onResult] with `(lat, lng)` when a valid fix is found, or `null` otherwise.
 */
@SuppressLint("MissingPermission")
fun fetchCurrentDeviceLocation(
    context: Context,
    cancellationSignal: CancellationSignal? = null,
    onResult: (Pair<Double, Double>?) -> Unit
) {
    if (!hasLocationPermission(context)) {
        onResult(null)
        return
    }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
        onResult(null)
        return
    }

    val providers = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(LocationManager.FUSED_PROVIDER)
        }
        add(LocationManager.GPS_PROVIDER)
        add(LocationManager.NETWORK_PROVIDER)
        add(LocationManager.PASSIVE_PROVIDER)
    }.filter { provider ->
        runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
    }

    val bestLastKnown: Location? = providers
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .filter { isValidOutletCoordinate(it.latitude, it.longitude) }
        .maxByOrNull { it.time }

    if (bestLastKnown != null) {
        onResult(bestLastKnown.latitude to bestLastKnown.longitude)
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && providers.isNotEmpty()) {
        val activeProvider = providers.first()
        runCatching {
            locationManager.getCurrentLocation(
                activeProvider,
                cancellationSignal,
                context.mainExecutor
            ) { location ->
                val valid = location?.takeIf { isValidOutletCoordinate(it.latitude, it.longitude) }
                onResult(valid?.let { it.latitude to it.longitude })
            }
        }.onFailure {
            onResult(null)
        }
    } else {
        onResult(null)
    }
}
