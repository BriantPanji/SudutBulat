package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import kotlinx.coroutines.delay

const val ARRIVED_REMINDER_INTERVAL_MS = 60_000L

/**
 * Global reminder host mounted at the MainScreen level when active workspace is [com.sudutcorp.sudutbulat.data.model.Role.OUTLET].
 * Displays a confirmation popup on ANY screen whenever >= 1 distribution has status ARRIVED,
 * and re-displays every 1 minute ([ARRIVED_REMINDER_INTERVAL_MS]) if snoozed until confirmed.
 */
@Composable
fun OutletArrivedReminderHost(
    isEnabled: Boolean,
    arrivedDistributions: List<OutletDistributionUi>,
    onConfirmNow: (distributionId: String) -> Unit,
    reminderIntervalMs: Long = ARRIVED_REMINDER_INTERVAL_MS
) {
    val arrivedKey = remember(arrivedDistributions) {
        arrivedDistributions.joinToString("|") { it.id }
    }
    var isPopupVisible by rememberSaveable { mutableStateOf(false) }
    var snoozeTrigger by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(isEnabled, arrivedKey, snoozeTrigger) {
        if (!isEnabled || arrivedDistributions.isEmpty()) {
            isPopupVisible = false
            return@LaunchedEffect
        }
        if (snoozeTrigger == 0) {
            isPopupVisible = true
        } else {
            isPopupVisible = false
            delay(reminderIntervalMs)
            isPopupVisible = true
        }
    }

    val primaryDist = arrivedDistributions.firstOrNull()
    if (isEnabled && isPopupVisible && primaryDist != null) {
        OutletArrivedReminderDialog(
            primary = primaryDist,
            totalArrivedCount = arrivedDistributions.size,
            onSnooze = {
                isPopupVisible = false
                snoozeTrigger += 1
            },
            onConfirmNow = {
                isPopupVisible = false
                snoozeTrigger += 1
                onConfirmNow(primaryDist.id)
            }
        )
    }
}

@Composable
private fun OutletArrivedReminderDialog(
    primary: OutletDistributionUi,
    totalArrivedCount: Int,
    onSnooze: () -> Unit,
    onConfirmNow: () -> Unit
) {
    Dialog(
        onDismissRequest = onSnooze,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            border = BorderStroke(1.dp, SBColor.Primary_200)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
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
                            painter = painterResource(id = R.drawable.ic_truck),
                            contentDescription = "Pengiriman tiba di outlet",
                            tint = SBColor.Primary_550,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (totalArrivedCount == 1) {
                            "Pengiriman sudah tiba!"
                        } else {
                            "$totalArrivedCount pengiriman sudah tiba!"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Gudang menandai barang sudah sampai di lokasi outlet. Segera cek fisik barang dan konfirmasi penerimaan.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SBColor.Grey_100.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Distribusi ${primary.type.label}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700
                            )
                            Text(
                                text = primary.deliveryMethod.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Primary_550
                            )
                        }
                        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_200)
                        Text(
                            text = "${primary.items.size} jenis bahan • Total ${primary.totalSent} unit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Grey_600
                        )
                        primary.arrivedTimeLabel?.let { arrivedTime ->
                            Text(
                                text = "Ditandai tiba: $arrivedTime",
                                fontSize = 11.sp,
                                color = SBColor.Grey_500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirmNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Primary_550,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Konfirmasi sekarang",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Grey_100,
                            contentColor = SBColor.Grey_700
                        )
                    ) {
                        Text(
                            text = "Nanti (ingatkan 1 menit lagi)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
