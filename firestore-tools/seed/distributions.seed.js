const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

/**
 * Sesuai PRD.md:
 * 1. Distribusi Rutin Pagi Hari Ini (dari Gudang Pusat Medan Petisah):
 *    - `dist_morning_sudirman` -> Sudah dikirim pagi ini dan dikonfirmasi RECEIVED oleh staf outlet Sudirman.
 *    - `dist_morning_gajah_mada` -> Sudah dikirim pagi ini (SENT), menunggu konfirmasi staf outlet Gajah Mada.
 *    - Catatan: `outlet_cikini` (Dr. Mansyur) dan `outlet_tebet` (Ringroad Setia Budi) sengaja BELUM diberikan
 *      distribusi pagi hari ini agar UI Gudang dapat menampilkan outlet yang belum menerima distribusi rutin pagi.
 * 2. Request Tambahan Stok dari Outlet:
 *    - `dist_request_gajah_mada` -> Dibuat oleh `staff_gm` (status DRAFT) meminta tambahan Tahu Pedas & Chilly Oil.
 * 3. Riwayat Distribusi Kemarin (RECEIVED & PARTIALLY_RECEIVED) agar riwayat distribusi realistis.
 */
function buildDistributions(admin) {
  const Timestamp = admin.firestore.Timestamp;
  const now = new Date();
  const at = (daysAgo, hour, minute) =>
    Timestamp.fromDate(new Date(now.getFullYear(), now.getMonth(), now.getDate() - daysAgo, hour, minute, 0));

  const headers = [
    // --- Hari Ini ---
    {
      id: "dist_morning_sudirman",
      outlet_id: "outlet_sudirman",
      created_by: "gudang",
      confirmed_by: "staff",
      status: "RECEIVED",
      note: "Distribusi rutin pagi dari Gudang Pusat Medan Petisah ke Outlet Sudirman",
      confirmation_note: "Stok diterima lengkap sebelum buka gerobak, tahu masih segar",
      created_at: at(0, 6, 30),
      sent_at: at(0, 6, 45),
      confirmed_at: at(0, 7, 30),
    },
    {
      id: "dist_morning_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "gudang2",
      confirmed_by: null,
      status: "SENT",
      note: "Distribusi rutin pagi via kurir motor dari Gudang Medan Petisah",
      confirmation_note: null,
      created_at: at(0, 6, 35),
      sent_at: at(0, 6, 55),
      confirmed_at: null,
    },
    {
      id: "dist_request_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "staff_gm",
      confirmed_by: null,
      status: "DRAFT",
      note: "[REQUEST] Minta tambahan stok siang, pesanan area Gajah Mada & Petisah ramai",
      confirmation_note: null,
      created_at: at(0, 11, 20),
      sent_at: null,
      confirmed_at: null,
    },

    // --- Riwayat Kemarin ---
    {
      id: "dist_yday_sudirman",
      outlet_id: "outlet_sudirman",
      created_by: "gudang",
      confirmed_by: "staff2",
      status: "RECEIVED",
      note: "Distribusi rutin pagi ke Jl. Jenderal Sudirman",
      confirmation_note: "Jumlah sesuai surat jalan",
      created_at: at(1, 6, 25),
      sent_at: at(1, 6, 40),
      confirmed_at: at(1, 7, 25),
    },
    {
      id: "dist_yday_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "gudang2",
      confirmed_by: "staff_gm",
      status: "RECEIVED",
      note: "Distribusi rutin pagi ke Jl. Gajah Mada",
      confirmation_note: "Diterima lengkap",
      created_at: at(1, 6, 30),
      sent_at: at(1, 6, 50),
      confirmed_at: at(1, 7, 35),
    },
    {
      id: "dist_yday_cikini",
      outlet_id: "outlet_cikini",
      created_by: "gudang",
      confirmed_by: "staff_cikini",
      status: "PARTIALLY_RECEIVED",
      note: "Distribusi rutin pagi ke Outlet Dr. Mansyur (area USU)",
      confirmation_note: "Tahu original kurang 10 pcs karena wadah terguncang di jalan rusak Padang Bulan",
      created_at: at(1, 6, 40),
      sent_at: at(1, 7, 0),
      confirmed_at: at(1, 7, 50),
    },
    {
      id: "dist_yday_tebet",
      outlet_id: "outlet_tebet",
      created_by: "gudang2",
      confirmed_by: "staff_tebet",
      status: "RECEIVED",
      note: "Distribusi rutin pagi ke Outlet Ringroad Setia Budi",
      confirmation_note: "Semua bahan mentah diterima dalam kondisi baik",
      created_at: at(1, 6, 45),
      sent_at: at(1, 7, 10),
      confirmed_at: at(1, 8, 5),
    },
  ];

  const items = [
    // Hari Ini - Sudirman (RECEIVED)
    { id: "dist_item_sudirman_1", distribution_id: "dist_morning_sudirman", material_id: "mat_tahu_original", quantity_sent: 550, quantity_received: 550, note: null },
    { id: "dist_item_sudirman_2", distribution_id: "dist_morning_sudirman", material_id: "mat_tahu_pedas", quantity_sent: 320, quantity_received: 320, note: null },
    { id: "dist_item_sudirman_3", distribution_id: "dist_morning_sudirman", material_id: "mat_tahu_keju", quantity_sent: 200, quantity_received: 200, note: null },
    { id: "dist_item_sudirman_4", distribution_id: "dist_morning_sudirman", material_id: "mat_saus_sambal", quantity_sent: 50, quantity_received: 50, note: null },
    { id: "dist_item_sudirman_5", distribution_id: "dist_morning_sudirman", material_id: "mat_chilly_oil", quantity_sent: 5, quantity_received: 5, note: "5 botol chilly oil" },

    // Hari Ini - Gajah Mada (SENT)
    { id: "dist_item_gm_1", distribution_id: "dist_morning_gajah_mada", material_id: "mat_tahu_original", quantity_sent: 420, quantity_received: null, note: null },
    { id: "dist_item_gm_2", distribution_id: "dist_morning_gajah_mada", material_id: "mat_tahu_pedas", quantity_sent: 260, quantity_received: null, note: null },
    { id: "dist_item_gm_3", distribution_id: "dist_morning_gajah_mada", material_id: "mat_saus_sambal", quantity_sent: 40, quantity_received: null, note: null },

    // Hari Ini - Request Gajah Mada (DRAFT)
    { id: "dist_item_req_gm_1", distribution_id: "dist_request_gajah_mada", material_id: "mat_tahu_pedas", quantity_sent: 150, quantity_received: null, note: "Stok tahu pedas menipis jelang jam makan siang" },
    { id: "dist_item_req_gm_2", distribution_id: "dist_request_gajah_mada", material_id: "mat_chilly_oil", quantity_sent: 3, quantity_received: null, note: "Minta tambah 3 botol chilly oil" },

    // Kemarin - Sudirman (RECEIVED)
    { id: "dist_item_yday_sudirman_1", distribution_id: "dist_yday_sudirman", material_id: "mat_tahu_original", quantity_sent: 500, quantity_received: 500, note: null },
    { id: "dist_item_yday_sudirman_2", distribution_id: "dist_yday_sudirman", material_id: "mat_tahu_pedas", quantity_sent: 300, quantity_received: 300, note: null },
    { id: "dist_item_yday_sudirman_3", distribution_id: "dist_yday_sudirman", material_id: "mat_bumbu_balado", quantity_sent: 500, quantity_received: 500, note: "500 gr bubuk balado" },

    // Kemarin - Gajah Mada (RECEIVED)
    { id: "dist_item_yday_gm_1", distribution_id: "dist_yday_gajah_mada", material_id: "mat_tahu_original", quantity_sent: 400, quantity_received: 400, note: null },
    { id: "dist_item_yday_gm_2", distribution_id: "dist_yday_gajah_mada", material_id: "mat_tahu_pedas", quantity_sent: 240, quantity_received: 240, note: null },

    // Kemarin - Dr. Mansyur / outlet_cikini (PARTIALLY_RECEIVED)
    { id: "dist_item_yday_cikini_1", distribution_id: "dist_yday_cikini", material_id: "mat_tahu_original", quantity_sent: 480, quantity_received: 470, note: "10 pcs pecah di perjalanan" },
    { id: "dist_item_yday_cikini_2", distribution_id: "dist_yday_cikini", material_id: "mat_tahu_pedas", quantity_sent: 320, quantity_received: 320, note: null },
    { id: "dist_item_yday_cikini_3", distribution_id: "dist_yday_cikini", material_id: "mat_tahu_keju", quantity_sent: 180, quantity_received: 180, note: null },

    // Kemarin - Ringroad Setia Budi / outlet_tebet (RECEIVED)
    { id: "dist_item_yday_tebet_1", distribution_id: "dist_yday_tebet", material_id: "mat_tahu_original", quantity_sent: 380, quantity_received: 380, note: null },
    { id: "dist_item_yday_tebet_2", distribution_id: "dist_yday_tebet", material_id: "mat_tahu_pedas", quantity_sent: 220, quantity_received: 220, note: null },
    { id: "dist_item_yday_tebet_3", distribution_id: "dist_yday_tebet", material_id: "mat_bumbu_bbq", quantity_sent: 400, quantity_received: 400, note: null },
  ];

  return { headers, items };
}

async function seed(db, admin) {
  const { headers, items } = buildDistributions(admin);
  const headerCount = await batchUpsert(db, COLLECTIONS.MATERIAL_DISTRIBUTIONS, headers);
  const itemCount = await batchUpsert(db, COLLECTIONS.MATERIAL_DISTRIBUTION_ITEMS, items);
  console.log(
    `  [OK] ${COLLECTIONS.MATERIAL_DISTRIBUTIONS}: ${headerCount} distribusi & ${itemCount} item berhasil di-seed.`
  );
  return headerCount + itemCount;
}

module.exports = {
  name: "distributions",
  collection: COLLECTIONS.MATERIAL_DISTRIBUTIONS,
  buildDistributions,
  seed,
};
