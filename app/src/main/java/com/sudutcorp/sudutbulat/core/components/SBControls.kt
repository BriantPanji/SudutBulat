package com.sudutcorp.sudutbulat.core.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import kotlin.math.roundToInt

@Composable
fun <T> SBSegmentedTabs(
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return

    val spacing = 4.dp
    val density = LocalDensity.current
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
            .height(36.dp)
    ) {
        val tabWidth = (maxWidth - spacing * (options.size - 1)) / options.size
        val targetOffsetPx = with(density) { ((tabWidth + spacing) * selectedIndex).toPx() }
        val indicatorOffsetPx by animateFloatAsState(
            targetValue = targetOffsetPx,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "sb_segmented_tab_indicator"
        )

        Box(
            modifier = Modifier
                .width(tabWidth)
                .fillMaxHeight()
                .offset { IntOffset(indicatorOffsetPx.roundToInt(), 0) }
                .background(color = Color.White, shape = RoundedCornerShape(9.dp))
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab
                        ) { onSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelOf(option),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) SBColor.Grey_700 else SBColor.Grey_500
                    )
                }
            }
        }
    }
}

@Composable
fun SBSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = SBColor.Grey_500
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = placeholder,
                tint = SBColor.Grey_500,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Hapus pencarian",
                        tint = SBColor.Grey_500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SBColor.Primary,
            unfocusedBorderColor = SBColor.Grey_200,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
fun SBFilterChipRow(
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (key, label) ->
            val isSelected = key == selectedKey
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(key) },
                label = {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                shape = RoundedCornerShape(9.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SBColor.Grey_700,
                    selectedLabelColor = Color.White,
                    containerColor = SBColor.Grey_100,
                    labelColor = SBColor.Grey_600
                ),
                border = null
            )
        }
    }
}

@Composable
fun <F> SBSortDropdownButton(
    fields: List<F>,
    selectedField: F,
    isDescending: Boolean,
    labelOf: (F) -> String,
    isAlphaField: (F) -> Boolean,
    defaultDescendingFor: (F) -> Boolean,
    onSelectSort: (field: F, isDescending: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    ascLabelOf: (F) -> String = { if (isAlphaField(it)) "A – Z ↑" else "Terendah ↑" },
    descLabelOf: (F) -> String = { if (isAlphaField(it)) "Z – A ↓" else "Tertinggi ↓" }
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val arrow = if (isDescending) "↓" else "↑"

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text(
                text = "${labelOf(selectedField)} $arrow",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_600
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(228.dp)
                .background(Color.White),
            shape = RoundedCornerShape(14.dp),
            containerColor = Color.White
        ) {
            Text(
                text = "Urutkan berdasarkan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_500,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            fields.forEach { field ->
                val isSelected = field == selectedField
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = labelOf(field),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SBColor.Primary_550 else SBColor.Grey_700
                            )
                            if (isSelected) {
                                Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                            }
                        }
                    },
                    onClick = {
                        val nextDesc = if (isSelected) !isDescending else defaultDescendingFor(field)
                        onSelectSort(field, nextDesc)
                    }
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = SBColor.Grey_100,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            val ascLabel = ascLabelOf(selectedField)
            val descLabel = descLabelOf(selectedField)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(false to ascLabel, true to descLabel).forEach { (desc, label) ->
                    val isDirSelected = isDescending == desc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDirSelected) SBColor.Primary_100 else SBColor.Grey_100)
                            .clickable {
                                onSelectSort(selectedField, desc)
                                expanded = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDirSelected) SBColor.Primary_550 else SBColor.Grey_600
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SBDatePickerDialog(
    initialSelectedDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit,
    onReset: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
    val dateFormatter = remember { DatePickerDefaults.dateFormatter() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val pickerColors = DatePickerDefaults.colors(
        containerColor = Color.White,
        selectedDayContainerColor = SBColor.Primary_550,
        selectedDayContentColor = Color.White,
        todayContentColor = SBColor.Primary_550,
        todayDateBorderColor = SBColor.Primary_550
    )

    LaunchedEffect(datePickerState.displayMode) {
        if (datePickerState.displayMode == DisplayMode.Picker) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        // Prevent Android's Dialog window from resizing frame-by-frame while the IME animates closed.
        modifier = if (datePickerState.displayMode == DisplayMode.Input) {
            Modifier.padding(bottom = 96.dp)
        } else {
            Modifier
        },
        colors = pickerColors,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        confirmButton = {
            TextButton(onClick = { onConfirm(datePickerState.selectedDateMillis) }) {
                Text("Pilih", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (initialSelectedDateMillis != null) {
                    TextButton(onClick = onReset) {
                        Text("Reset", fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal", fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                }
            }
        }
    ) {
        // Bypass M3's private AnimatedContent + SizeTransform to prevent choppy dialog resizing on mode switch.
        key(datePickerState.displayMode) {
            DatePicker(
                state = datePickerState,
                dateFormatter = dateFormatter,
                colors = pickerColors
            )
        }
    }
}
