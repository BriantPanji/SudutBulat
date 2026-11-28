package com.sudutcorp.sudutbulat.feature.user

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.navigation.popBackStackSafe
import com.sudutcorp.sudutbulat.core.components.SBAvatar
import com.sudutcorp.sudutbulat.core.components.SBConfirmDialog
import com.sudutcorp.sudutbulat.core.components.SBKeyValueRow
import com.sudutcorp.sudutbulat.core.components.SBSectionCard
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailStaffScreen(
    navController: NavHostController,
    userId: String,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    onOutletClick: (String) -> Unit = {}
) {
    val staff = uiState.staffItems.firstOrNull { it.id == userId }
        ?: uiState.staffItems.firstOrNull()

    var showOutletPickerSheet by rememberSaveable { mutableStateOf(false) }
    var showToggleActiveDialog by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SBSubHeader(
            eyebrow = staff?.role?.displayName ?: "Staf",
            title = staff?.name ?: "Detail staf",
            onBackClick = { navController.popBackStackSafe() }
        )

        if (staff != null) {
            // Identity Card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SBAvatar(
                            initials = staff.initials,
                            photoUrl = staff.photoUrl,
                            size = 52.dp,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(staff.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                            Text(staff.email, fontSize = 13.sp, color = SBColor.Grey_500)
                        }
                    }

                    SBStatusBadge(
                        text = if (staff.isActive) "Aktif" else "Nonaktif",
                        containerColor = if (staff.isActive) SBColor.Success_100 else SBColor.Grey_100,
                        contentColor = if (staff.isActive) SBColor.Success_600 else SBColor.Grey_600,
                        onClick = { showToggleActiveDialog = true }
                    )
                }
            }

            // Penugasan Outlet (khusus Staf Outlet)
            if (staff.role == Role.OUTLET) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Penugasan outlet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        TextButton(
                            onClick = { showOutletPickerSheet = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Ubah outlet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                        }
                    }

                    OutlinedCard(
                        onClick = {
                            val assignedId = staff.outletId
                            if (!assignedId.isNullOrBlank()) onOutletClick(assignedId) else showOutletPickerSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SBColor.Grey_100)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = SBColor.Primary_100
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_store),
                                        contentDescription = null,
                                        tint = SBColor.Primary_550,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = staff.outletName ?: "Belum ditugaskan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Grey_700
                                )
                                Text(
                                    text = if (staff.outletName != null) {
                                        "Ketuk untuk melihat detail outlet"
                                    } else {
                                        "Ketuk untuk menugaskan ke outlet"
                                    },
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
            }

            // Informasi Akun
            SBSectionCard(title = "Informasi akun") {
                SBKeyValueRow("Kode login", staff.id, labelColor = SBColor.Grey_500)
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                SBKeyValueRow("Username", staff.username, labelColor = SBColor.Grey_500)
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                SBKeyValueRow("Email", staff.email, labelColor = SBColor.Grey_500)
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                SBKeyValueRow("Peran", staff.role.displayName, labelColor = SBColor.Grey_500)
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                SBKeyValueRow("Status akun", if (staff.isActive) "Aktif" else "Nonaktif", labelColor = SBColor.Grey_500)
            }
        }
    }

    if (showOutletPickerSheet && staff != null) {
        ModalBottomSheet(
            onDismissRequest = { showOutletPickerSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pilih outlet penugasan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = uiState.outletItems,
                        key = { it.id },
                        contentType = { "outlet_option" }
                    ) { outlet ->
                        val isSelected = staff.outletId == outlet.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(OwnerUiEvent.ReassignSingleStaffOutlet(staff.id, outlet.id))
                                    showOutletPickerSheet = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(outlet.displayName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                Text(outlet.address, fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onEvent(OwnerUiEvent.ReassignSingleStaffOutlet(staff.id, outlet.id))
                                    showOutletPickerSheet = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = SBColor.Primary_550)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showToggleActiveDialog && staff != null) {
        val nextAction = if (staff.isActive) "nonaktifkan" else "aktifkan"
        SBConfirmDialog(
            title = if (staff.isActive) "Nonaktifkan akun?" else "Aktifkan akun?",
            description = "Apakah Anda yakin ingin men-$nextAction akun ${staff.name}?",
            confirmText = "Konfirmasi",
            onDismiss = { showToggleActiveDialog = false },
            onConfirm = {
                showToggleActiveDialog = false
                onEvent(OwnerUiEvent.ToggleUserActive(staff.id))
            }
        )
    }
}
