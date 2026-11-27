# Product Requirements Document (PRD) — SudutBulat

> **IMPORTANT FOR AI AGENTS & DEVELOPERS:**
> Dokumen ini adalah **Single Source of Truth (SSOT)** untuk aplikasi **SudutBulat**. Jika terdapat perbedaan antara dokumen ini dengan komentar lama, `README.md`, atau *mock* UI statis di dalam codebase, **selalu ikuti aturan di dalam PRD ini**. Jangan mengarang fitur (*hallucinate*) di luar cakupan dokumen ini.

---

## 1. Gambaran Umum Proyek (Project Overview)

- **Nama Aplikasi:** SudutBulat (`com.sudutcorp.sudutbulat`)
- **Platform:** Android Mobile (Kotlin + Jetpack Compose)
- **Konteks Proyek:** Tugas Besar Pemrograman Mobile — Aplikasi manajemen operasional internal untuk UMKM kuliner **Tahu Bulat**.
- **Model Bisnis:**
  - Skema bisnis berupa **Outlet Gerobakan Menetap (Statis)** yang tersebar di beberapa titik lokasi. Gerobak tidak berkeliling; bentuk gerobak hanya untuk identitas visual/gaya.
  - Terdapat **1 Gudang Pusat (Warehouse)** yang menyuplai bahan mentah (**Stok**) ke seluruh outlet.
  - Setiap **Outlet** dijaga oleh satu atau lebih **Staf Outlet** yang bertugas menggoreng/menjual produk dan membuat laporan harian.

---

## 2. Batasan Domain Tegas (Strict Anti-Hallucination Guardrails)

AI **DILARANG** membuat asumsi atau menambahkan fitur berikut:

1. **Perbedaan Tegas antara STOK (`Material`) dan PRODUK (`Product`):**
   - **Stok (`Material`):** Barang/bahan mentah yang dikirim dari Gudang ke Outlet. Tidak dijual langsung ke konsumen dalam bentuk mentah, memiliki satuan dasar (`pcs`, `gr`, `ml`, `unit`/botol/bungkus) dan nilai modal (`cost`).
     - *Contoh Stok:* Tahu Bulat Original (pcs), Tahu Bulat Pedas (pcs), Tahu Bulat Keju (pcs), Saus Sambal (pcs), Chilly Oil (botol), Bubuk Bumbu.
   - **Produk (`Product`):** Barang matang siap jual ke pelanggan di outlet yang memiliki **Harga Jual (`price`)**.
     - *Contoh Produk:* Tahu Bulat Original (satuan atau paket 5 pcs/bungkus), Tahu Bulat Pedas (satuan atau paket 5 pcs/bungkus), dll.
2. **Gudang TIDAK Mencatat Jumlah Stok Gudang:**
   - Gudang **tidak** memiliki fitur pencatatan stok masuk dari *supplier* dan **tidak** melacak sisa kuantitas barang di dalam gudang.
   - Gudang hanya mengurusi **katalog master jenis bahan mentah (`Material`)**, **pendistribusian stok ke setiap outlet**, dan **persetujuan (Terima/Tolak) permintaan tambahan stok dari outlet**.
3. **Proses Pengantaran Fisik Tidak Relevan:**
   - Pengiriman stok dari gudang ke outlet bisa menggunakan Gojek, kurir gudang, dijemput staf outlet, dll. Aplikasi **tidak** melacak kurir, ongkos kirim, peta perjalanan/GPS tracking pengiriman, atau metode pengantaran. Yang dicatat hanya **status distribusi dan kuantitas barang**.
4. **Outlet TIDAK Menghitung Sisa Stok Aktual Secara Otomatis:**
   - Sistem tidak perlu melakukan kalkulasi otomatis `Stok Awal - Terjual - Rusak = Sisa Stok`.
   - Staf Outlet cukup melakukan konfirmasi penerimaan barang, meminta tambahan stok jika kurang, dan pada akhir hari menginput angka **Laporan Harian (Closing)** secara langsung.

