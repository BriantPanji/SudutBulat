package com.sudutcorp.sudutbulat.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor

@Composable
fun ProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding( bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .width(62.dp)
                    .height(55.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Owner",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(600),
                    color = SBColor.Grey_500,
                )
                Text(
                    text = "Profil",
                    fontSize = 25.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight(700),
                    color = SBColor.Grey_700,
                )
            }
            Row(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .background(color = SBColor.Secondary_100, shape = RoundedCornerShape(999.dp)),
                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AR",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(800),
                        color = SBColor.Primary_500,
                    )
                )
            }
        }

        // BODY
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Profile Card
            Column(
                Modifier.padding(top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    Modifier
                        .width(80.dp)
                        .height(80.dp)
                        .background(
                            color = SBColor.Secondary_100,
                            shape = RoundedCornerShape(size = 999.dp)
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        0.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "AR",
                        style = TextStyle(
                            fontSize = 25.sp,
                            lineHeight = 31.25.sp,
                            fontWeight = FontWeight(800),
                            color = SBColor.Primary_500,
                        )
                    )
                }
                Text(
                    text = "Arif Rahman",
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight(700),
                        color = SBColor.Grey_700,
                    )
                )
                Row(
                    Modifier
                        .width(57.dp)
                        .height(25.dp)
                        .background(color = SBColor.Grey_100, shape = RoundedCornerShape(99.dp))
                        .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        0.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Owner",
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 17.4.sp,
                            fontWeight = FontWeight(700),
                            color = SBColor.Grey_500
                        )
                    )
                }
                Text(
                    text = "arif@sudutbulat.id",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.3.sp,
                        fontWeight = FontWeight(400),
                        color = SBColor.Grey_500,
                    )
                )
            }

            // Setting Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                // Notifikasi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = SBColor.Grey_100,
                                start = Offset(0f, size.height - strokeWidth / 2),
                                end = Offset(size.width, size.height - strokeWidth / 2),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(
                                color = SBColor.Secondary_100,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bell),
                            contentDescription = "Notifikasi",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Notifikasi",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Detail",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )
                }

                // Keamanan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = SBColor.Grey_100,
                                start = Offset(0f, size.height - strokeWidth / 2),
                                end = Offset(size.width, size.height - strokeWidth / 2),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(
                                color = SBColor.Secondary_100,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Keamanan",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Keamanan & kata sandi",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Detail",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )
                }

                // Bahasa
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = SBColor.Grey_100,
                                start = Offset(0f, size.height - strokeWidth / 2),
                                end = Offset(size.width, size.height - strokeWidth / 2),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(
                                color = SBColor.Secondary_100,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Bahasa",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Bahasa",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Detail",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )
                }

                // Bantuan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = SBColor.Grey_100,
                                start = Offset(0f, size.height - strokeWidth / 2),
                                end = Offset(size.width, size.height - strokeWidth / 2),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(
                                color = SBColor.Secondary_100,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "Bantuan",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Bantuan",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Detail",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )
                }

                // Keluar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            runCatching { FirebaseAuth.getInstance().signOut() }
                            onLogout()
                        }
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            drawLine(
                                color = SBColor.Grey_100,
                                start = Offset(0f, size.height - strokeWidth / 2),
                                end = Offset(size.width, size.height - strokeWidth / 2),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(
                                color = Color(0xFFFFEDEC),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = "Keluar",
                            tint = Color(0xFFB62C08)
                        )
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "Keluar",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = Color(0xFFB62C08)
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Keluar",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFB62C08)
                    )
                }
            }
        }
    }
}
