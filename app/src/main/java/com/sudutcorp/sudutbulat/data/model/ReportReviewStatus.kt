package com.sudutcorp.sudutbulat.data.model

enum class ReportReviewStatus(val label: String) {
    PENDING("Menunggu"),
    APPROVED("Disetujui"),
    REJECTED("Ditolak");

    companion object {
        fun fromString(raw: String?): ReportReviewStatus {
            val clean = raw?.trim()?.uppercase() ?: return PENDING
            return entries.firstOrNull { it.name == clean } ?: PENDING
        }
    }
}