---

## 3. Role-Based Access Control (RBAC) & Alur Kerja

Aplikasi memiliki **3 Role Bisnis Utama** ditambah **1 Role Developer (`DEV`)** untuk keperluan pengujian/demo:

### 3.1. Owner (`Role.OWNER`) — Superadmin & Monitoring
Owner adalah pemilik bisnis yang memiliki akses tertinggi, dengan fokus utama pada **pemantauan laporan** dan **manajemen master entitas (Outlet & Akun User)**.
- **Dashboard & Laporan (`HomeScreen`, `OwnerReportScreen`, `OwnerDetailReportScreen`):**
  - Melihat ringkasan omzet/pendapatan (Cash & QRIS), total produk terjual, pengeluaran operasional outlet, dan kerugian bahan rusak dari seluruh outlet.
  - Memantau daftar laporan harian per outlet dan meninjau laporan yang memiliki catatan tidak biasa (stok rusak tinggi / pengeluaran ekstra).
- **Manajemen Outlet — CRUD (`OutletScreen`, `OwnerDetailOutletScreen`):**
  - Menambah, melihat detail, mengubah, dan menonaktifkan Outlet (Nama Outlet, Alamat, Koordinat statis, Status Aktif/Buka).
- **Manajemen Akun/User — CRUD:**
  - Membuat, mengubah, dan menonaktifkan akun untuk **Admin Gudang** dan **Staf Outlet**.
  - Menugaskan (*assign*) akun Staf Outlet ke 1 `outlet_id` tertentu.
- **Akses Katalog (`Material` & `Product`):**
  - Berhak mengelola katalog bahan mentah dan produk, namun di antarmuka sehari-hari pengelolaan katalog bahan mentah diutamakan pada role Gudang.

### 3.2. Admin Gudang (`Role.WAREHOUSE`) — Distribusi & Katalog Bahan
Meskipun secara fisik hanya ada **1 Gudang**, terdapat **lebih dari 1 pegawai gudang (minimal 2 akun)**.
- **Distribusi Rutin Pagi Hari (`WarehouseHomeScreen`, `DistributionScreen`):**
  - Setiap pagi, gudang wajib mendistribusikan stok ke seluruh outlet aktif tanpa perlu diminta oleh outlet.
  - UI menampilkan indikator jelas **daftar outlet mana saja yang BELUM diberikan distribusi rutin pagi hari ini**, sehingga Admin Gudang dapat langsung membuat/mengirim distribusi pagi ke outlet-outlet tersebut.
- **Penanganan Request Tambahan Stok dari Outlet:**
  - Melihat permintaan (*request*) tambahan stok yang diajukan oleh Staf Outlet.
  - **Menerima (Approve/Kirim)** atau **Menolak (Reject)** request tambahan stok tersebut.
- **Katalog Bahan Mentah (`GudangStockScreen`):**
  - Tab **"Stok"** pada akun Gudang berfungsi sebagai **Daftar Katalog Bahan Mentah (`materials`)** (beserta pengelolaan/CRUD jenis bahan mentah dan produk), **bukan** jumlah sisa stok di gudang.

### 3.3. Staf Outlet (`Role.OUTLET`) — Operasional & Laporan Harian
Setiap akun Staf Outlet hanya bertugas pada **1 Outlet tetap (`outlet_id`)**, namun **1 Outlet bisa memiliki beberapa akun Staf**.
- **Penerimaan & Permintaan Stok (`OutletHomeScreen`, `OutletStockScreen`):**
  - Melihat daftar pengiriman stok dari Gudang dan mengonfirmasi penerimaan barang (`RECEIVED`, `PARTIALLY_RECEIVED`, atau `NOT_RECEIVED` beserta catatan jika jumlah berbeda).
  - Mengajukan **Request Tambahan Stok** ke Gudang apabila stok di gerobak menipis di tengah hari.
