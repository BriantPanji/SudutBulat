# 🟡 SudutBulat (Internal Management App)

SudutBulat adalah aplikasi Android internal yang dirancang khusus untuk mengelola operasional, manajemen stok, dan pelaporan keuangan harian untuk jaringan bisnis Tahu Bulat berkonsep outlet gerobak menetap.

Aplikasi ini dibangun menggunakan **Kotlin** dan **Jetpack Compose** untuk memberikan pengalaman antarmuka yang modern, reaktif, dan mudah digunakan oleh seluruh tingkat staf.

---

## 👥 Role-Based Access Control (RBAC) & Fitur Utama

Sistem ini membagi hak akses ke dalam tiga role utama dengan alur kerja yang spesifik:

### 1. 👑 Owner (Pemilik)
Memiliki akses penuh untuk memantau seluruh aktivitas bisnis.
* **Dashboard Utama:** Melihat ringkasan performa seluruh outlet.
* **Laporan Komprehensif:** Akses rekapitulasi penjualan, pengeluaran operasional, dan penyusutan bahan (stok rusak) dari semua outlet.

### 2. 📦 Admin Gudang (Warehouse Admin)
Bertanggung jawab atas pusat logistik dan ketersediaan bahan baku.
* **Manajemen Stok Gudang:** Mencatat barang masuk dari *supplier*.
* **Distribusi Stok:** Membagi dan mencatat pengiriman bahan baku ke masing-masing outlet gerobak. Bahan yang didistribusikan meliputi:
    * Tahu bulat mentah
    * Saus
    * Bubuk topping / bumbu
* **Pemantauan Permintaan:** Memantau sisa stok di setiap outlet untuk perencanaan distribusi hari berikutnya.

### 3. 🏪 Staf Outlet
Bertugas di lapangan pada satu lokasi gerobak tetap (tidak ada rotasi/perpindahan lokasi outlet untuk staf).
* **Laporan Penjualan (Income):** Menginput total tahu bulat yang berhasil terjual pada shift/hari tersebut.
* **Laporan Pengeluaran (Expense):** Mencatat pengeluaran operasional harian gerobak (contoh: pembelian gas, plastik kantongan, tusuk lidi).
* **Laporan Penyusutan (Damaged Stock):** Melaporkan jumlah stok tahu mentah atau bahan lain yang rusak, basi, atau tidak layak jual di akhir jam operasional.

---

## 🏗️ Alur Kerja Sistem (Business Logic)

1. **Setup Outlet:** Setiap outlet (gerobak) berada di titik lokasi yang statis. Minimal 1 Staf Outlet di-assign secara permanen ke ID Outlet tersebut.
2. **Distribusi:** Admin Gudang mengirimkan bahan baku ke outlet dan mencatat kuantitasnya di aplikasi.
3. **Operasional:** Staf Outlet menerima stok (update status di sistem).
4. **Closing (Akhir Hari):** Staf Outlet wajib mengisi *form closing* harian yang terdiri dari: Tahu Terjual, Pengeluaran Ekstra, dan Stok Rusak.
5. **Sinkronisasi:** Data laporan langsung masuk ke database pusat dan dapat direview secara *real-time* oleh Owner.

---

## 🛠️ Tech Stack & Architecture

* **Platform:** Android (Min SDK 22 / Target SDK 31)
* **UI Toolkit:** Jetpack Compose
* **Language:** Kotlin
* **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture
* **Asynchronous Programming:** Kotlin Coroutines & Flow
* **Dependency Injection:** Hilt / Dagger (Opsional, sesuaikan jika pakai)
* **Network / API:** Retrofit & OkHttp
* **BaaS / Cloud Services:** Firebase (Authentication, Crashlytics, Cloud Messaging)
* **Local Storage:** Room Database / DataStore (untuk caching sesi login staf)