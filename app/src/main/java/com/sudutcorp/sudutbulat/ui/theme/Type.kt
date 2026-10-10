package com.sudutcorp.sudutbulat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sudutcorp.sudutbulat.R

val PlusJakartaSans = FontFamily(
    Font(
        R.font.PlusJakartaSans_Bold,
        FontWeight.Bold,
        FontStyle.Normal
    ),
    Font(
        R.font.PlusJakartaSans_ExtraBold,
        FontWeight.ExtraBold,
        FontStyle.Normal
    ),
    Font(
        R.font.PlusJakartaSans_ExtraLight,
        FontWeight.ExtraLight,
        FontStyle.Normal
    ),
    Font(
        R.font.PlusJakartaSans_Light,
        FontWeight.Light,
        FontStyle.Normal
    ),
    Font(
        R.font.PlusJakartaSans_Medium,
        FontWeight.Medium,
        FontStyle.Normal
    ),
    Font(
        R.font.PlusJakartaSans_Regular,
        FontWeight.Normal,
        FontStyle.Normal
    ),
    Font(
        R.font.PlusJakartaSans_SemiBold,
        FontWeight.SemiBold,
        FontStyle.Normal
    ),
    Font(
        R.font.PlusJakartaSans_BoldItalic,
        FontWeight.Bold,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_ExtraBoldItalic,
        FontWeight.ExtraBold,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_ExtraLightItalic,
        FontWeight.ExtraLight,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_LightItalic,
        FontWeight.Light,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_MediumItalic,
        FontWeight.Medium,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_Italic,
        FontWeight.Normal,
        FontStyle.Italic
    ),
    Font(
        R.font.PlusJakartaSans_SemiBoldItalic,
        FontWeight.SemiBold,
        FontStyle.Italic
    ),
);

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    displayLarge = TextStyle(fontFamily = PlusJakartaSans),
    headlineLarge = TextStyle(fontFamily = PlusJakartaSans),
    titleLarge = TextStyle(fontFamily = PlusJakartaSans),
    bodyMedium = TextStyle(fontFamily = PlusJakartaSans),
    labelLarge = TextStyle(fontFamily = PlusJakartaSans),
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)