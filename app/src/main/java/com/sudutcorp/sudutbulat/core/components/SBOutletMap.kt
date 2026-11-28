package com.sudutcorp.sudutbulat.core.components

import android.Manifest
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.DEFAULT_MEDAN_LAT
import com.sudutcorp.sudutbulat.core.util.DEFAULT_MEDAN_LNG
import com.sudutcorp.sudutbulat.core.util.OPEN_FREE_MAP_STYLE_URL
import com.sudutcorp.sudutbulat.core.util.fetchCurrentDeviceLocation
import com.sudutcorp.sudutbulat.core.util.formatCoordinate
import com.sudutcorp.sudutbulat.core.util.hasLocationPermission
import com.sudutcorp.sudutbulat.core.util.resolveOutletCoordinate
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView

@Composable
private fun rememberLifecycleMapView(): MapView {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)
        // TextureView avoids SurfaceView z-order and rounded-corner clipping glitches inside scrollable containers.
        val options = MapLibreMapOptions.createFromAttributes(context).textureMode(true)
        MapView(context, options).apply {
            onCreate(null)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    return mapView
}

@Composable
private fun MapCenterPin(modifier: Modifier = Modifier) {
    // Shift upward by half height so the bottom tip of the pin aligns with the exact center coordinate.
    Icon(
        painter = painterResource(id = R.drawable.ic_location_pin),
        contentDescription = "Pin lokasi outlet",
        tint = SBColor.Primary_550,
        modifier = modifier
            .size(38.dp)
            .offset(y = (-17).dp)
    )
}

/**
 * Non-interactive MapLibre preview card centered on [latitude], [longitude] (falls back to Medan).
 * Gestures are disabled so vertical scroll containers never fight touch events.
 */
@Composable
fun SBOutletMapPreview(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    zoom: Double = 15.5,
    onClick: (() -> Unit)? = null
) {
    val (resolvedLat, resolvedLng) = resolveOutletCoordinate(latitude, longitude)
    val mapView = rememberLifecycleMapView()
    var mapLibreRef by remember(mapView) { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(mapLibreRef, resolvedLat, resolvedLng, zoom) {
        mapLibreRef?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(resolvedLat, resolvedLng))
                    .zoom(zoom)
                    .build()
            )
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SBColor.Grey_100,
        border = BorderStroke(1.dp, SBColor.Grey_200)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        ) {
            AndroidView(
                factory = {
                    mapView.apply {
                        getMapAsync { map ->
                            map.uiSettings.apply {
                                setAllGesturesEnabled(false)
                                isCompassEnabled = false
                                isLogoEnabled = false
                            }
                            map.setStyle(OPEN_FREE_MAP_STYLE_URL)
                            map.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(LatLng(resolvedLat, resolvedLng))
                                        .zoom(zoom)
                                        .build()
                                )
                            )
                            mapLibreRef = map
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            MapCenterPin(modifier = Modifier.align(Alignment.Center))

            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = onClick)
                )
            }
        }
    }
}

/**
 * Full-screen interactive MapLibre location picker.
 * Optionally centers on the device's GPS location on first open (falls back to Medan).
 */
