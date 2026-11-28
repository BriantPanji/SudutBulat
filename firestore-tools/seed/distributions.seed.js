const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

/**
 * Sesuai PRD.md:
 * 1. Distribusi Rutin Pagi Hari Ini (`type: "ROUTINE"`):
 *    - `dist_morning_sudirman` -> RECEIVED
 *    - `dist_morning_gajah_mada` -> SENT
 *    - `outlet_cikini` (Dr. Mansyur) & `outlet_tebet` (Ringroad Setia Budi) sengaja BELUM dikirim hari ini
 *      agar tab Rutin menampilkan indikator & daftar outlet yang belum dikirim hari ini.
 * 2. Distribusi Tambahan / Permintaan Hari Ini (`type: "ADDITIONAL"`):
 *    - `dist_request_gajah_mada` -> DRAFT (Pending persetujuan Admin Gudang)
 *    - `dist_extra_sudirman_today` -> SENT (tambahan stok yang sudah dikirim siang ini)
 *    - `dist_request_tebet_cancelled` -> CANCELLED (permintaan dibatalkan/ditolak)
 * 3. Riwayat Distribusi (1, 3, dan 7 hari lalu) mencakup ROUTINE & ADDITIONAL lintas status
 *    (RECEIVED, PARTIALLY_RECEIVED, NOT_RECEIVED, CANCELLED) untuk pengujian tab Riwayat & DatePicker.
 */
