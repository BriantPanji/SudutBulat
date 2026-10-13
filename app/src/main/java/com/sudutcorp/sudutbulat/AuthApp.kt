package com.sudutcorp.sudutbulat

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.sudutcorp.sudutbulat.auth.AuthRepository
import com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar.NavbarNavigation
import com.sudutcorp.sudutbulat.ui.theme.SBColor
import com.sudutcorp.sudutbulat.ui.theme.Typography

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
sealed class AuthRoutes {
    @Serializable
    object Login : AuthRoutes()

    @Serializable
    object Main : AuthRoutes()
}

@Composable
fun AuthApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.Login,
        modifier = modifier
    ) {
        composable<AuthRoutes.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AuthRoutes.Main) {
                        popUpTo<AuthRoutes.Login> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<AuthRoutes.Main> {
            NavbarNavigation(
                onLogout = {
                    runCatching { FirebaseAuth.getInstance().signOut() }
                    navController.navigate(AuthRoutes.Login) {
                        popUpTo<AuthRoutes.Main> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val collapseRangePx = with(density) { (HeaderMaxHeight - HeaderMinHeight).toPx() }
    val collapseFraction = (scrollState.value / collapseRangePx).coerceIn(0f, 1f)

    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(HeaderMaxHeight))
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp, 16.dp),
                    Arrangement.spacedBy(24.dp)
                ) {
                    LoginTextBody()
                    LoginFormSection(onLoginSuccess = onLoginSuccess)
                }
                Spacer(Modifier.height(280.dp))
            }
            LoginHeader(collapseFraction = collapseFraction)
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PrevLogin() {
    MaterialTheme(
        typography = Typography,
        colorScheme = lightColorScheme(
            primary = SBColor.Primary,
            background = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen()
        }
    }
}

private val HeaderMaxHeight = 260.dp
private val HeaderMinHeight = 20.dp

/**
 * @param collapseFraction 0f = ukuran penuh, 1f = ukuran minimum.
 */
@Composable
fun LoginHeader(modifier: Modifier = Modifier, collapseFraction: Float = 0f) {
    val height = lerp(HeaderMaxHeight, HeaderMinHeight, collapseFraction)
    val corner = lerp(36.dp, 4.dp, collapseFraction)
    val logoSize = lerp(50.dp, 28.dp, collapseFraction)
    val fontSize = lerp(40.sp, 22.sp, collapseFraction)
    val spacing = lerp(8.dp, 6.dp, collapseFraction)

    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(bottomStart = corner, bottomEnd = corner))
            .background(SBColor.Primary_400),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(R.drawable.lg_sudutbulat_icon), "Logo", Modifier.size(logoSize),
            colorFilter = ColorFilter.tint(SBColor.Secondary_100)
        )
        Spacer(Modifier.width(spacing))
        Text("SudutBulat", fontSize = fontSize, fontWeight = FontWeight.ExtraBold, color = SBColor.Secondary_100)
    }
}

@Composable
fun LoginTextBody(modifier: Modifier = Modifier) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Selamat datang kembali", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
        Text("Masuk ke akun Anda", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(stringResource(R.string.login_text_body), fontSize = 14.sp, fontWeight = FontWeight.Normal)
    }
}

@Composable
fun LoginFormSection(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
    repository: AuthRepository = remember { AuthRepository() }
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = login,
                    onValueChange = {
                        login = it
                        message = ""
                    },
                    label = { Text("Email/Kode Login") },
                    placeholder = {Text("Masukkan email / kode login Anda", fontSize = 13.sp)},
                    leadingIcon = { Icon(painterResource(R.drawable.ic_user), contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = SBColor.Primary_550,
                        unfocusedBorderColor = SBColor.Grey_300,
                        focusedLabelColor = SBColor.Primary_550,
                        unfocusedLabelColor = SBColor.Grey_500,
                        focusedLeadingIconColor = SBColor.Primary_550,
                        unfocusedLeadingIconColor = SBColor.Grey_400,
                        cursorColor = SBColor.Primary_550,
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        message = ""
                    },
                    label = { Text("Password") },
                    placeholder = {Text("Password", fontSize = 13.sp)},
                    leadingIcon = { Icon(painterResource(R.drawable.ic_logout), contentDescription = null) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = SBColor.Primary_550,
                        unfocusedBorderColor = SBColor.Grey_300,
                        focusedLabelColor = SBColor.Primary_550,
                        unfocusedLabelColor = SBColor.Grey_500,
                        focusedLeadingIconColor = SBColor.Primary_550,
                        unfocusedLeadingIconColor = SBColor.Grey_400,
                        cursorColor = SBColor.Primary_550,
                    )
                )
            }
            Row(Modifier.fillMaxWidth().padding(0.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {}, contentPadding = PaddingValues(vertical = 0.dp, horizontal = 4.dp)) {
                    Text("Lupa kata sandi?", color = SBColor.Primary_550, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            message?.let {
                Text(
                    it,
                    modifier = Modifier.padding(start = 2.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val cleanLogin = login.trim()

                    if (cleanLogin.isBlank() || password.isBlank()) {
                        message = "Kode Login/Email dan password wajib diisi."
                    } else {
                        isLoading = true
                        message = ""

                        repository.loginWithCodeOrEmail(
                            login = cleanLogin,
                            password = password,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onError = { error ->
                                isLoading = false
                                message = error
                            }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", fontWeight = FontWeight.Medium)
                }
            }

        }

    }
}