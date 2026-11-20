package com.sudutcorp.sudutbulat.feature.owner

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.model.User

fun defaultSeededOutlets(): List<Outlet> = listOf(
    Outlet(
        id = "outlet_sudirman", name = "Sudirman", address = "Jl. Jenderal Sudirman No. 18",
        isActive = true, isOpen = true, targetPercentage = 0.85f, totalRevenue = 2_480_000L,
        totalPiecesSold = 496, openTime = "09.00", closeTime = "21.00", dailyTarget = 2_900_000L
    ),
    Outlet(
        id = "outlet_gajah_mada", name = "Gajah Mada", address = "Jl. Gajah Mada No. 7",
        isActive = true, isOpen = true, targetPercentage = 0.86f, totalRevenue = 2_120_000L,
        totalPiecesSold = 424, openTime = "08.00", closeTime = "21.00", dailyTarget = 2_460_000L
    ),
    Outlet(
        id = "outlet_cikini", name = "Cikini", address = "Jl. Cikini Raya No. 33",
        isActive = true, isOpen = true, targetPercentage = 0.72f, totalRevenue = 1_780_000L,
        totalPiecesSold = 356, openTime = "08.00", closeTime = "21.00", dailyTarget = 2_470_000L
    ),
    Outlet(
        id = "outlet_kemang", name = "Kemang", address = "Jl. Kemang Raya No. 12",
        isActive = true, isOpen = true, targetPercentage = 0.65f, totalRevenue = 1_320_000L,
        totalPiecesSold = 264, openTime = "08.00", closeTime = "21.00", dailyTarget = 2_000_000L
    ),
    Outlet(
        id = "outlet_blok_m", name = "Blok M", address = "Jl. Melawai No. 5",
        isActive = true, isOpen = false, targetPercentage = 0.36f, totalRevenue = 720_000L,
        totalPiecesSold = 144, openTime = "09.00", closeTime = "21.00", dailyTarget = 2_000_000L
    )
)

fun defaultSeededUsers(): List<User> = listOf(
    User("owner", "Arif Rahman", "sdb.owner@gmail.com", "owner", Role.OWNER, null, true),
    User("gudang", "Budi Santoso", "sdb.gudang@gmail.com", "gudang", Role.WAREHOUSE, null, true),
    User("gudang2", "Riko Pratama", "sdb.gudang2@gmail.com", "gudang2", Role.WAREHOUSE, null, true),
    User("staff", "Dimas Pratama", "sdb.staff@gmail.com", "staff", Role.OUTLET, "outlet_sudirman", true),
    User("staff2", "Andi Saputra", "sdb.staff2@gmail.com", "staff2", Role.OUTLET, "outlet_sudirman", true),
    User("staff_gm", "Siti Rahma", "sdb.staff.gm@gmail.com", "staff_gm", Role.OUTLET, "outlet_gajah_mada", true),
    User("staff_gm2", "Rudi Hartono", "sdb.staff.gm2@gmail.com", "staff_gm2", Role.OUTLET, "outlet_gajah_mada", true),
    User("staff_cikini", "Fajar Nugroho", "sdb.staff.cikini@gmail.com", "staff_cikini", Role.OUTLET, "outlet_cikini", true),
    User("staff_kemang1", "Bagas Saputra", "sdb.staff.kemang1@gmail.com", "staff_kemang1", Role.OUTLET, "outlet_kemang", true),
    User("staff_kemang2", "Rina Wijaya", "sdb.staff.kemang2@gmail.com", "staff_kemang2", Role.OUTLET, "outlet_kemang", true),
    User("staff_blok_m", "Reza Mahendra", "sdb.staff.blokm@gmail.com", "staff_blok_m", Role.OUTLET, "outlet_blok_m", true)
)

