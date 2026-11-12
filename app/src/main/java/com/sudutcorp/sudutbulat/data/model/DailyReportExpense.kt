package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for daily report expenses in Firestore collection `daily_report_expenses`.
 *
 * @property dailyReportId Optional reference to daily_reports.id. Can be null if it's a general expense.
 * @property amount Expense amount in currency (Rp), non-negative (>= 0).
 * @property note Optional notes (max 512 chars).
 */
data class DailyReportExpense(
    @DocumentId
    val id: String = "",

    @get:PropertyName("daily_report_id")
    @set:PropertyName("daily_report_id")
    var dailyReportId: String? = null,

    val category: ExpenseCategory = ExpenseCategory.OTHER,

    val amount: Long = 0L,

    val note: String? = null
) {
    init {
        require(amount >= 0L) { "Expense amount must not be negative: $amount" }
        require(note == null || note.length <= 512) { "Note must not exceed 512 characters" }
    }
}
