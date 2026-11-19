package com.sudutcorp.sudutbulat.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.distribution.CreateDistributionBottomSheet
import com.sudutcorp.sudutbulat.feature.distribution.DistributionCardRow
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiEffect
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiEvent
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun WarehouseHomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: WarehouseUiState = WarehouseUiState(),
    effects: Flow<WarehouseUiEffect> = emptyFlow(),
    onEvent: (WarehouseUiEvent) -> Unit = {},
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onOpenCatalogTab: () -> Unit = {},
    onOpenDistributionTab: () -> Unit = {}
) {
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is WarehouseUiEffect.DistributionSaved) {
                showCreateSheet = false
            }
        }
    }

    val totalActiveOutlets = uiState.activeOutlets.size
    val completedToday = uiState.outletsCompletedMorningCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WarehouseHeaderMain(
            isDevMode = isDevMode,
            onProfileClick = { if (isDevMode) onSwitchRoleClick() }
        )

        // Progres Distribusi Pagi Hari Ini (sesuai PRD §3.2)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SBColor.Primary
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Distribusi pagi hari ini", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text(
                            text = "$completedToday / $totalActiveOutlets",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (uiState.outletsPendingMorning.isEmpty()) {
                                "Semua outlet aktif sudah dikirim"
                            } else {
                                "${uiState.outletsPendingMorning.size} outlet belum dikirim hari ini"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = SBColor.Primary_100
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painterResource(R.drawable.ic_truck),
                                    contentDescription = null,
                                    tint = SBColor.Primary_550,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "${uiState.pendingRequests.size} permintaan tambahan stok menunggu persetujuan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // Quick Actions (sesuai PRD: Katalog Bahan & Buat Distribusi)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedCard(
                onClick = {
                    onEvent(WarehouseUiEvent.InitCreateForm(null))
                    showCreateSheet = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = SBColor.Primary_100) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_truck), contentDescription = null, modifier = Modifier.size(20.dp), tint = SBColor.Primary_550)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Kirim distribusi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SBColor.Grey_700)
                        Text("Kirim stok ke outlet", fontSize = 12.sp, color = SBColor.Grey_500)
                    }
                }
            }

            OutlinedCard(
                onClick = onOpenCatalogTab,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = SBColor.Secondary_100) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_box), contentDescription = null, modifier = Modifier.size(20.dp), tint = SBColor.Secondary_500)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Katalog bahan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SBColor.Grey_700)
                        Text("${uiState.activeMaterials.size} material aktif", fontSize = 12.sp, color = SBColor.Grey_500)
                    }
                }
            }
        }

        // Outlet Belum Menerima Distribusi Pagi Hari Ini
        if (uiState.outletsPendingMorning.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Belum dikirim hari ini", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    TextButton(
                        onClick = onOpenDistributionTab,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Lihat semua", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Column {
                        uiState.outletsPendingMorning.forEachIndexed { idx, outlet ->
                            WarehousePerhatianItem(
                                title = "Outlet ${outlet.name}",
                                subtitle = outlet.address,
                                badgeText = "Kirim",
                                onClick = {
                                    onEvent(WarehouseUiEvent.InitCreateForm(outlet.id))
                                    showCreateSheet = true
                                }
                            )
                            if (idx < uiState.outletsPendingMorning.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                            }
                        }
                    }
                }
            }
        }

        // Request Tambahan Stok dari Outlet (Approve / Reject)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Permintaan tambahan stok", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                TextButton(
                    onClick = onOpenDistributionTab,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Semua distribusi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                }
            }

            if (uiState.pendingRequests.isEmpty()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada permintaan tambahan stok yang menunggu.", fontSize = 13.sp, color = SBColor.Grey_500)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.pendingRequests.forEach { req ->
                        DistributionCardRow(
                            card = req,
                            isActionLoading = uiState.isActionLoading,
                            onApprove = { onEvent(WarehouseUiEvent.ApproveRequest(req.id)) },
                            onReject = { onEvent(WarehouseUiEvent.RejectRequest(req.id, "Ditolak oleh Admin Gudang")) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateDistributionBottomSheet(
            uiState = uiState,
            onEvent = onEvent,
            onDismiss = { if (!uiState.createForm.isSaving) showCreateSheet = false }
        )
    }
}

@Composable
fun WarehouseHeaderMain(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDevMode: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isDevMode) "Gudang Pusat • Mode DEV" else "Gudang Pusat • Jakarta",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SBColor.Grey_500
            )
            Text(
                text = "Dashboard Gudang",
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
                    .clickable(enabled = isDevMode, onClick = onProfileClick),
                shape = CircleShape,
                color = SBColor.Secondary_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isDevMode) "DV" else "BS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_500
                    )
                }
            }
        }
    }
}

@Composable
fun WarehousePerhatianItem(
    title: String,
    subtitle: String,
    badgeText: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = SBColor.Primary_100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(R.drawable.ic_store),
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
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
            Text(subtitle, fontSize = 12.sp, color = SBColor.Grey_500)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SBColor.Warning_100
            ) {
                Text(
                    text = badgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Warning_600,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Icon(
                painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SBColor.Grey_400
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseRoleBottomSheet(
    selectedRole: Role,
    onDismiss: () -> Unit,
    onRoleSelected: (Role) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Lihat sebagai", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text("Berpindah peran untuk meninjau sistem (Mode DEV)", fontSize = 13.sp, color = SBColor.Grey_500)
            }

            RoleOptionItem(
                title = "Owner",
                subtitle = "Pantau bisnis & laporan",
                isSelected = selectedRole == Role.OWNER,
                iconRes = R.drawable.ic_wallet,
                onClick = { onRoleSelected(Role.OWNER) }
            )

            RoleOptionItem(
                title = "Admin Gudang",
                subtitle = "Kelola stok & distribusi",
                isSelected = selectedRole == Role.WAREHOUSE,
                iconRes = R.drawable.ic_truck,
                onClick = { onRoleSelected(Role.WAREHOUSE) }
            )

            RoleOptionItem(
                title = "Staf Outlet",
                subtitle = "Operasional outlet harian",
                isSelected = selectedRole == Role.OUTLET,
                iconRes = R.drawable.ic_store,
                onClick = { onRoleSelected(Role.OUTLET) }
            )
        }
    }
}

@Composable
fun RoleOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    iconRes: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "RoleOptionScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) SBColor.Primary_400 else SBColor.Grey_100,
        label = "RoleOptionBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) SBColor.Primary_100.copy(alpha = 0.5f) else Color.White,
        label = "RoleOptionBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) SBColor.Primary_100 else SBColor.Secondary_100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(iconRes),
                    contentDescription = null,
                    tint = if (isSelected) SBColor.Primary_550 else SBColor.Secondary_500,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SBColor.Grey_700)
            Text(subtitle, fontSize = 12.sp, color = SBColor.Grey_500)
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}