function buildDistributions(admin) {
  const Timestamp = admin.firestore.Timestamp;
  const now = new Date();
  const at = (daysAgo, hour, minute) =>
    Timestamp.fromDate(new Date(now.getFullYear(), now.getMonth(), now.getDate() - daysAgo, hour, minute, 0));

  const distributions = [
    // --- Hari Ini: Rutin ---
    {
      id: "dist_morning_sudirman",
      outlet_id: "outlet_sudirman",
      created_by: "gudang",
      confirmed_by: "staff",
      status: "RECEIVED",
      type: "ROUTINE",
      delivery_method: "WAREHOUSE_COURIER",
      note: "Distribusi rutin pagi dari Gudang Pusat Medan Petisah ke Outlet Sudirman",
      confirmation_note: "Stok diterima lengkap sebelum buka gerobak, tahu masih segar",
      created_at: at(0, 6, 30),
      sent_at: at(0, 6, 45),
      arrived_at: at(0, 7, 20),
      confirmed_at: at(0, 7, 30),
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 550, quantity_received: 550, note: null },
        { material_id: "mat_tahu_pedas", quantity_sent: 320, quantity_received: 320, note: null },
        { material_id: "mat_tahu_keju", quantity_sent: 200, quantity_received: 200, note: null },
        { material_id: "mat_saus_sambal", quantity_sent: 50, quantity_received: 50, note: null },
        { material_id: "mat_chilly_oil", quantity_sent: 5, quantity_received: 5, note: "5 botol chilly oil" },
      ],
    },
    {
      id: "dist_morning_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "gudang2",
      confirmed_by: null,
      status: "SENT",
      type: "ROUTINE",
      delivery_method: "WAREHOUSE_COURIER",
      note: "Distribusi rutin pagi via kurir motor dari Gudang Medan Petisah",
      confirmation_note: null,
      created_at: at(0, 6, 35),
      sent_at: at(0, 6, 55),
      arrived_at: null,
      confirmed_at: null,
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 420, quantity_received: null, note: null },
        { material_id: "mat_tahu_pedas", quantity_sent: 260, quantity_received: null, note: null },
        { material_id: "mat_saus_sambal", quantity_sent: 40, quantity_received: null, note: null },
      ],
    },

    // --- Hari Ini: Tambahan / Permintaan ---
    {
      id: "dist_request_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "staff_gm",
      confirmed_by: null,
      status: "DRAFT",
      type: "ADDITIONAL",
      delivery_method: "WAREHOUSE_COURIER",
      note: "Minta tambahan stok siang, pesanan area Gajah Mada & Petisah ramai",
      confirmation_note: null,
      created_at: at(0, 11, 20),
      sent_at: null,
      arrived_at: null,
      confirmed_at: null,
      items: [
        { material_id: "mat_tahu_pedas", quantity_sent: 150, quantity_received: null, note: "Stok tahu pedas menipis jelang jam makan siang" },
        { material_id: "mat_chilly_oil", quantity_sent: 3, quantity_received: null, note: "Minta tambah 3 botol chilly oil" },
      ],
    },
    {
      id: "dist_extra_sudirman_today",
      outlet_id: "outlet_sudirman",
      created_by: "gudang",
      confirmed_by: null,
      status: "ARRIVED",
      type: "ADDITIONAL",
      delivery_method: "GOJEK",
      note: "Tambahan stok sore via Ojol (Gojek) untuk antisipasi pesanan grup kantoran",
      confirmation_note: null,
      created_at: at(0, 13, 10),
      sent_at: at(0, 13, 25),
      arrived_at: at(0, 13, 45),
      confirmed_at: null,
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 180, quantity_received: null, note: null },
        { material_id: "mat_bumbu_balado", quantity_sent: 250, quantity_received: null, note: null },
      ],
    },
    {
      id: "dist_request_tebet_cancelled",
      outlet_id: "outlet_tebet",
      created_by: "staff_tebet",
      confirmed_by: "gudang2",
      status: "CANCELLED",
      type: "ADDITIONAL",
      delivery_method: "WAREHOUSE_COURIER",
      note: "Permintaan tambahan tahu keju sore hari",
      confirmation_note: "Stok tahu keju di gudang habis, dijadwalkan besok pagi",
      created_at: at(0, 10, 15),
      sent_at: null,
      arrived_at: null,
      confirmed_at: at(0, 10, 40),
      items: [
        { material_id: "mat_tahu_keju", quantity_sent: 120, quantity_received: null, note: null },
      ],
    },

    // --- Riwayat Kemarin (1 hari lalu) ---
    {
      id: "dist_yday_sudirman",
      outlet_id: "outlet_sudirman",
      created_by: "gudang",
      confirmed_by: "staff2",
      status: "RECEIVED",
      type: "ROUTINE",
      delivery_method: "WAREHOUSE_COURIER",
      note: "Distribusi rutin pagi ke Jl. Jenderal Sudirman",
      confirmation_note: "Jumlah sesuai surat jalan",
      created_at: at(1, 6, 25),
      sent_at: at(1, 6, 40),
      arrived_at: at(1, 7, 15),
      confirmed_at: at(1, 7, 25),
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 500, quantity_received: 500, note: null },
        { material_id: "mat_tahu_pedas", quantity_sent: 300, quantity_received: 300, note: null },
        { material_id: "mat_bumbu_balado", quantity_sent: 500, quantity_received: 500, note: "500 gr bubuk balado" },
      ],
    },
    {
      id: "dist_yday_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "gudang2",
      confirmed_by: "staff_gm",
      status: "RECEIVED",
      type: "ROUTINE",
      delivery_method: "SELF_PICKUP",
      note: "Distribusi rutin pagi ke Jl. Gajah Mada",
      confirmation_note: "Diterima lengkap",
      created_at: at(1, 6, 30),
      sent_at: at(1, 6, 50),
      arrived_at: at(1, 7, 25),
      confirmed_at: at(1, 7, 35),
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 400, quantity_received: 400, note: null },
        { material_id: "mat_tahu_pedas", quantity_sent: 240, quantity_received: 240, note: null },
      ],
    },
    {
      id: "dist_yday_cikini",
      outlet_id: "outlet_cikini",
      created_by: "gudang",
      confirmed_by: "staff_cikini",
      status: "PARTIALLY_RECEIVED",
      type: "ROUTINE",
      delivery_method: "GRAB",
      note: "Distribusi rutin pagi ke Outlet Dr. Mansyur (area USU)",
      confirmation_note: "Tahu original kurang 10 pcs karena wadah terguncang di jalan rusak Padang Bulan",
      created_at: at(1, 6, 40),
      sent_at: at(1, 7, 0),
      arrived_at: at(1, 7, 40),
      confirmed_at: at(1, 7, 50),
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 480, quantity_received: 470, note: "10 pcs pecah di perjalanan" },
        { material_id: "mat_tahu_pedas", quantity_sent: 320, quantity_received: 320, note: null },
        { material_id: "mat_tahu_keju", quantity_sent: 180, quantity_received: 180, note: null },
      ],
    },
    {
      id: "dist_yday_tebet",
      outlet_id: "outlet_tebet",
      created_by: "gudang2",
      confirmed_by: "staff_tebet",
      status: "RECEIVED",
      type: "ROUTINE",
      delivery_method: "WAREHOUSE_COURIER",
      note: "Distribusi rutin pagi ke Outlet Ringroad Setia Budi",
      confirmation_note: "Semua bahan mentah diterima dalam kondisi baik",
      created_at: at(1, 6, 45),
      sent_at: at(1, 7, 10),
      arrived_at: at(1, 7, 55),
      confirmed_at: at(1, 8, 5),
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 380, quantity_received: 380, note: null },
        { material_id: "mat_tahu_pedas", quantity_sent: 220, quantity_received: 220, note: null },
        { material_id: "mat_bumbu_bbq", quantity_sent: 400, quantity_received: 400, note: null },
      ],
    },

    // --- Riwayat 3 & 7 Hari Lalu ---
    {
      id: "dist_3d_cikini_additional",
      outlet_id: "outlet_cikini",
      created_by: "staff_cikini",
      confirmed_by: "staff_cikini",
      status: "RECEIVED",
      type: "ADDITIONAL",
      delivery_method: "GOJEK",
      note: "Tambahan stok sore acara mahasiswa USU",
      confirmation_note: "Tambahan stok diterima lengkap dan cepat",
      created_at: at(3, 14, 15),
      sent_at: at(3, 14, 35),
      arrived_at: at(3, 15, 0),
      confirmed_at: at(3, 15, 10),
      items: [
        { material_id: "mat_tahu_original", quantity_sent: 250, quantity_received: 250, note: null },
        { material_id: "mat_saus_sambal", quantity_sent: 30, quantity_received: 30, note: null },
      ],
    },
    {
      id: "dist_7d_tebet_not_received",
      outlet_id: "outlet_tebet",
      created_by: "gudang",
      confirmed_by: "staff_tebet",
      status: "NOT_RECEIVED",
      type: "ADDITIONAL",
      delivery_method: "MAXIM",
      note: "Pengiriman susulan malam hari",
      confirmation_note: "Gerobak sudah tutup karena hujan deras, barang kembali ke gudang",
      created_at: at(7, 18, 20),
      sent_at: at(7, 18, 35),
      arrived_at: at(7, 19, 5),
      confirmed_at: at(7, 19, 15),
      items: [
        { material_id: "mat_tahu_pedas", quantity_sent: 100, quantity_received: 0, note: null },
      ],
    },
  ];

  return distributions.map((d) => ({ receipt_photo_url: null, ...d }));
}

async function seed(db, admin) {
  const distributions = buildDistributions(admin);
  const count = await batchUpsert(db, COLLECTIONS.MATERIAL_DISTRIBUTIONS, distributions);
  console.log(
    `  [OK] ${COLLECTIONS.MATERIAL_DISTRIBUTIONS}: ${count} dokumen distribusi (beserta embedded items & type) berhasil di-seed.`
  );
  return count;
}

module.exports = {
  name: "distributions",
  collection: COLLECTIONS.MATERIAL_DISTRIBUTIONS,
  buildDistributions,
  seed,
};
