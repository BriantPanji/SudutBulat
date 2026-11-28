package com.sudutcorp.sudutbulat.feature.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.ProfileImageProcessor
import kotlinx.coroutines.launch

@Stable
class ProfilePhotoLauncherController(
    private val onPickFromGallery: () -> Unit,
    private val onTakePhotoFromCamera: () -> Unit
) {
    fun launchGalleryPicker() = onPickFromGallery()
    fun launchCameraCapture() = onTakePhotoFromCamera()
}

/**
 * Modular Compose hook that coordinates:
 * 1. Android Photo Picker ([ActivityResultContracts.PickVisualMedia]) for gallery selection.
 * 2. Runtime CAMERA permission ([ActivityResultContracts.RequestPermission]) + full-resolution
 *    camera capture ([ActivityResultContracts.TakePicture]) via [androidx.core.content.FileProvider].
 * 3. Off-main-thread EXIF rotation, 1:1 center-crop, and JPEG compression via [ProfileImageProcessor].
 */
@Composable
fun rememberProfilePhotoLauncher(
    onPhotoReady: (jpegBytes: ByteArray) -> Unit,
    onError: (String) -> Unit,
    targetSizePx: Int = ProfileImageProcessor.TARGET_AVATAR_PX
): ProfilePhotoLauncherController {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnPhotoReady by rememberUpdatedState(onPhotoReady)
    val currentOnError by rememberUpdatedState(onError)

    var pendingCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }

    val handleCapturedOrPickedUri: (Uri) -> Unit = remember(context, coroutineScope, targetSizePx) {
        { uri ->
            coroutineScope.launch {
                ProfileImageProcessor.processUriToJpegBytes(context, uri, targetSizePx)
                    .onSuccess { jpegBytes ->
                        currentOnPhotoReady(jpegBytes)
                    }
                    .onFailure { err ->
                        val msg = err.localizedMessage?.takeIf { it.isNotBlank() }
                            ?: "Gagal memproses foto."
                        currentOnError(msg)
                    }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            handleCapturedOrPickedUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSuccess: Boolean ->
        val rawUriString = pendingCameraUriString
        pendingCameraUriString = null
        if (isSuccess && !rawUriString.isNullOrBlank()) {
            handleCapturedOrPickedUri(Uri.parse(rawUriString))
        }
    }

    val startCameraCapture: () -> Unit = remember(context, cameraLauncher) {
        {
            runCatching {
                val uri = ProfileImageProcessor.createTempCameraUri(context)
                pendingCameraUriString = uri.toString()
                cameraLauncher.launch(uri)
            }.onFailure { err ->
                pendingCameraUriString = null
                currentOnError("Gagal membuka kamera: ${err.localizedMessage ?: "Perangkat tidak mendukung kamera."}")
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCameraCapture()
        } else {
            currentOnError("Izin kamera diperlukan untuk mengambil foto profil.")
        }
    }

    return remember(context, galleryLauncher, cameraPermissionLauncher, startCameraCapture) {
        ProfilePhotoLauncherController(
            onPickFromGallery = {
                runCatching {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }.onFailure { err ->
                    currentOnError("Gagal membuka galeri: ${err.localizedMessage ?: "Tidak tersedia."}")
                }
            },
            onTakePhotoFromCamera = {
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasCameraPermission) {
                    startCameraCapture()
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoBottomSheet(
    hasExistingPhoto: Boolean,
    onDismiss: () -> Unit,
    onPickFromGalleryClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (hasExistingPhoto) "Ubah foto profil" else "Buat foto profil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = "Pilih sumber foto profil yang akan ditampilkan pada akun Anda.",
                    fontSize = 13.sp,
                    color = SBColor.Grey_500
                )
            }

            ProfilePhotoOptionCard(
                iconRes = R.drawable.ic_gallery,
                title = "Pilih dari galeri",
                subtitle = "Gunakan foto yang tersimpan di perangkat Anda",
                iconBgColor = SBColor.Secondary_100,
                iconTint = SBColor.Secondary_500,
                titleColor = SBColor.Grey_700,
                onClick = onPickFromGalleryClick
            )

            ProfilePhotoOptionCard(
                iconRes = R.drawable.ic_camera,
                title = "Ambil foto",
                subtitle = "Buka kamera dan tangkap foto profil baru",
                iconBgColor = SBColor.Primary_100,
                iconTint = SBColor.Primary_550,
                titleColor = SBColor.Grey_700,
                onClick = onTakePhotoClick
            )

            if (hasExistingPhoto) {
                ProfilePhotoOptionCard(
                    iconRes = R.drawable.ic_trash,
                    title = "Hapus foto profil",
                    subtitle = "Kembali menggunakan inisial nama pada avatar",
                    iconBgColor = SBColor.Error_100,
                    iconTint = SBColor.Primary_550,
                    titleColor = SBColor.Primary_550,
                    borderColor = SBColor.Primary_200,
                    onClick = onRemovePhotoClick
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoOptionCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    iconBgColor: Color,
    iconTint: Color,
    titleColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = SBColor.Grey_100
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SBColor.Grey_500
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = SBColor.Grey_400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
