package com.sudutcorp.sudutbulat.core.util

import java.text.NumberFormat
import java.util.Locale

private val ID_LOCALE: Locale = Locale.forLanguageTag("id-ID")

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(ID_LOCALE)
    return "Rp${formatter.format(amount)}"
}

fun formatShortRupiah(amount: Long): String {
    return when {
        amount >= 1_000_000L -> {
            val millions = amount / 100_000L / 10.0
            "Rp${String.format(ID_LOCALE, "%.1f", millions)}jt"
        }
        amount >= 1_000L -> "Rp${amount / 1_000L}rb"
        else -> formatRupiah(amount)
    }
}

fun formatDecimal(value: Int): String = NumberFormat.getNumberInstance(ID_LOCALE).format(value)

fun buildInitials(name: String): String {
    val cleanName = name.replace(Regex("\\s*\\(.*?\\)"), "").trim()
    val parts = cleanName.split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase(Locale.ROOT)
        parts.size == 1 -> parts[0].first().uppercase(Locale.ROOT)
        else -> "?"
    }
}