- **Laporan Harian / Closing Tutup Toko (`OutletReportScreen`):**
  - Setiap akhir hari (saat outlet tutup), staf wajib mengirimkan 1 laporan harian untuk outletnya (`(outlet_id, date)` unik) yang mencakup:
    1. **Penjualan Produk (`daily_report_products` & `daily_reports`):** Rincian produk siap jual yang laku (satuan/bungkus), total item terjual (`total_sold`), pendapatan tunai (`cash_revenue`), dan pendapatan non-tunai (`qris_revenue`).
    2. **Pengeluaran Operasional Outlet (`daily_report_expenses`):** Pencatatan biaya yang dibeli mandiri oleh outlet (seperti lidi tusuk, plastik pembungkus, plastik kantong, gas LPG, minyak goreng, atau saus sambal umum non-resep khusus) beserta nominal (`amount`) dan catatan.
    3. **Kerugian / Bahan Rusak (`daily_report_damaged_materials`):** Pencatatan bahan mentah (`material_id`) yang rusak, basi, atau gagal produksi beserta jumlah (`quantity`) dan alasannya.

### 3.4. Developer (`Role.DEV`) — Role Switcher (Khusus Demo/Testing)
- Akun khusus (`sdb.dev@gmail.com` / kode `dev`) yang dapat berpindah tampilan (*workspace*) antara `OWNER`, `WAREHOUSE`, dan `OUTLET` secara instan melalui `WarehouseRoleBottomSheet` dan `AIFab`. Fitur ini harus tetap dipertahankan untuk kemudahan demo tugas besar.

---

## 4. Kontrak Skema Database (Firestore Schema Contract)

