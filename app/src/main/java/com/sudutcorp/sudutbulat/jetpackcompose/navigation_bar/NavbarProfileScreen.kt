package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.ui.theme.SBColor

@Composable
fun NavbarProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
    ) {

        //HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .width(62.dp)
                    .height(55.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            )
            {
                Text(
                    text = "Owner",
                    // Plus Jakarta Sans/14/SemiBold
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(600),
                    color = SBColor.Grey_500,
                )

                Text(
                    text = "Profil",
                    // Plus Jakarta Sans/25/Bold
                    fontSize = 25.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight(700),
                    color = SBColor.Grey_700,
                ) // Child views.
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
                    // Plus Jakarta Sans/14/ExtraBold
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(800),
                        color = SBColor.Primary_500,
                    )
                )
            }
        }

        //BODY
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,

            ) { //Profile
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
                        // Plus Jakarta Sans/25/ExtraBold
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

                    // Plus Jakarta Sans/20/Bold
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
                        // Plus Jakarta Sans/12/Bold
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

                    // Plus Jakarta Sans/14/Regular
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.3.sp,
                        fontWeight = FontWeight(400),
                        color = SBColor.Grey_500,
                    )
                )
            }

            //Setting Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                //Notifikasi

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
                        .padding(
                            start = 4.dp,
                            top = 12.dp,
                            end = 4.dp,
                            bottom = 12.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 12.dp,
                        alignment = Alignment.Start
                    ),
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
                        horizontalArrangement = Arrangement.spacedBy(
                            0.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bell),
                            contentDescription = "Profile",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Notifikasi",
                        // Plus Jakarta Sans/14/SemiBold
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Profile",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )

                }

                //Kemanan
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
                        .padding(
                            start = 4.dp,
                            top = 12.dp,
                            end = 4.dp,
                            bottom = 12.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 12.dp,
                        alignment = Alignment.Start
                    ),
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
                        horizontalArrangement = Arrangement.spacedBy(
                            0.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Profile",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Keamanan & kata sandi",
                        // Plus Jakarta Sans/14/SemiBold
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Profile",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )

                }

                //Bahasa
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
                        .padding(
                            start = 4.dp,
                            top = 12.dp,
                            end = 4.dp,
                            bottom = 12.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 12.dp,
                        alignment = Alignment.Start
                    ),
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
                        horizontalArrangement = Arrangement.spacedBy(
                            0.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Profile",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Bahasa",
                        // Plus Jakarta Sans/14/SemiBold
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Profile",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )

                }

                //Bantuan
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
                        .padding(
                            start = 4.dp,
                            top = 12.dp,
                            end = 4.dp,
                            bottom = 12.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 12.dp,
                        alignment = Alignment.Start
                    ),
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
                        horizontalArrangement = Arrangement.spacedBy(
                            0.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "Profile",
                            tint = SBColor.Secondary_500
                        )
                    }
                    Text(
                        modifier = Modifier.width(260.dp),
                        text = "Bantuan",
                        // Plus Jakarta Sans/14/SemiBold
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.3.sp,
                            fontWeight = FontWeight(600),
                            color = SBColor.Grey_700
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron),
                        contentDescription = "Profile",
                        modifier = Modifier.size(18.dp),
                        tint = SBColor.Secondary_500
                    )

                }

                //Keluar
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
                        .padding(
                            start = 4.dp,
                            top = 12.dp,
                            end = 4.dp,
                            bottom = 12.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 12.dp,
                        alignment = Alignment.Start
                    ),
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
                        horizontalArrangement = Arrangement.spacedBy(
                            0.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
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
                        // Plus Jakarta Sans/14/SemiBold
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

//@Preview
//@Composable
//private fun Previewbos() {
//    val navController = rememberNavController()
//    Scaffold(
//        bottomBar = {
//            SBNavbar(navController = navController)
//        },
//        floatingActionButton = { },
//        modifier = Modifier
//    ) { innerPadding ->
//        NavbarProfileScreen(navController)
//    }
//}