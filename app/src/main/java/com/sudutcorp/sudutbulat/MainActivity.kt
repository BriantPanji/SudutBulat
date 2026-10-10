package com.sudutcorp.sudutbulat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sudutcorp.sudutbulat.ui.theme.SBColor
import com.sudutcorp.sudutbulat.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val auth = FirebaseAuth.getInstance()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true, widthDp = 390)
@Composable
private fun HomePagePrev() {
    MaterialTheme(
        typography = Typography ,
        colorScheme = lightColorScheme(
            primary = SBColor.Primary,
            background = Color.White
        )
    ) {
        Scaffold() { innerPadding ->
            HomePage(modifier = Modifier.padding(innerPadding))
        }

    }
}

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sudut Bulat", fontSize = 14.sp, color = SBColor.Secondary_600)
                Text("Selamat pagi, Arif", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            }
            Row(Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SBColor.Primary_100),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("PD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_500)
            }
        }
        Row() {
//            Icon()
        }
    }
}

