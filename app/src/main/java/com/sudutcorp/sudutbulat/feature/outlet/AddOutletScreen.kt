package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.ThousandSeparatorVisualTransformation
import com.sudutcorp.sudutbulat.core.util.TimeVisualTransformation
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEffect
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.defaultSeededUsers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOutletScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    effects: Flow<OwnerUiEffect> = emptyFlow(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    onAddNewUserClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var showStaffSheet by rememberSaveable { mutableStateOf(false) }
    val staffSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val thousandTransformation = remember { ThousandSeparatorVisualTransformation() }
    val timeTransformation = remember { TimeVisualTransformation() }

    val form = uiState.addOutletForm
    val availableStaff = remember(uiState.users) {
        uiState.users.ifEmpty { defaultSeededUsers() }.filter {
            it.isActive && it.role == Role.OUTLET
        }
    }
    val selectedStaffNames = remember(availableStaff, form.selectedStaffIds) {
        availableStaff
            .filter { it.id in form.selectedStaffIds }
            .joinToString(", ") { it.name }
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
                        onClick = { onEvent(OwnerUiEvent.SubmitNewOutlet) },
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
                                text = "Simpan outlet",
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
            // Sub Header (204:6480)
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
                        text = "Tambah outlet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Field: Nama outlet
            FormLabeledTextField(
                label = "Nama outlet",
                value = form.name,
                placeholder = "Outlet Kemang",
                leadingIconRes = R.drawable.ic_store,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                onValueChange = { onEvent(OwnerUiEvent.AddOutletNameChanged(it)) }
            )

            // Field: Alamat lengkap
            FormLabeledTextField(
                label = "Alamat lengkap",
                value = form.address,
                placeholder = "Jl. Kemang Raya No. 12",
                leadingIconRes = R.drawable.ic_store,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                onValueChange = { onEvent(OwnerUiEvent.AddOutletAddressChanged(it)) }
            )

            // Row: Jam buka & Jam tutup (hanya angka, diformat HH.mm)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormLabeledTextField(
                    modifier = Modifier.weight(1f),
                    label = "Jam buka",
                    value = form.openTime,
                    placeholder = "08.00",
                    leadingIconRes = R.drawable.calendar,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    visualTransformation = timeTransformation,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                    onValueChange = { onEvent(OwnerUiEvent.AddOutletOpenTimeChanged(it)) }
                )
                FormLabeledTextField(
                    modifier = Modifier.weight(1f),
                    label = "Jam tutup",
                    value = form.closeTime,
                    placeholder = "21.00",
                    leadingIconRes = R.drawable.calendar,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    visualTransformation = timeTransformation,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                    onValueChange = { onEvent(OwnerUiEvent.AddOutletCloseTimeChanged(it)) }
                )
            }

            // Field: Target penjualan harian (format ribuan otomatis dengan titik)
            FormLabeledTextField(
                label = "Target penjualan harian",
                value = form.dailyTargetInput,
                placeholder = "2.000.000",
                leadingIconRes = R.drawable.ic_wallet,
                prefixText = "Rp",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                visualTransformation = thousandTransformation,
                onImeAction = { focusManager.clearFocus() },
                onValueChange = { onEvent(OwnerUiEvent.AddOutletTargetChanged(it)) }
            )

            // Select Field: Staf outlet (min. 1)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Staf outlet (min. 1)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_700
                )
                OutlinedCard(
                    onClick = {
                        focusManager.clearFocus()
                        showStaffSheet = true
                    },
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
                            text = if (selectedStaffNames.isNotBlank()) {
                                selectedStaffNames
                            } else {
                                "Pilih staf yang ditugaskan"
                            },
                            fontSize = 14.sp,
                            color = if (selectedStaffNames.isNotBlank()) SBColor.Grey_700 else SBColor.Grey_500,
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
                        text = "Staf ditugaskan permanen ke outlet ini dan tidak berpindah lokasi.",
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

    if (showStaffSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStaffSheet = false },
            sheetState = staffSheetState,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih staf outlet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    TextButton(
                        onClick = {
                            showStaffSheet = false
                            onAddNewUserClick()
                        }
                    ) {
                        Text(
                            text = "+ Buat staf baru",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = availableStaff,
                        key = { it.id },
                        contentType = { "staff_option" }
                    ) { staff ->
                        val isChecked = staff.id in form.selectedStaffIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(OwnerUiEvent.ToggleAddOutletStaff(staff.id)) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = staff.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                                Text(
                                    text = staff.email,
                                    fontSize = 12.sp,
                                    color = SBColor.Grey_500
                                )
                            }
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { onEvent(OwnerUiEvent.ToggleAddOutletStaff(staff.id)) },
                                colors = CheckboxDefaults.colors(checkedColor = SBColor.Primary_550)
                            )
                        }
                    }
                }

                Button(
                    onClick = { showStaffSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    Text("Selesai", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun FormLabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    leadingIconRes: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefixText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onImeAction: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SBColor.Grey_700
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = SBColor.Grey_500
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = leadingIconRes),
                    contentDescription = label,
                    tint = SBColor.Grey_500,
                    modifier = Modifier.size(18.dp)
                )
            },
            prefix = prefixText?.let { prefix ->
                {
                    Text(
                        text = prefix,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_700
                    )
                }
            },
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
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
}
