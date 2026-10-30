package com.sudutcorp.sudutbulat.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R

@Composable
fun OwnerNotificationScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    OwnerNotificationContent(
        modifier = modifier,
        onBackClick = { navController.popBackStack() },
        onMarkAllAsReadClick = { /* Aksi tandai dibaca */ }
    )
}

@Composable
fun OwnerNotificationContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMarkAllAsReadClick: () -> Unit = {}
) {
    // Definisi Warna
    val primaryColor = Color(0xFF231F1F)
    val secondaryColor = Color(0xFF443D3D)
    val accentRed = Color(0xFFCD3009)
    val textMuted = Color(0xFF685E5D)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(color = Color(0xFFF1F0F0), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Kembali",
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Owner",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryColor
                )
                Text(
                    text = "Notifikasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }

        // Section: Perlu ditinjau
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perlu ditinjau",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                TextButton(onClick = onMarkAllAsReadClick) {
                    Text(
                        text = "Tandai dibaca",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentRed
                    )
                }
            }

            // Card 1: Stok rusak — Outlet Cikini
            ReviewNotificationCard(
                title = "Stok rusak — Outlet Cikini",
                description = "12 pcs Tahu Pedas dilaporkan rusak (kemasan sobek).",
                time = "5 menit lalu",
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                textMuted = textMuted,
                accentRed = accentRed
            )

            // Card 2: Pengeluaran tidak biasa
            ReviewNotificationCard(
                title = "Pengeluaran tidak biasa",
                description = "Outlet Gajah Mada mencatat Rp420.000 untuk kategori Lainnya.",
                time = "32 menit lalu",
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                textMuted = textMuted,
                accentRed = accentRed
            )
        }

        // Section: Sebelumnya
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Sebelumnya",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Card 3: Laporan harian diterima
            PreviousNotificationCard(
                title = "Laporan harian diterima",
                description = "Outlet Sudirman mengirim laporan tutup kasir kemarin.",
                time = "Kemarin, 21.40",
                secondaryColor = secondaryColor,
                textMuted = textMuted
            )

            // Card 4: Stok Tahu Original menipis
            PreviousNotificationCard(
                title = "Stok Tahu Original menipis",
                description = "Sisa 620 pcs di gudang pusat.",
                time = "Kemarin, 15.10",
                secondaryColor = secondaryColor,
                textMuted = textMuted
            )
        }
    }
}

@Composable
private fun ReviewNotificationCard(
    title: String,
    description: String,
    time: String,
    primaryColor: Color,
    secondaryColor: Color,
    textMuted: Color,
    accentRed: Color
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xFFFFEDEC)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color.Transparent)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_store),
                    contentDescription = null,
                    tint = accentRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    // Indikator Belum Dibaca (Titik Merah)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color = accentRed, shape = CircleShape)
                    )
                }

                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryColor
                )

                Text(
                    text = time,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PreviousNotificationCard(
    title: String,
    description: String,
    time: String,
    secondaryColor: Color,
    textMuted: Color
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEFEFEF))
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFF8EBD8),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_store),
                    contentDescription = null,
                    tint = Color(0xFF645434),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryColor
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryColor
                )

                Text(
                    text = time,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerNotificationScreenPreview() {
    OwnerNotificationContent()
}