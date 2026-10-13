package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.common.util.AndroidUtilsLight
import com.sudutcorp.sudutbulat.AIFab
import com.sudutcorp.sudutbulat.R

//
//val PlusJakartaSansFamily = FontFamily(
//    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
//    Font(R.font.plus_jakarta_sans_semi_bold, FontWeight.SemiBold),
//    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
//    Font(R.font.plus_jakarta_sans_extra_bold, FontWeight.ExtraBold),
//)


@Composable
fun NavbarOwnerReportScreen(navController: NavHostController) {
//    Header
    Column(modifier = Modifier
        .padding(20.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column() {
                Row() {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Owner",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Laporan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp
                        )
                    }
                }
            }
            Column() {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = Color(0xFFF8EBD8), shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AR",
                            color = Color(0xFFB62C08),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                            .size(12.dp)
                            .border(width = 2.dp, shape = CircleShape, color = Color.White)
                            .background(
                                color = Color(0xFF168257),
                                shape = CircleShape
                            )
                    ) {
                    }
                }
            }
        }
        //    Akhir dari Header

        //    Filter waktu
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(color = Color(0xFFF1F0F0), shape = RoundedCornerShape(12.dp))
                .height(46.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .height(40.dp),

                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = "7 Hari")
            }
            Column(modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(size = 9.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = "30 Hari")
            }
            Column(modifier = Modifier
                .weight(1f)
                .height(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = "3 Bulan")
            }

        }
//    Akhir dari filter waktu
    }
}


@Preview
@Composable
private fun PreviewBos() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            SBNavbar(navController = navController)
        },
        floatingActionButton = { AIFab() },
        modifier = Modifier
    ) { innerPadding ->
        NavbarOwnerReportScreen(navController) // INI GANTI KE COMPOSABLE YG KELEN KERJAIN
    }
}