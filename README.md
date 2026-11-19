# SudutBulat (Internal Management App)

SudutBulat adalah aplikasi Android internal untuk mengelola operasional, distribusi bahan mentah, dan pelaporan harian jaringan bisnis UMKM **Tahu Bulat** dengan konsep outlet gerobakan menetap (statis).

Untuk spesifikasi lengkap, batasan domain, dan kontrak skema database, lihat [PRD.md](PRD.md).

## Konsep Utama Bisnis

- **Outlet Menetap:** Setiap gerobak berada di titik lokasi tetap (bukan keliling).
- **Stok vs Produk:** **Stok (`Material`)** adalah bahan mentah yang disuplai gudang (tahu mentah `pcs`, saus sambal `pcs`, chilly oil `botol`, dll), sedangkan **Produk (`Product`)** adalah barang matang siap jual yang memiliki harga jual (satuan atau paket/bungkus).
- **Cakupan Pencatatan:** Gudang tidak mencatat stok masuk dari *supplier* maupun sisa kuantitas stok di gudang; gudang hanya mengelola katalog bahan mentah dan mencatat pendistribusian ke outlet. Metode pengantaran fisik (Gojek, kurir, jemput sendiri) tidak dilacak di aplikasi.

## Role-Based Access Control (RBAC)

### 1. Owner (Superadmin & Monitoring)
- Memantau dashboard performa, omzet (Cash & QRIS), pengeluaran outlet, dan laporan bahan rusak dari seluruh outlet.
- Mengelola data master **Outlet** (CRUD) dan akun **User** (CRUD akun Admin Gudang dan Staf Outlet serta penugasan outlet).
- Berhak mengelola katalog bahan mentah dan produk.

### 2. Admin Gudang (1 Gudang, >= 2 Akun Pegawai)
- Melakukan distribusi rutin pagi hari ke seluruh outlet aktif, dilengkapi indikator outlet mana saja yang belum menerima distribusi pagi.
- Meninjau dan memutuskan (Terima / Tolak) permintaan (*request*) tambahan stok dari Staf Outlet.
- Mengelola **Daftar Katalog Bahan Mentah (`Material`)** dan **Produk (`Product`)**.

### 3. Staf Outlet (1 Akun = 1 Outlet Tetap)
- Mengonfirmasi penerimaan distribusi stok dari gudang (`RECEIVED`, `PARTIALLY_RECEIVED`, atau `NOT_RECEIVED`).
- Mengajukan permintaan tambahan stok ke gudang saat bahan menipis.
- Mengisi **Laporan Harian (Closing)** saat outlet tutup:
  - Penjualan produk siap jual (total terjual, pendapatan Cash & QRIS).
  - Pengeluaran operasional mandiri outlet (gas LPG, plastik kantong/pembungkus, lidi tusuk, minyak goreng, saus sambal umum, dll).
  - Bahan mentah rusak/basi (`damaged_materials`).

### 4. Developer (`DEV` - Khusus Demo/Testing)
- Mendukung *role switcher* untuk berpindah tampilan antar role (`OWNER`, `WAREHOUSE`, `OUTLET`) saat pengujian.

## Alur Kerja Operasional

1. **Setup Master Data:** Owner menyiapkan data outlet dan akun pegawai; Gudang/Owner menyiapkan katalog bahan mentah dan produk.
2. **Distribusi Pagi:** Admin Gudang mengirim distribusi rutin pagi ke seluruh outlet yang belum terdistribusi hari itu.
3. **Operasional & Request Tambahan:** Staf Outlet mengonfirmasi penerimaan stok dan dapat mengirim request tambahan stok jika dibutuhkan (untuk disetujui/ditolak oleh Gudang).
4. **Closing Harian:** Saat tutup toko, Staf Outlet mengirimkan laporan harian (penjualan, pengeluaran mandiri, dan bahan rusak) tanpa perlu menghitung sisa stok aktual secara otomatis.
5. **Monitoring:** Owner meninjau laporan dan performa tiap outlet secara *real-time*.

## Tech Stack & Architecture

- **Platform:** Android (`minSdk = 29`, `targetSdk = 37`, `compileSdk = 37`)
- **Language:** Kotlin (`2.4.21`)
- **UI Toolkit:** Jetpack Compose (`compose-bom:2026.02.01`) & Material 3
- **Architecture:** MVI / Unidirectional Data Flow (UDF) + Repository Pattern
- **Navigation:** Type-safe Navigation Compose (`2.10.0`)
- **Async:** Kotlin Coroutines & Flow
- **Dependency Injection:** Manual DI melalui `AppContainer` (`DefaultAppContainer`)
- **Backend / BaaS:** Firebase Authentication & Cloud Firestore (`firebase-bom:35.0.0`)
