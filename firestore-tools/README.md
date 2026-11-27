# SudutBulat — Firestore Tools (Seeder & Migration CLI)

Tools berbasis Node.js (`firebase-admin`) untuk memberikan pengalaman **Seeder** dan **Migration** ala Laravel / Prisma pada database Cloud Firestore aplikasi **SudutBulat**.

## 1. Persiapan & Konfigurasi (`.env`)

1. Masuk ke folder `firestore-tools` dan install dependencies:
   ```bash
   cd firestore-tools
   npm install
   ```
2. Salin `.env.example` menjadi `.env` di **root project** (`SudutBulat/.env`):
   ```bash
   cp ../.env.example ../.env
   ```
3. Isi kredensial Service Account Firebase di file `.env` root project (pilih salah satu):
   - **Cara 1 (File JSON):** Unduh file JSON Service Account dari *Firebase Console -> Project Settings -> Service accounts -> Generate new private key*, simpan sebagai `serviceAccountKey.json` di root project (`SudutBulat/serviceAccountKey.json`).
   - **Cara 2 (Inline `.env`):** Isi variabel `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, dan `FIREBASE_PRIVATE_KEY` di `.env`.

---

## 2. Daftar Command CLI

Jalankan seluruh command di bawah ini dari dalam folder `firestore-tools/`:

### Generator (Membuat File Baru)
- **Buat Seeder Baru:**
  ```bash
  npm run make:seed <nama_seeder>
  # Contoh: npm run make:seed promos -> membuat seed/promos.seed.js
  ```
- **Buat Migration Baru:**
  ```bash
  npm run make:migrations <nama_migrasi>
  # Contoh: npm run make:migrations add_note_to_products -> membuat migrations/003_add_note_to_products.js
  ```

### Seeder (Mengisi Data Awal Sesuai `PRD.md`)
- **Jalankan Semua Default Seeder:**
  ```bash
  npm run seed
  ```
- **Jalankan 1 Seeder Spesifik:**
  ```bash
  npm run seed materials
  npm run seed users
  ```

### Migration (Menjalankan Perubahan Skema)
- **Jalankan Migrasi Baru (Pending):**
  ```bash
  npm run migrate
  ```
- **Lihat Status Migrasi:**
  ```bash
  npm run migrate:status
  ```
- **Rollback Batch Migrasi Terakhir:**
  ```bash
  npm run migrate:rollback
  ```
- **Reset Seluruh Koleksi + Migrate (+ Seed):**
  ```bash
  npm run migrate:fresh
  npm run migrate:fresh -- --seed
  ```

### Generator Data Harian Acak (Otomatis Tanggal Hari Ini)
Mengenerate data operasional harian acak (**distribusi rutin**, **request tambahan stok**, dan **laporan harian**) berdasarkan master data aktif yang sudah ada di Firestore — **tidak pernah** membuat akun, outlet, material, atau produk baru.
```bash
npm run generate:daily                         # Semua fitur harian (hari ini)
npm run generate:daily -- routine              # Distribusi rutin hari ini saja
npm run generate:daily -- requests --count=3   # 3 request tambahan stok hari ini
npm run generate:daily -- distributions        # Rutin + request tambahan hari ini
npm run generate:daily -- reports --overwrite  # Laporan harian hari ini (timpa jika sudah ada)
```

---

## 3. Data Default Seeder (Sesuai `PRD.md`)

1. **`outlets.seed.js`**: 4 Outlet Gerobakan Menetap (`Sudirman`, `Gajah Mada`, `Cikini`, `Tebet`).
2. **`users.seed.js`**:
   - `dev` (`sdb.dev@gmail.com`) — Role Developer (Role Switcher)
   - `owner` (`sdb.owner@gmail.com`) — Role Owner
   - `gudang` & `gudang2` (`sdb.gudang@gmail.com`, `sdb.gudang2@gmail.com`) — 2 Pegawai untuk 1 Gudang
   - `staff` & `staff2` — 2 Staf di Outlet Sudirman
   - `staff_gm`, `staff_cikini`, `staff_tebet` — Staf di masing-masing outlet lainnya
   *(Password default Firebase Auth: `password`)*
3. **`materials.seed.js`**: Katalog bahan mentah/stok (`Tahu Bulat Original pcs`, `Tahu Bulat Pedas pcs`, `Tahu Bulat Keju pcs`, `Saus Sambal pcs`, `Chilly Oil botol/unit`, `Bubuk Bumbu gr`).
4. **`products.seed.js`**: Katalog produk matang siap jual (Satuan & Bungkus 5 pcs).
5. **`distributions.seed.js`**: Distribusi rutin pagi ke Sudirman (`RECEIVED`) dan Gajah Mada (`SENT`), request tambahan stok dari Gajah Mada (`DRAFT`), sedangkan Cikini & Tebet sengaja belum diberi distribusi pagi untuk kebutuhan tampilan indikator UI Gudang.
6. **`daily_reports.seed.js`**: Contoh laporan tutup toko harian lengkap dengan rincian produk terjual (Cash & QRIS), pengeluaran mandiri outlet (Gas LPG, plastik, minyak goreng, lidi tusuk, saus umum), dan bahan rusak.
