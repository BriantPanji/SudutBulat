package com.sudutcorp.sudutbulat.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState

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
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAddUser: () -> Unit = {},
    onNavigateToOutletList: () -> Unit = {},
    onNavigateToCatalog: () -> Unit = {}
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val roleLabel = if (isDevAccount) "Developer (${userRole.displayName})" else userRole.displayName
    val displayName = if (isDevAccount) {
        "Developer SDB"
    } else {
        when (userRole) {
            Role.DEV -> "Developer SDB"
            Role.OWNER -> ownerUiState.ownerName
            Role.WAREHOUSE -> "Budi Santoso"
            Role.OUTLET -> "Dimas Pratama"
        }
    }
    val initials = if (isDevAccount) {
        "DV"
    } else {
        when (userRole) {
            Role.DEV -> "DV"
            Role.OWNER -> ownerUiState.ownerInitials
            Role.WAREHOUSE -> "BS"
            Role.OUTLET -> "DP"
        }
    }
    val emailText = userEmail?.takeIf { it.isNotBlank() } ?: if (isDevAccount) {
        "sdb.dev@gmail.com"
    } else {
        when (userRole) {
            Role.DEV -> "sdb.dev@gmail.com"
            Role.OWNER -> ownerUiState.ownerEmail.ifBlank { "arif@sudutbulat.id" }
            Role.WAREHOUSE -> "sdb.gudang@gmail.com"
            Role.OUTLET -> "sdb.staff@gmail.com"
        }
    }

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
        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = roleLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_500
                )
                Text(
                    text = "Profil",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SBColor.Grey_700
                )
            }
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = SBColor.Success_500,
                        modifier = Modifier
                            .size(12.dp)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(enabled = isDevAccount, onClick = onSwitchRoleClick),
                    shape = CircleShape,
                    color = SBColor.Secondary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_500
                        )
                    }
                }
            }
        }

        // PROFILE CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = SBColor.Secondary_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Primary_500
                    )
                }
            }
            Text(
                text = displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = SBColor.Grey_100
            ) {
                Text(
                    text = roleLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_500,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Text(
                text = emailText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = SBColor.Grey_500
            )
        }

        // OWNER MANAGEMENT ACTIONS
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

        // SETTING BUTTONS
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
                iconRes = R.drawable.ic_bell,
                title = "Notifikasi",
                onClick = onNavigateToNotifications
            )

            ProfileMenuItem(
                iconRes = R.drawable.ic_lock,
                title = "Keamanan & kata sandi",
                onClick = { /* Navigasi keamanan */ }
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

    // Figma Frame 27:2257 — Owner — Keluar (Konfirmasi)
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirmLogout = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}

@Composable
private fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = SBColor.Primary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = "Keluar",
                            tint = SBColor.Primary_550,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = "Keluar dari akun?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Anda perlu masuk kembali untuk mengakses data operasional.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Grey_100,
                            contentColor = SBColor.Grey_700
                        )
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onConfirmLogout,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Primary_550,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Keluar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
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
                Text(
                    text = valueText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SBColor.Grey_500
                )
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
