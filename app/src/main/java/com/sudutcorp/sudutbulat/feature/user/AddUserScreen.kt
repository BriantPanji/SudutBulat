package com.sudutcorp.sudutbulat.feature.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.outlet.FormLabeledTextField
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEffect
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    preselectedOutletId: String? = null,
    uiState: OwnerUiState = OwnerUiState(),
    effects: Flow<OwnerUiEffect> = emptyFlow(),
    onEvent: (OwnerUiEvent) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var showOutletSheet by rememberSaveable { mutableStateOf(false) }
    val form = uiState.addUserForm
    val selectedOutlet = uiState.outletItems.firstOrNull { it.id == form.selectedOutletId }
        ?: uiState.outletItems.firstOrNull()

    LaunchedEffect(preselectedOutletId) {
        if (!preselectedOutletId.isNullOrBlank()) {
            onEvent(OwnerUiEvent.InitAddUserForm(preselectedOutletId))
        }
    }

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            if (effect is OwnerUiEffect.NavigateBack) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    Button(
                        onClick = { onEvent(OwnerUiEvent.SubmitNewUser) },
                        enabled = !form.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Primary_550,
                            contentColor = Color.White
                        )
                    ) {
                        if (form.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Buat akun pengguna",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sub Header (204:6610)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(color = SBColor.Grey_100, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_left),
                        contentDescription = "Kembali",
                        tint = SBColor.Grey_700,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Owner",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_600
                    )
                    Text(
                        text = "Tambah pengguna",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Peran pengguna (2 Role Segments sesuai PRD 3.1: Gudang & Outlet)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Peran pengguna",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleSegmentCard(
                        modifier = Modifier.weight(1f),
                        label = "Gudang",
                        iconRes = R.drawable.ic_box,
                        isSelected = form.selectedRole == Role.WAREHOUSE,
                        onClick = { onEvent(OwnerUiEvent.AddUserRoleSelected(Role.WAREHOUSE)) }
                    )
                    RoleSegmentCard(
                        modifier = Modifier.weight(1f),
                        label = "Outlet",
                        iconRes = R.drawable.ic_store,
                        isSelected = form.selectedRole == Role.OUTLET,
                        onClick = { onEvent(OwnerUiEvent.AddUserRoleSelected(Role.OUTLET)) }
                    )
                }
            }

            // Nama lengkap
            FormLabeledTextField(
                label = "Nama lengkap",
                value = form.fullName,
                placeholder = "Dimas Prasetyo",
                leadingIconRes = R.drawable.ic_user,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                onValueChange = { onEvent(OwnerUiEvent.AddUserNameChanged(it)) }
            )

            // Email atau nomor HP
            FormLabeledTextField(
                label = "Email atau nomor HP",
                value = form.emailOrPhone,
                placeholder = "dimas@sudutbulat.id",
                leadingIconRes = R.drawable.ic_info,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                onValueChange = { onEvent(OwnerUiEvent.AddUserEmailChanged(it)) }
            )

            // Kata sandi sementara
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kata sandi sementara",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_700
                )
                OutlinedTextField(
                    value = form.password,
                    onValueChange = { onEvent(OwnerUiEvent.AddUserPasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "••••••••",
                            fontSize = 14.sp,
                            color = SBColor.Grey_500
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Kata sandi sementara",
                            tint = SBColor.Grey_500,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = if (form.isPasswordVisible) "Tutup" else "Lihat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550,
                            modifier = Modifier
                                .clickable { onEvent(OwnerUiEvent.ToggleAddUserPasswordVisibility) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    },
                    visualTransformation = if (form.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SBColor.Primary_550,
                        unfocusedBorderColor = SBColor.Grey_200,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = SBColor.Grey_700,
                        unfocusedTextColor = SBColor.Grey_700
                    )
                )
            }

            // Select Field: Tugaskan ke outlet (aktif jika Role.OUTLET)
            if (form.selectedRole == Role.OUTLET) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tugaskan ke outlet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_700
                    )
                    OutlinedCard(
                        onClick = { showOutletSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SBColor.Grey_200)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedOutlet?.displayName ?: "Pilih outlet",
                                fontSize = 14.sp,
                                color = SBColor.Grey_700,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron),
                                contentDescription = null,
                                tint = SBColor.Grey_500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Inline Alert (#F8EBD8, teks #645434)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SBColor.Secondary_100
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = null,
                        tint = SBColor.Secondary_500,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (form.selectedRole == Role.OUTLET) {
                            "Staf outlet ditugaskan permanen ke satu outlet."
                        } else if (form.selectedRole == Role.WAREHOUSE) {
                            "Admin gudang bertugas di Gudang Pusat untuk distribusi stok."
                        } else {
                            "Akun Owner memiliki akses penuh ke pemantauan bisnis."
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SBColor.Secondary_500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            form.errorMessage?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Primary_550
                )
            }
        }
    }

    if (showOutletSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOutletSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pilih outlet penugasan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                uiState.outletItems.forEach { outlet ->
                    val isSelected = outlet.id == (selectedOutlet?.id ?: "")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEvent(OwnerUiEvent.AddUserOutletSelected(outlet.id))
                                showOutletSheet = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = outlet.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SBColor.Grey_700
                            )
                            Text(
                                text = outlet.address,
                                fontSize = 12.sp,
                                color = SBColor.Grey_500
                            )
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onEvent(OwnerUiEvent.AddUserOutletSelected(outlet.id))
                                showOutletSheet = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = SBColor.Primary_550)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleSegmentCard(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) SBColor.Primary_100 else SBColor.Grey_100
    val contentColor = if (isSelected) SBColor.Primary_550 else SBColor.Grey_600
    val borderMod = if (isSelected) {
        Modifier.border(1.dp, SBColor.Primary_200, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(borderMod)
            .background(bgColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
