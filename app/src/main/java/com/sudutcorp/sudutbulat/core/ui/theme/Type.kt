package com.sudutcorp.sudutbulat.core.ui.theme

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
        R.font.plus_jakarta_sans_bold,
        FontWeight.Bold,
        FontStyle.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_extra_bold,
        FontWeight.ExtraBold,
        FontStyle.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_extra_light,
        FontWeight.ExtraLight,
        FontStyle.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_light,
        FontWeight.Light,
        FontStyle.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_medium,
        FontWeight.Medium,
        FontStyle.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_regular,
        FontWeight.Normal,
        FontStyle.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_semi_bold,
        FontWeight.SemiBold,
        FontStyle.Normal
    ),
    Font(
        R.font.plus_jakarta_sans_bold_italic,
        FontWeight.Bold,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_extra_bold_italic,
        FontWeight.ExtraBold,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_extra_light_italic,
        FontWeight.ExtraLight,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_light_italic,
        FontWeight.Light,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_medium_italic,
        FontWeight.Medium,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_italic,
        FontWeight.Normal,
        FontStyle.Italic
    ),
    Font(
        R.font.plus_jakarta_sans_semi_bold_italic,
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
