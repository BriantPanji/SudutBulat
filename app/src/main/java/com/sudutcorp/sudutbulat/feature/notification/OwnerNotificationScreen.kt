package com.sudutcorp.sudutbulat.feature.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.feature.owner.OwnerNotificationUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState

@Composable
fun OwnerNotificationScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    onNotificationClick: (OwnerNotificationUi) -> Unit = {}
) {
    OwnerNotificationContent(
        modifier = modifier,
        notifications = uiState.notifications,
        onBackClick = { navController.popBackStackSafe() },
        onMarkAllAsReadClick = { onEvent(OwnerUiEvent.MarkAllNotificationsRead) },
        onNotificationClick = onNotificationClick
    )
}

@Composable
fun OwnerNotificationContent(
    modifier: Modifier = Modifier,
    notifications: List<OwnerNotificationUi> = OwnerUiState().notifications,
    onBackClick: () -> Unit = {},
    onMarkAllAsReadClick: () -> Unit = {},
    onNotificationClick: (OwnerNotificationUi) -> Unit = {}
) {
    val reviewItems = notifications.filter { it.isNeedsReview }
    val previousItems = notifications.filterNot { it.isNeedsReview }

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
            eyebrow = "Owner",
            title = "Notifikasi",
            onBackClick = onBackClick
        )

        if (reviewItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Perlu ditinjau", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    TextButton(
                        onClick = onMarkAllAsReadClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Tandai dibaca", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }

                reviewItems.forEach { item ->
                    NotificationItemCard(
                        title = item.title,
                        description = item.description,
                        time = item.time,
                        isNeedsReview = true,
                        onClick = { onNotificationClick(item) }
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Sebelumnya", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
            previousItems.forEach { item ->
                NotificationItemCard(
                    title = item.title,
                    description = item.description,
                    time = item.time,
                    isNeedsReview = false,
                    onClick = { onNotificationClick(item) }
                )
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    title: String,
    description: String,
    time: String,
    isNeedsReview: Boolean,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isNeedsReview) SBColor.Primary_100 else Color.White
        ),
        border = BorderStroke(1.dp, if (isNeedsReview) SBColor.Primary_200 else SBColor.Grey_100)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isNeedsReview) Color.White.copy(alpha = 0.8f) else SBColor.Secondary_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(
                            id = if (isNeedsReview) R.drawable.ic_warning else R.drawable.ic_store
                        ),
                        contentDescription = null,
                        tint = if (isNeedsReview) SBColor.Primary_550 else SBColor.Secondary_500,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                        color = SBColor.Grey_700,
                        modifier = Modifier.weight(1f)
                    )
                    if (isNeedsReview) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color = SBColor.Primary_550, shape = CircleShape)
                        )
                    }
                }
                Text(text = description, fontSize = 12.sp, color = SBColor.Grey_600)
                Text(
                    text = time,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SBColor.Grey_500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
