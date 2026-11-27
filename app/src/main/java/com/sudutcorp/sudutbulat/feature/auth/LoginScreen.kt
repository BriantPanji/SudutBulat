package com.sudutcorp.sudutbulat.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudutcorp.sudutbulat.data.model.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val HeaderMaxHeight = 260.dp
private val HeaderMinHeight = 20.dp

private enum class FocusedLoginField {
    LOGIN,
    PASSWORD
}

// Plain non-snapshot holder so writing layout coordinates never triggers recomposition
private class LoginCoordsRef {
    var box: LayoutCoordinates? = null
    var loginField: LayoutCoordinates? = null
    var passwordField: LayoutCoordinates? = null
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = remember { AuthViewModel() },
    onLoginSuccess: (Role) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val collapseRangePx = with(density) { (HeaderMaxHeight - HeaderMinHeight).toPx() }

    val coordsRef = remember { LoginCoordsRef() }
    var focusedField by remember { mutableStateOf<FocusedLoginField?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val latestImeBottomPx by rememberUpdatedState(imeBottomPx)
    val isImeVisible = imeBottomPx > 0

    suspend fun scrollFocusedFieldToCenter(field: FocusedLoginField, currentImeBottomPx: Int) {
        val containerCoords = coordsRef.box?.takeIf { it.isAttached } ?: return
        val fieldCoords = when (field) {
            FocusedLoginField.LOGIN -> coordsRef.loginField
            FocusedLoginField.PASSWORD -> coordsRef.passwordField
        }?.takeIf { it.isAttached } ?: return

        val fieldCenterInBox = containerCoords.localPositionOf(
            fieldCoords,
            Offset(0f, fieldCoords.size.height / 2f)
        ).y
        val visibleTop = with(density) { HeaderMinHeight.toPx() }
        val visibleBottom = (containerCoords.size.height - currentImeBottomPx).toFloat().coerceAtLeast(visibleTop)
        val targetCenterInBox = (visibleTop + visibleBottom) / 2f
        val deltaScroll = fieldCenterInBox - targetCenterInBox
        val targetScroll = (scrollState.value + deltaScroll)
            .roundToInt()
            .coerceIn(0, scrollState.maxValue)

        scrollState.animateScrollTo(targetScroll)
    }

    LaunchedEffect(focusedField) {
        val field = focusedField ?: return@LaunchedEffect
        withFrameNanos { }
        scrollFocusedFieldToCenter(field, latestImeBottomPx)
    }

    LaunchedEffect(isImeVisible) {
        val field = focusedField ?: return@LaunchedEffect
        if (isImeVisible) {
            delay(150)
            scrollFocusedFieldToCenter(field, latestImeBottomPx)
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .onPlaced { coordsRef.box = it }
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
                    LoginFormSection(
                        uiState = uiState,
                        onLoginChange = viewModel::onLoginChanged,
                        onPasswordChange = viewModel::onPasswordChanged,
                        onSubmit = { viewModel.login(onSuccess = onLoginSuccess) },
                        onForgotPasswordClick = { showResetDialog = true },
                        onLoginPositioned = { coordsRef.loginField = it },
                        onPasswordPositioned = { coordsRef.passwordField = it },
                        onLoginFocusChanged = { isFocused ->
                            if (isFocused) {
                                focusedField = FocusedLoginField.LOGIN
                            } else if (focusedField == FocusedLoginField.LOGIN) {
                                focusedField = null
                            }
                        },
                        onPasswordFocusChanged = { isFocused ->
                            if (isFocused) {
                                focusedField = FocusedLoginField.PASSWORD
                            } else if (focusedField == FocusedLoginField.PASSWORD) {
                                focusedField = null
                            }
                        }
                    )
                }
                if (isImeVisible) {
                    Spacer(Modifier.height(320.dp))
                } else {
                    Spacer(Modifier.height(32.dp))
                }
            }
            LoginHeader(
                collapseFractionProvider = { (scrollState.value / collapseRangePx).coerceIn(0f, 1f) }
            )
        }
    }

    if (showResetDialog) {
        var resetEmail by remember { mutableStateOf(uiState.loginInput.takeIf { it.contains("@") }.orEmpty()) }
        var isSending by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf<String?>(null) }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSending) showResetDialog = false },
            containerColor = Color.White,
            title = { Text("Reset kata sandi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Masukkan email akun untuk menerima tautan reset kata sandi.", fontSize = 13.sp, color = SBColor.Grey_600)
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it; statusMessage = null },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isError) MaterialTheme.colorScheme.error else SBColor.Success_600
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSending,
                    onClick = {
                        coroutineScope.launch {
                            isSending = true
                            statusMessage = null
                            viewModel.sendPasswordResetEmail(resetEmail)
                                .onSuccess {
                                    isSending = false
                                    isError = false
                                    statusMessage = "Tautan reset kata sandi telah dikirim ke $resetEmail."
                                }
                                .onFailure { e ->
                                    isSending = false
                                    isError = true
                                    statusMessage = e.localizedMessage ?: "Gagal mengirim email reset."
                                }
                        }
                    }
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Kirim", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !isSending, onClick = { showResetDialog = false }) { Text("Tutup") }
            }
        )
    }
}

@Composable
fun LoginHeader(
    modifier: Modifier = Modifier,
    collapseFractionProvider: () -> Float = { 0f }
) {
    // Reading scrollState inside LoginHeader isolates scroll recompositions from LoginScreen & LoginFormSection
    val collapseFraction = collapseFractionProvider()
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
            painter = painterResource(R.drawable.lg_sudutbulat_icon),
            contentDescription = "Logo",
            modifier = Modifier.size(logoSize),
            colorFilter = ColorFilter.tint(SBColor.Secondary_100)
        )
        Spacer(Modifier.width(spacing))
        Text(
            text = "SudutBulat",
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = SBColor.Secondary_100
        )
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
    uiState: AuthUiState,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    onForgotPasswordClick: () -> Unit = {},
    onLoginPositioned: (LayoutCoordinates) -> Unit = {},
    onPasswordPositioned: (LayoutCoordinates) -> Unit = {},
    onLoginFocusChanged: (Boolean) -> Unit = {},
    onPasswordFocusChanged: (Boolean) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.loginInput,
                    onValueChange = onLoginChange,
                    label = { Text("Email/Kode Login") },
                    placeholder = { Text("Masukkan email / kode login Anda", fontSize = 13.sp) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_user), contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPlaced(onLoginPositioned)
                        .onFocusChanged { onLoginFocusChanged(it.isFocused) },
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
                    value = uiState.passwordInput,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    placeholder = { Text("Password", fontSize = 13.sp) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_lock), contentDescription = null) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!uiState.isLoading) {
                                focusManager.clearFocus()
                                onSubmit()
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPlaced(onPasswordPositioned)
                        .onFocusChanged { onPasswordFocusChanged(it.isFocused) },
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
                TextButton(onClick = onForgotPasswordClick, contentPadding = PaddingValues(vertical = 0.dp, horizontal = 4.dp)) {
                    Text("Lupa kata sandi?", color = SBColor.Primary_550, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.padding(start = 2.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isLoading) {
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PrevLogin() {
    com.sudutcorp.sudutbulat.core.ui.theme.SudutBulatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen()
        }
    }
}
