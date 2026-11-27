package com.sudutcorp.sudutbulat.feature.distribution

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sudutcorp.sudutbulat.core.components.SBBottomCtaBar
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.RoutinePresetItem

@Composable
fun RoutinePresetEditorBottomSheet(
    outletName: String,
    activeMaterials: List<Material>,
    initialPreset: List<RoutinePresetItem>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<RoutinePresetItem>) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val initialMap = remember(outletName, initialPreset) {
        initialPreset.filter { it.quantity > 0 }.associate { it.materialId to it.quantity.toString() }
    }
    var draftQuantities by rememberSaveable(outletName, initialPreset.hashCode()) {
        mutableStateOf(initialMap)
    }
    val selectedCount = remember(draftQuantities) {
        draftQuantities.values.count { (it.toIntOrNull() ?: 0) > 0 }
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            ) {
                SBSubHeader(
                    eyebrow = "Preset distribusi rutin",
                    title = outletName,
                    onBackClick = { if (!isSaving) onDismiss() },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )

                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "preset_intro", contentType = "intro") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Jumlah ini akan terisi otomatis setiap kali Gudang membuat distribusi rutin ke outlet ini.",
                                fontSize = 13.sp,
                                color = SBColor.Grey_500
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Daftar bahan mentah",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                                Text(
                                    text = "$selectedCount bahan diatur",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCount > 0) SBColor.Primary_550 else SBColor.Grey_500
                                )
                            }
                        }
                    }

                    items(
                        items = activeMaterials,
                        key = { it.id },
                        contentType = { "preset_material_row" }
                    ) { mat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = mat.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                                Text(
                                    text = "Satuan: ${mat.unit.firestoreValue}",
                                    fontSize = 12.sp,
                                    color = SBColor.Grey_500
                                )
                            }
                            OutlinedTextField(
                                value = draftQuantities[mat.id].orEmpty(),
                                onValueChange = { input ->
                                    val digits = input.filter(Char::isDigit)
                                    val updated = draftQuantities.toMutableMap()
                                    if (digits.isBlank()) updated.remove(mat.id) else updated[mat.id] = digits
                                    draftQuantities = updated
                                },
                                modifier = Modifier.width(108.dp),
                                placeholder = { Text("0", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SBColor.Primary_550,
                                    unfocusedBorderColor = SBColor.Grey_200
                                )
                            )
                        }
                    }
                }

                SBBottomCtaBar(
                    label = "Simpan preset ($selectedCount bahan)",
                    isLoading = isSaving,
                    onClick = {
                        val items = activeMaterials.mapNotNull { mat ->
                            val qty = draftQuantities[mat.id]?.toIntOrNull() ?: 0
                            if (qty > 0) RoutinePresetItem(materialId = mat.id, quantity = qty) else null
                        }
                        onSave(items)
                    }
                )
            }
        }
    }
}
