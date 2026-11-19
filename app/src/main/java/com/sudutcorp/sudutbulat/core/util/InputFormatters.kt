package com.sudutcorp.sudutbulat.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Formats a digit-only numeric string (e.g. "2000000") into Indonesian thousand-separated
 * display ("2.000.000") while keeping cursor mapping accurate during typing/deleting.
 */
class ThousandSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formatted = buildString {
            val len = raw.length
            for (i in raw.indices) {
                append(raw[i])
                val remaining = len - 1 - i
                if (remaining > 0 && remaining % 3 == 0) {
                    append('.')
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                if (clamped == 0) return 0
                val totalDots = (raw.length - 1) / 3
                val remainingAfterCursor = raw.length - clamped
                val dotsAfterCursor = if (remainingAfterCursor == 0) 0 else remainingAfterCursor / 3
                val dotsBeforeCursor = (totalDots - dotsAfterCursor).coerceAtLeast(0)
                return (clamped + dotsBeforeCursor).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                var digits = 0
                for (i in 0 until clamped) {
                    if (formatted[i] != '.') digits++
                }
                return digits.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/**
 * Formats up to 4 digits ("0800") into "08.00" with accurate cursor mapping.
 */
class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(4)
        val formatted = when {
            raw.length <= 2 -> raw
            else -> "${raw.substring(0, 2)}.${raw.substring(2)}"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return if (clamped <= 2) clamped else (clamped + 1).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return if (clamped <= 2) clamped else (clamped - 1).coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

fun isValidRawTime(rawDigits: String): Boolean {
    val digits = rawDigits.filter(Char::isDigit)
    if (digits.length != 4) return false
    val hour = digits.substring(0, 2).toIntOrNull() ?: return false
    val minute = digits.substring(2, 4).toIntOrNull() ?: return false
    return hour in 0..23 && minute in 0..59
}

fun formatRawTimeToDisplay(rawDigits: String, fallback: String): String {
    val digits = rawDigits.filter(Char::isDigit)
    return if (digits.length == 4) {
        "${digits.substring(0, 2)}.${digits.substring(2, 4)}"
    } else {
        fallback
    }
}
