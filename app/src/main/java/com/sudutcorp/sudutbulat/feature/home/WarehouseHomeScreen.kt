package com.sudutcorp.sudutbulat.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor

@Composable
fun WarehouseHomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var showRoleBottomSheet by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Owner") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            WarehouseHeaderMain(
                onProfileClick = { showRoleBottomSheet = true }
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD34317))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Total stok gudang", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("12.840", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("pcs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_wallet),
                                contentDescription = "",
                                tint = Color(0xFFD34317),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(painterResource(R.drawable.arrow_up), "", tint = Color.White, modifier = Modifier.size(10.dp))
                    Text("Diperbarui 09.38", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.arrow_up), contentDescription = "", modifier = Modifier.size(20.dp), tint = Color(0xFFD34317))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Stok masuk", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SBColor.Grey_700)
                        Text("Catat barang tiba", fontSize = 12.sp, color = SBColor.Grey_400)
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_wallet), contentDescription = "", modifier = Modifier.size(20.dp), tint = Color(0xFFD34317))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Distribusi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SBColor.Grey_700)
                        Text("Kirim ke outlet", fontSize = 12.sp, color = SBColor.Grey_400)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Perlu perhatian", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    Text("Kelola stok", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD34317))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
                ) {
                    WarehousePerhatianItem("Tahu Original", "Stok tersisa 620 pcs", "Menipis")
                    HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    WarehousePerhatianItem("Bumbu Pedas", "Stok tersisa 18 pak", "Menipis")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Distribusi hari ini", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    Text("Lihat semua", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD34317))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
                ) {
                    DistribusiItem(isFinished = true, name = "Outlet Sudirman", info = "1.200 pcs • 08.15", statusText = "Terkirim", statusBg = Color(0xFFD1FAE5), statusTextColor = Color(0xFF065F46))
                    HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    DistribusiItem(isFinished = false, name = "Outlet Cikini", info = "900 pcs • 10.30", statusText = "Di perjalanan", statusBg = Color(0xFFF1F5F9), statusTextColor = Color(0xFF475569))
                    HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    DistribusiItem(isFinished = false, name = "Outlet Tebet", info = "1.050 pcs • 13.00", statusText = "Dijadwalkan", statusBg = Color(0xFFF1F5F9), statusTextColor = Color(0xFF475569))
                }
            }
        }

        if (showRoleBottomSheet) {
            WarehouseRoleBottomSheet(
                selectedRole = selectedRole,
                onDismiss = { showRoleBottomSheet = false },
                onRoleSelected = { selectedRole = it }
            )
        }
    }
}

@Composable
fun WarehouseHeaderMain(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Gudang Pusat • Jakarta", fontSize = 13.sp, color = SBColor.Grey_500)
            Text("Dashboard Gudang", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
        Box(
            modifier = Modifier.clickable { onProfileClick() }
        ) {
            Row(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SBColor.Secondary_100),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_500)
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun WarehousePerhatianItem(title: String, subtitle: String, badgeText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_wallet), contentDescription = "", tint = Color(0xFFD34317), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
            Text(subtitle, fontSize = 12.sp, color = SBColor.Grey_400)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
            }
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "", modifier = Modifier.size(16.dp), tint = SBColor.Grey_300)
        }
    }
}

@Composable
fun DistribusiItem(isFinished: Boolean, name: String, info: String, statusText: String, statusBg: Color, statusTextColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isFinished) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(if (isFinished) R.drawable.arrow_up else R.drawable.ic_store),
                contentDescription = "",
                tint = if (isFinished) Color(0xFF059669) else Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
            Text(info, fontSize = 12.sp, color = SBColor.Grey_400)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusBg)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusTextColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseRoleBottomSheet(
    selectedRole: String,
    onDismiss: () -> Unit,
    onRoleSelected: (String) -> Unit
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
                Text("Lihat sebagai", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Berpindah peran untuk meninjau sistem", fontSize = 13.sp, color = SBColor.Grey_500)
            }

            RoleOptionItem(
                title = "Owner",
                subtitle = "Pantau bisnis & laporan",
                isSelected = selectedRole == "Owner",
                iconRes = R.drawable.ic_wallet,
                onClick = { onRoleSelected("Owner") }
            )

            RoleOptionItem(
                title = "Admin Gudang",
                subtitle = "Kelola stok & distribusi",
                isSelected = selectedRole == "Admin Gudang",
                iconRes = R.drawable.ic_store,
                onClick = { onRoleSelected("Admin Gudang") }
            )

            RoleOptionItem(
                title = "Staf Outlet",
                subtitle = "Operasional outlet harian",
                isSelected = selectedRole == "Staf Outlet",
                iconRes = R.drawable.ic_store,
                onClick = { onRoleSelected("Staf Outlet") }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFFEE2E2) else SBColor.Grey_100,
                shape = RoundedCornerShape(16.dp)
            )
            .background(if (isSelected) Color(0xFFFFF5F5) else Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(iconRes),
                contentDescription = "",
                tint = Color(0xFFB45309),
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SBColor.Grey_700)
            Text(subtitle, fontSize = 12.sp, color = SBColor.Grey_500)
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}