fun defaultSeededReports(): List<DailyReport> {
    val nowSec = System.currentTimeMillis() / 1000L
    val daySec = 86_400L
    return listOf(
        DailyReport("report_sudirman_today", "outlet_sudirman", Timestamp(nowSec - 3600L, 0), 1_520_000L, 960_000L, 496),
        DailyReport("report_cikini_today", "outlet_cikini", Timestamp(nowSec - 5400L, 0), 1_020_000L, 760_000L, 356),
        DailyReport("report_sudirman_yesterday", "outlet_sudirman", Timestamp(nowSec - daySec, 0), 1_480_000L, 1_000_000L, 496),
        DailyReport("report_gajah_mada_yesterday", "outlet_gajah_mada", Timestamp(nowSec - daySec - 1800L, 0), 1_320_000L, 800_000L, 424),
        DailyReport("report_kemang_3d", "outlet_kemang", Timestamp(nowSec - (3 * daySec), 0), 780_000L, 540_000L, 264),
        DailyReport("report_gajah_mada_10d", "outlet_gajah_mada", Timestamp(nowSec - (10 * daySec), 0), 1_180_000L, 870_000L, 410)
    )
}

fun defaultSeededMaterials(): List<Material> = listOf(
    Material("mat_tahu_original", "Tahu Original", 500L, UnitType.PCS),
    Material("mat_tahu_pedas", "Tahu Pedas", 600L, UnitType.PCS),
    Material("mat_tahu_keju", "Tahu Keju", 700L, UnitType.PCS),
    Material("mat_saus_sambal", "Saus Sambal", 3_000L, UnitType.UNIT),
    Material("mat_chilly_oil", "Chilly Oil", 15_000L, UnitType.UNIT),
    Material("mat_bumbu_original", "Bumbu Original", 12_000L, UnitType.UNIT),
    Material("mat_bumbu_pedas", "Bumbu Pedas", 14_000L, UnitType.UNIT),
    Material("mat_bumbu_bbq", "Bumbu BBQ", 14_000L, UnitType.UNIT)
)

fun defaultSeededProducts(): List<Product> = listOf(
    Product("prod_tahu_ori_satuan", "Tahu Bulat Original (Satuan)", 1_000L, ProductSaleType.SATUAN, 1, true),
    Product("prod_tahu_ori_bungkus", "Tahu Bulat Original (Bungkus 5 pcs)", 5_000L, ProductSaleType.BUNGKUS, 5, true),
    Product("prod_tahu_pedas_satuan", "Tahu Bulat Pedas (Satuan)", 1_200L, ProductSaleType.SATUAN, 1, true),
    Product("prod_tahu_pedas_bungkus", "Tahu Bulat Pedas (Bungkus 5 pcs)", 6_000L, ProductSaleType.BUNGKUS, 5, true),
    Product("prod_tahu_keju_satuan", "Tahu Bulat Keju (Satuan)", 1_500L, ProductSaleType.SATUAN, 1, true),
    Product("prod_tahu_keju_bungkus", "Tahu Bulat Keju (Bungkus 5 pcs)", 7_000L, ProductSaleType.BUNGKUS, 5, true)
)

fun defaultNotifications(): List<OwnerNotificationUi> = listOf(
    OwnerNotificationUi(
        "notif_1", "Stok rusak — Outlet Cikini",
        "12 pcs Tahu Pedas dilaporkan rusak (kemasan sobek).", "5 menit lalu", true, "outlet_cikini"
    ),
    OwnerNotificationUi(
        "notif_2", "Pengeluaran tidak biasa",
        "Outlet Gajah Mada mencatat Rp420.000 untuk kategori Lainnya.", "32 menit lalu", true, "outlet_gajah_mada"
    ),
    OwnerNotificationUi(
        "notif_3", "Laporan harian diterima",
        "Outlet Sudirman mengirim laporan tutup kasir kemarin.", "Kemarin, 21.40", false, "outlet_sudirman"
    ),
    OwnerNotificationUi(
        "notif_4", "Distribusi pagi selesai",
        "Gudang pusat telah mengirim stok pagi ke Outlet Sudirman.", "Kemarin, 15.10", false, "outlet_sudirman"
    )
)
