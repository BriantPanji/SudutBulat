package com.sudutcorp.sudutbulat.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor

@Composable
fun SBLabeledTextField(
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

@Composable
fun SBSelectField(
    label: String,
    valueText: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        OutlinedCard(
            onClick = onClick,
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
                    text = valueText,
                    fontSize = 14.sp,
                    color = if (isPlaceholder) SBColor.Grey_500 else SBColor.Grey_700,
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

@Composable
fun SBInlineAlert(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SBColor.Secondary_500,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SBBottomCtaBar(
    label: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
            Button(
                onClick = onClick,
                enabled = !isLoading,
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
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SBConfirmDialog(
    title: String,
    description: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    cancelText: String = "Batal",
    iconRes: Int? = null,
    iconBgColor: Color = SBColor.Secondary_100,
    iconTint: Color = SBColor.Secondary_500
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (iconRes != null) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = iconBgColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = title,
                                tint = iconTint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = SBColor.Grey_500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Grey_100,
                            contentColor = SBColor.Grey_700
                        )
                    ) {
                        Text(text = cancelText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Primary_550,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = confirmText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SBSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier
) {
    val message = data.visuals.message
    val isError = message.startsWith("Gagal", ignoreCase = true) ||
        message.contains("error", ignoreCase = true) ||
        message.contains("PERMISSION_DENIED", ignoreCase = true)

    val borderColor = if (isError) SBColor.Primary_200 else SBColor.Success_300
    val iconBgColor = if (isError) SBColor.Primary_100 else SBColor.Success_100
    val iconTint = if (isError) SBColor.Primary_550 else SBColor.Success_600
    val iconRes = if (isError) R.drawable.ic_warning else R.drawable.ic_info

    key(data) {
        val dismissState = rememberSwipeToDismissBoxState()
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                data.dismiss()
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {},
            modifier = modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = iconBgColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = if (isError) "Error" else "Info",
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = message,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_700,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = { data.dismiss() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            shape = CircleShape,
                            color = SBColor.Grey_100
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✕",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Grey_600
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