Sumber acuan: [`basedb.dbdiagram`](file:///c:/Users/FASILKOMTIUSU/panjay/SudutBulat/app/src/main/java/com/sudutcorp/sudutbulat/core/util/basedb.dbdiagram) dan [`FirestoreCollections.kt`](file:///c:/Users/FASILKOMTIUSU/panjay/SudutBulat/app/src/main/java/com/sudutcorp/sudutbulat/data/remote/FirestoreCollections.kt).

| Nama Koleksi | Model Kotlin | Deskripsi & Aturan Penting |
| :--- | :--- | :--- |
| `users` | `User` | `id` (PK/loginCode), `uid` (opsional, Firebase Auth UID), `name`, `email`, `username`, `role` (`DEV`, `OWNER`, `WAREHOUSE`, `OUTLET`), `outlet_id` (nullable, wajib terisi untuk `OUTLET`), `is_active`. |
| `outlets` | `Outlet` | `id`, `name`, `address`, `coordinate` (`GeoPoint`), `is_active`, `is_open`, `open_time`, `close_time`, `daily_target`, `routine_preset` (array `{ material_id, quantity }` untuk auto-fill distribusi rutin per outlet, diatur oleh Owner/Gudang), serta cache ringkasan harian (`total_revenue`, `total_pieces_sold`, `target_percentage`). |
| `materials` | `Material` | Katalog bahan mentah/stok: `id`, `name` (unik), `cost` (Long $\ge 0$), `unit` (`PCS`, `GR`, `ML`, `UNIT`), `is_active`. **Tidak menyimpan stok gudang.** |
| `products` | `Product` | Katalog barang siap jual: `id`, `name` (unik), `price` (Long $\ge 0$), `sale_type` (`satuan`, `bungkus`), `pcs_per_pack` (Int $\ge 1$), `is_active`. |
| `material_distributions` | `MaterialDistribution` | Dokumen distribusi/request stok ke outlet: `id`, `outlet_id`, `created_by`, `confirmed_by`, `status` (`DistributionStatus`), `type` (`DistributionType`: `ROUTINE`, `ADDITIONAL`), `note`, `confirmation_note`, `created_at`, `sent_at`, `confirmed_at`, serta `items` (array of map: `{ material_id, quantity_sent, quantity_received, note }`). |
| `daily_reports` | `DailyReport` | Header laporan tutup toko harian: `id`, `outlet_id`, `date` (`Timestamp`), `cash_revenue`, `qris_revenue`, `total_sold`. Unik per `(outlet_id, date)` per tanggal kalender; pembuatan laporan ganda di tanggal sama ditolak otomatis dan diarahkan ke konfirmasi edit/update. |
| `daily_report_products` | `DailyReportProduct` | 1 dokumen per `daily_report_id` (doc ID = `daily_report_id`) yang menyimpan array `items`: `[{ product_id, quantity, price }]`. |
| `daily_report_expenses` | `DailyReportExpense` | 1 dokumen per `daily_report_id` (doc ID = `daily_report_id`) yang menyimpan array `items`: `[{ category, amount, note }]`. |
| `daily_report_damaged_materials` | `DailyReportDamagedMaterial` | 1 dokumen per `daily_report_id` (doc ID = `daily_report_id`) yang menyimpan array `items`: `[{ material_id, quantity, note }]`. |

### Siklus Status Distribusi & Request Stok (`DistributionStatus`)
- **Distribusi Rutin Pagi (Inisiatif Gudang):**
  - `DRAFT` (opsional/dijadwalkan) $\rightarrow$ `SENT` (dikirim oleh Gudang) $\rightarrow$ dikonfirmasi oleh Staf Outlet menjadi `RECEIVED`, `PARTIALLY_RECEIVED`, atau `NOT_RECEIVED` (atau `CANCELLED` jika dibatalkan).
- **Request Tambahan Stok (Inisiatif Staf Outlet):**
  - Staf Outlet membuat permintaan stok (menggunakan status `DRAFT` / `REQUESTED` dengan `created_by` adalah user Outlet) $\rightarrow$ Gudang meninjau:
    - Jika **Diterima (Approved)**: status diubah menjadi `SENT` dan `sent_at` diisi.
    - Jika **Ditolak (Rejected)**: status diubah menjadi `CANCELLED` (dengan alasan di `confirmation_note` / `note`).

---

## 5. Arsitektur Teknis & Panduan Kode (AI Engineering Guidelines)

Bagian ini mengikat cara AI menulis dan memodifikasi kode di repositori ini:

### 5.1. Tech Stack Aktual
- **Konfigurasi SDK:** `minSdk = 29`, `targetSdk = 37`, `compileSdk = 37`, Java 11, Kotlin `2.4.21`, AGP `9.3.3`.
- **UI & Navigasi:** Jetpack Compose (`compose-bom:2026.02.01`), Material 3, Type-safe Navigation Compose (`2.10.0`) + `HorizontalPager` per role di `MainScreen`.
- **Backend & Async:** Firebase Auth, Cloud Firestore (`firebase-bom:35.0.0`), Kotlin Coroutines & Flow (`kotlinx-coroutines-play-services`).
- **Dependency Injection:** Manual DI melalui [`AppContainer.kt`](file:///c:/Users/FASILKOMTIUSU/panjay/SudutBulat/app/src/main/java/com/sudutcorp/sudutbulat/data/AppContainer.kt) (`DefaultAppContainer`) yang dipegang oleh `SudutBulatApplication`. **Jangan** menambahkan library Hilt/Dagger/Koin kecuali diminta secara eksplisit.

### 5.2. Prinsip Ponytail (`/ponytail`)
- **The Smallest Complete Change:** Tulis kode sesedikit dan sejelas mungkin yang menyelesaikan seluruh kebutuhan tugas.
- Gunakan komponen tema (`SBColor`, `SudutBulatTheme`, `PlusJakartaSans`), `AppContainer`, `RemoteDataSource`, dan `Repository` yang sudah ada di codebase.
- Jangan membuat *wrapper*, *abstraction layer*, atau konfigurasi spekulatif yang tidak diminta.

### 5.3. Arsitektur & State Management (`/android-kotlin-architecture`)
- Gunakan pola **MVI / Unidirectional Data Flow (UDF)** saat menghubungkan layar ke ViewModel:
  - `UiState` (`data class` dengan properti `val` immutable), `UiEvent` (`sealed interface`), dan `UiEffect` (`sealed interface` via `Channel` jika perlu navigasi/snackbar).
  - Mutasi state di dalam `ViewModel` **wajib** menggunakan `_state.update { it.copy(...) }`.
  - Composable menerima state dan lambda `onEvent: (UiEvent) -> Unit`, bukan instance `ViewModel` yang dioper ke sub-komponen.

### 5.4. Jetpack Compose, Performa, Navigasi & Adaptive (`/android-kotlin-compose`, `/jetpack-compose-performance`, `/navigation-event`, `/adaptive`)
- **State Preservation:** Gunakan `rememberSaveable` untuk semua input form/filter/tab dan gunakan `rememberScrollState()` / `rememberLazyListState()` agar state aman saat rotasi layar atau perubahan konfigurasi. Jangan mengunci orientasi layar di `AndroidManifest.xml`.
- **Lazy Layouts:** Untuk daftar yang dinamis dan panjang, gunakan `LazyColumn` dengan parameter `key = { it.id }` dan `contentType`. Untuk daftar kecil dan terbatas (misal 2–5 kartu statis di dalam halaman yang sudah `verticalScroll`), gunakan `Column` biasa (hindari nested `LazyColumn` di dalam `verticalScroll`).
- **Stability & Flow Collection:** Gunakan `collectAsStateWithLifecycle()` saat mengamati `StateFlow` dari ViewModel. Jangan menempelkan `@Immutable` atau `@Stable` pada model yang berisi `List`/`Set`/`Map` di bawah *Strong Skipping Mode*.
- **Back Navigation (`compileSdk = 37`):** Gunakan dispatcher bawaan `ComponentActivity` / `NavigationBackHandler` (hindari membuat custom `NavigationEventDispatcher` yang tidak terhubung).

---

## 6. Status Sinkronisasi Codebase Saat Ini vs PRD

Status implementasi aktual di repositori:

1. **Lapisan Data (`data/model`, `data/remote`, `data/repository`):**
   - Sudah diimplementasikan lengkap dengan validasi `require(...)`, mapper eksplisit (`FirestoreMappers.kt`), skema array tersemat (`items`), dan unit test di `RepositoryTest.kt` & `BackendPersistenceTest.kt`.
2. **Layar Owner (`HomeScreen.kt`, `OutletScreen.kt`, `OwnerReportScreen.kt`, `OwnerDetailOutletScreen.kt`, `OwnerDetailStaffScreen.kt`, `AddOutletScreen.kt`, `AddUserScreen.kt`):**
   - Sudah terhubung ke Firestore melalui `OwnerViewModel` (MVI), mencakup CRUD Outlet, CRUD Akun User/Staf, filter tanggal laporan, dan detail laporan harian. Akun baru dibuat dari Owner (tidak ada fitur self-register).
3. **Layar Gudang (`WarehouseHomeScreen.kt`, `DistributionScreen.kt`) & Katalog (`GudangStockScreen.kt`):**
   - `WarehouseHomeScreen` & `DistributionScreen` sudah terhubung ke `WarehouseViewModel` (progres distribusi pagi hari ini, kirim distribusi, dan Approve/Reject request stok dari outlet).
   - `GudangStockScreen` (Katalog `Material` & `Product`) dipisahkan ke `CatalogViewModel` tersendiri sehingga dapat dipakai bersama oleh Gudang maupun Owner tanpa menginstansiasi `OwnerViewModel` di role Gudang.
4. **Layar Staf Outlet (`OutletHomeScreen.kt`, `OutletStockScreen.kt`, `OutletReportScreen.kt`):**
   - Sudah terhubung ke Firestore melalui `OutletViewModel` (toggle status buka/tutup outlet, konfirmasi penerimaan distribusi, pengajuan request tambahan stok, dan form input/update Laporan Harian dengan penolakan otomatis duplikasi per tanggal).
