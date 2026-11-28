package com.sudutcorp.sudutbulat.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBAvatar
import com.sudutcorp.sudutbulat.core.components.SBConfirmDialog
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    userRole: Role = Role.OWNER,
    userEmail: String? = null,
    isDevAccount: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    ownerUiState: OwnerUiState = OwnerUiState(),
    profileUiState: ProfileUiState = ProfileUiState(),
    onProfileEvent: (ProfileUiEvent) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAddUser: () -> Unit = {},
    onNavigateToOutletList: () -> Unit = {},
    onNavigateToCatalog: () -> Unit = {},
    onUpdatePassword: suspend (currentPassword: String, newPassword: String) -> Result<Unit> = { _, _ ->
        Result.failure(IllegalStateException("Layanan tidak tersedia."))
    },
    onPasswordUpdated: (String) -> Unit = {}
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val identity = remember(
        profileUiState.users,
        profileUiState.localPhotoOverrides,
        userRole,
        userEmail,
        isDevAccount,
        ownerUiState.ownerName,
        ownerUiState.ownerEmail
    ) {
        resolveProfileIdentity(
            state = profileUiState,
            userRole = userRole,
            userEmail = userEmail,
            isDevAccount = isDevAccount,
            ownerFallbackName = ownerUiState.ownerName,
            ownerFallbackEmail = ownerUiState.ownerEmail
        )
    }

    val hasProfilePhoto = !identity.photoUrl.isNullOrBlank()
    val photoLauncher = rememberProfilePhotoLauncher(
        onPhotoReady = { jpegBytes ->
            onProfileEvent(
                ProfileUiEvent.UploadProfilePhoto(
                    userId = identity.userId,
                    jpegBytes = jpegBytes
                )
            )
        },
        onError = { errorMsg ->
            onProfileEvent(ProfileUiEvent.PhotoProcessFailed(errorMsg))
        }
    )

    val isOwnerView = userRole == Role.OWNER || userRole == Role.DEV

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SBPageHeader(
            eyebrow = identity.roleLabel,
            title = "Profil",
            initials = identity.initials,
            photoUrl = identity.photoUrl,
            isAvatarClickable = isDevAccount,
            onAvatarClick = onSwitchRoleClick
        )

        // Profile Identity & Modular Avatar Picker Trigger
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                SBAvatar(
                    initials = identity.initials,
                    photoUrl = identity.photoUrl,
                    size = 88.dp,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    isLoading = profileUiState.isPhotoUpdating,
                    contentDescription = "Foto profil ${identity.displayName}",
                    onClick = {
                        if (!profileUiState.isPhotoUpdating) {
                            onProfileEvent(ProfileUiEvent.OpenPhotoOptionsSheet)
                        }
                    }
                )

                Surface(
                    onClick = {
                        if (!profileUiState.isPhotoUpdating) {
                            onProfileEvent(ProfileUiEvent.OpenPhotoOptionsSheet)
                        }
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .border(2.dp, Color.White, CircleShape),
                    shape = CircleShape,
                    color = SBColor.Primary_550
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = if (hasProfilePhoto) "Ubah foto profil" else "Buat foto profil",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Surface(
                onClick = {
                    if (!profileUiState.isPhotoUpdating) {
                        onProfileEvent(ProfileUiEvent.OpenPhotoOptionsSheet)
                    }
                },
                shape = RoundedCornerShape(99.dp),
                color = SBColor.Primary_100,
                border = BorderStroke(1.dp, SBColor.Primary_200)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null,
                        tint = SBColor.Primary_550,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (hasProfilePhoto) "Ubah foto profil" else "Buat foto profil",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550
                    )
                }
            }

            Text(
                text = identity.displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
            Surface(shape = RoundedCornerShape(99.dp), color = SBColor.Grey_100) {
                Text(
                    text = identity.roleLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_500,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Text(text = identity.email, fontSize = 14.sp, color = SBColor.Grey_500)
        }

        if (isOwnerView) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Manajemen bisnis",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_500,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ProfileMenuItem(
                    iconRes = R.drawable.ic_user,
                    title = "Tambah pengguna",
                    valueText = "${ownerUiState.users.size} akun",
                    iconBgColor = SBColor.Primary_100,
                    iconTint = SBColor.Primary_550,
                    onClick = onNavigateToAddUser
                )
                ProfileMenuItem(
                    iconRes = R.drawable.ic_store,
                    title = "Daftar outlet",
                    valueText = "${ownerUiState.activeOutletsCount} outlet",
                    iconBgColor = SBColor.Primary_100,
                    iconTint = SBColor.Primary_550,
                    onClick = onNavigateToOutletList
                )
                ProfileMenuItem(
                    iconRes = R.drawable.ic_box,
                    title = "Daftar barang",
                    valueText = "${ownerUiState.catalogItems.size} item",
                    iconBgColor = SBColor.Primary_100,
                    iconTint = SBColor.Primary_550,
                    onClick = onNavigateToCatalog
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            if (isDevAccount) {
                ProfileMenuItem(
                    iconRes = R.drawable.ic_user,
                    title = "Lihat sebagai (${userRole.displayName})",
                    iconBgColor = SBColor.Primary_100,
                    iconTint = SBColor.Primary_550,
                    textColor = SBColor.Primary_550,
                    chevronTint = SBColor.Primary_550,
                    fontWeight = FontWeight.Bold,
                    onClick = onSwitchRoleClick
                )
            }

            ProfileMenuItem(
                iconRes = R.drawable.ic_camera,
                title = if (hasProfilePhoto) "Ubah foto profil" else "Buat foto profil",
                valueText = if (hasProfilePhoto) "Foto aktif" else "Inisial (${identity.initials})",
                onClick = { onProfileEvent(ProfileUiEvent.OpenPhotoOptionsSheet) }
            )
            ProfileMenuItem(
                iconRes = R.drawable.ic_bell,
                title = "Notifikasi",
                onClick = onNavigateToNotifications
            )
            ProfileMenuItem(
                iconRes = R.drawable.ic_lock,
                title = "Keamanan & kata sandi",
                onClick = { showPasswordDialog = true }
            )
            ProfileMenuItem(
                iconRes = R.drawable.ic_logout,
                title = "Keluar",
                iconBgColor = SBColor.Primary_100,
                iconTint = SBColor.Primary_500,
                textColor = SBColor.Primary_500,
                chevronTint = SBColor.Primary_500,
                onClick = { showLogoutDialog = true }
            )
        }
    }

    if (profileUiState.showPhotoOptionsSheet) {
        ProfilePhotoBottomSheet(
            hasExistingPhoto = hasProfilePhoto,
            onDismiss = { onProfileEvent(ProfileUiEvent.DismissPhotoOptionsSheet) },
            onPickFromGalleryClick = {
                onProfileEvent(ProfileUiEvent.DismissPhotoOptionsSheet)
                photoLauncher.launchGalleryPicker()
            },
            onTakePhotoClick = {
                onProfileEvent(ProfileUiEvent.DismissPhotoOptionsSheet)
                photoLauncher.launchCameraCapture()
            },
            onRemovePhotoClick = {
                onProfileEvent(ProfileUiEvent.RequestDeletePhoto)
            }
        )
    }

    if (profileUiState.showDeletePhotoConfirmDialog) {
        SBConfirmDialog(
            title = "Hapus foto profil?",
            description = "Foto profil Anda akan dihapus dan tampilan avatar akan kembali menggunakan inisial (${identity.initials}).",
            confirmText = "Hapus",
            iconRes = R.drawable.ic_trash,
            iconBgColor = SBColor.Error_100,
            iconTint = SBColor.Primary_550,
            onDismiss = { onProfileEvent(ProfileUiEvent.DismissDeletePhotoConfirm) },
            onConfirm = {
                onProfileEvent(ProfileUiEvent.ConfirmDeleteProfilePhoto(identity.userId))
            }
        )
    }

    if (showLogoutDialog) {
        SBConfirmDialog(
            title = "Keluar dari akun?",
            description = "Anda perlu masuk kembali untuk mengakses data operasional.",
            confirmText = "Keluar",
            iconRes = R.drawable.ic_logout,
            iconBgColor = SBColor.Primary_100,
            iconTint = SBColor.Primary_550,
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }

    if (showPasswordDialog) {
        var currentPw by remember { mutableStateOf("") }
        var newPw by remember { mutableStateOf("") }
        var isUpdating by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isUpdating) showPasswordDialog = false },
            containerColor = Color.White,
            title = { Text("Ubah kata sandi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentPw,
                        onValueChange = { currentPw = it; errorMsg = null },
                        label = { Text("Kata sandi saat ini") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPw,
                        onValueChange = { newPw = it; errorMsg = null },
                        label = { Text("Kata sandi baru (min. 6 karakter)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    errorMsg?.let { err ->
                        Text(err, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isUpdating,
                    onClick = {
                        coroutineScope.launch {
                            isUpdating = true
                            errorMsg = null
                            onUpdatePassword(currentPw, newPw)
                                .onSuccess {
                                    isUpdating = false
                                    showPasswordDialog = false
                                    onPasswordUpdated("Kata sandi berhasil diperbarui.")
                                }
                                .onFailure { e ->
                                    isUpdating = false
                                    errorMsg = e.localizedMessage ?: "Gagal memperbarui kata sandi."
                                }
                        }
                    }
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Simpan", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !isUpdating, onClick = { showPasswordDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    valueText: String? = null,
    iconBgColor: Color = SBColor.Secondary_100,
    iconTint: Color = SBColor.Secondary_500,
    textColor: Color = SBColor.Grey_700,
    chevronTint: Color = SBColor.Secondary_500,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
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
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                fontSize = 14.sp,
                fontWeight = fontWeight,
                color = textColor
            )
            if (!valueText.isNullOrBlank()) {
                Text(text = valueText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SBColor.Grey_500)
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = chevronTint
            )
        }
        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
    }
}