@Composable
fun OutletLocationPickerDialog(
    initialLatitude: Double,
    initialLongitude: Double,
    useDeviceLocationOnStart: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val (startLat, startLng) = resolveOutletCoordinate(initialLatitude, initialLongitude)

    var selectedLat by remember(startLat) { mutableDoubleStateOf(startLat) }
    var selectedLng by remember(startLng) { mutableDoubleStateOf(startLng) }
    var isLocatingGps by remember { mutableStateOf(false) }
    var statusHint by remember { mutableStateOf("Geser atau ketuk peta untuk menentukan titik outlet.") }
    var mapLibreRef by remember { mutableStateOf<MapLibreMap?>(null) }
    val gpsCancellation = remember { CancellationSignal() }

    val latestMapRef by rememberUpdatedState(mapLibreRef)

    DisposableEffect(Unit) {
        onDispose { gpsCancellation.cancel() }
    }

    fun moveMapTo(lat: Double, lng: Double, zoom: Double = 16.0, animate: Boolean = true) {
        selectedLat = lat
        selectedLng = lng
        val update = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(lat, lng))
                .zoom(zoom)
                .build()
        )
        val map = latestMapRef ?: return
        if (animate) map.animateCamera(update, 450) else map.moveCamera(update)
    }

    fun requestCurrentGpsFix(userTriggered: Boolean) {
        isLocatingGps = true
        statusHint = "Mencari lokasi perangkat..."
        fetchCurrentDeviceLocation(context, gpsCancellation) { fix ->
            isLocatingGps = false
            if (fix != null) {
                moveMapTo(fix.first, fix.second, zoom = 16.0, animate = true)
                statusHint = "Peta dipusatkan ke lokasi Anda saat ini."
            } else {
                statusHint = if (userTriggered) {
                    "Lokasi GPS belum tersedia. Pastikan GPS aktif atau pilih titik manual."
                } else {
                    "Lokasi GPS belum tersedia, peta dimulai dari pusat Kota Medan."
                }
            }
        }
    }

    var pendingUserTriggeredGps by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.values.any { it }
        if (granted) {
            requestCurrentGpsFix(userTriggered = pendingUserTriggeredGps)
        } else {
            isLocatingGps = false
            statusHint = "Izin lokasi ditolak, peta dimulai dari pusat Kota Medan."
        }
    }

    fun triggerGpsLocation(userTriggered: Boolean) {
        pendingUserTriggeredGps = userTriggered
        if (hasLocationPermission(context)) {
            requestCurrentGpsFix(userTriggered = userTriggered)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(useDeviceLocationOnStart) {
        if (useDeviceLocationOnStart) {
            triggerGpsLocation(userTriggered = false)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val mapView = rememberLifecycleMapView()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            AndroidView(
                factory = {
                    mapView.apply {
                        getMapAsync { map ->
                            map.uiSettings.apply {
                                setAllGesturesEnabled(true)
                                isRotateGesturesEnabled = false
                                isTiltGesturesEnabled = false
                                isCompassEnabled = false
                                isLogoEnabled = false
                            }
                            map.setStyle(OPEN_FREE_MAP_STYLE_URL)
                            map.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(LatLng(selectedLat, selectedLng))
                                        .zoom(15.5)
                                        .build()
                                )
                            )
                            map.addOnCameraIdleListener {
                                val target = map.cameraPosition.target ?: return@addOnCameraIdleListener
                                selectedLat = target.latitude
                                selectedLng = target.longitude
                            }
                            map.addOnMapClickListener { point ->
                                selectedLat = point.latitude
                                selectedLng = point.longitude
                                map.animateCamera(CameraUpdateFactory.newLatLng(point), 300)
                                true
                            }
                            mapLibreRef = map
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            MapCenterPin(modifier = Modifier.align(Alignment.Center))

            // Top bar
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Tutup peta",
                            tint = SBColor.Grey_700,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pilih titik lokasi outlet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = statusHint,
                            fontSize = 11.sp,
                            color = SBColor.Grey_500,
                            maxLines = 1
                        )
                    }
                }
            }

            // Bottom action sheet card
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Koordinat terpilih",
                                fontSize = 12.sp,
                                color = SBColor.Grey_500
                            )
                            Text(
                                text = formatCoordinate(selectedLat, selectedLng),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { triggerGpsLocation(userTriggered = true) },
                                enabled = !isLocatingGps,
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, SBColor.Grey_200)
                            ) {
                                if (isLocatingGps) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = SBColor.Primary_550
                                    )
                                } else {
                                    Text(
                                        text = "Lokasi saya",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Grey_700
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    moveMapTo(DEFAULT_MEDAN_LAT, DEFAULT_MEDAN_LNG, zoom = 14.5)
                                    statusHint = "Peta dipusatkan ke Kota Medan."
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, SBColor.Grey_200)
                            ) {
                                Text(
                                    text = "Medan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onConfirm(selectedLat, selectedLng) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                    ) {
                        Text(
                            text = "Gunakan lokasi ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
