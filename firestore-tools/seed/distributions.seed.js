const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

/**
 * Sesuai PRD.md:
 * 1. Distribusi Rutin Pagi:
 *    - `dist_morning_sudirman` -> Sudah dikirim pagi ini dan dikonfirmasi RECEIVED oleh staf outlet Sudirman.
 *    - `dist_morning_gajah_mada` -> Sudah dikirim pagi ini (SENT), menunggu konfirmasi staf outlet Gajah Mada.
 *    - Catatan: `outlet_cikini` dan `outlet_tebet` sengaja BELUM diberikan distribusi pagi hari ini
 *      agar UI Gudang dapat menampilkan daftar outlet yang belum menerima distribusi rutin pagi.
 * 2. Request Tambahan Stok dari Outlet:
 *    - `dist_request_gajah_mada` -> Dibuat oleh `staff_gm` (status DRAFT) meminta tambahan Tahu Pedas & Chilly Oil,
 *      siap diterima (Approve -> SENT) atau ditolak (Reject -> CANCELLED) oleh Admin Gudang.
 */
function buildDistributions(admin) {
  const Timestamp = admin.firestore.Timestamp;
  const now = new Date();
  const morningCreated = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 6, 30, 0);
  const morningSent = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 6, 45, 0);
  const morningConfirmed = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 7, 30, 0);
  const requestCreated = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 11, 15, 0);

  const headers = [
    {
      id: "dist_morning_sudirman",
      outlet_id: "outlet_sudirman",
      created_by: "gudang",
      confirmed_by: "staff",
      status: "RECEIVED",
      note: "Distribusi rutin pagi hari",
      confirmation_note: "Stok diterima lengkap dan sesuai",
      created_at: Timestamp.fromDate(morningCreated),
      sent_at: Timestamp.fromDate(morningSent),
      confirmed_at: Timestamp.fromDate(morningConfirmed),
    },
    {
      id: "dist_morning_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "gudang2",
      confirmed_by: null,
      status: "SENT",
      note: "Distribusi rutin pagi hari",
      confirmation_note: null,
      created_at: Timestamp.fromDate(morningCreated),
      sent_at: Timestamp.fromDate(morningSent),
      confirmed_at: null,
    },
    {
      id: "dist_request_gajah_mada",
      outlet_id: "outlet_gajah_mada",
      created_by: "staff_gm",
      confirmed_by: null,
      status: "DRAFT",
      note: "[REQUEST] Permintaan tambahan stok siang karena pesanan ramai",
      confirmation_note: null,
      created_at: Timestamp.fromDate(requestCreated),
      sent_at: null,
      confirmed_at: null,
    },
  ];

  const items = [
    // Item Distribusi Rutin Pagi - Outlet Sudirman (RECEIVED)
    {
      id: "dist_item_sudirman_1",
      distribution_id: "dist_morning_sudirman",
      material_id: "mat_tahu_original",
      quantity_sent: 500,
      quantity_received: 500,
      note: null,
    },
    {
      id: "dist_item_sudirman_2",
      distribution_id: "dist_morning_sudirman",
      material_id: "mat_tahu_pedas",
      quantity_sent: 300,
      quantity_received: 300,
      note: null,
    },
    {
      id: "dist_item_sudirman_3",
      distribution_id: "dist_morning_sudirman",
      material_id: "mat_tahu_keju",
      quantity_sent: 200,
      quantity_received: 200,
      note: null,
    },
    {
      id: "dist_item_sudirman_4",
      distribution_id: "dist_morning_sudirman",
      material_id: "mat_saus_sambal",
      quantity_sent: 50,
      quantity_received: 50,
      note: null,
    },
    {
      id: "dist_item_sudirman_5",
      distribution_id: "dist_morning_sudirman",
      material_id: "mat_chilly_oil",
      quantity_sent: 5,
      quantity_received: 5,
      note: "5 botol chilly oil",
    },

    // Item Distribusi Rutin Pagi - Outlet Gajah Mada (SENT, menunggu konfirmasi)
    {
      id: "dist_item_gm_1",
      distribution_id: "dist_morning_gajah_mada",
      material_id: "mat_tahu_original",
      quantity_sent: 400,
      quantity_received: null,
      note: null,
    },
    {
      id: "dist_item_gm_2",
      distribution_id: "dist_morning_gajah_mada",
      material_id: "mat_tahu_pedas",
      quantity_sent: 250,
      quantity_received: null,
      note: null,
    },
    {
      id: "dist_item_gm_3",
      distribution_id: "dist_morning_gajah_mada",
      material_id: "mat_saus_sambal",
      quantity_sent: 40,
      quantity_received: null,
      note: null,
    },

    // Item Request Tambahan Stok dari Staf Outlet Gajah Mada (DRAFT)
    {
      id: "dist_item_req_gm_1",
      distribution_id: "dist_request_gajah_mada",
      material_id: "mat_tahu_pedas",
      quantity_sent: 150,
      quantity_received: null,
      note: "Stok tahu pedas menipis",
    },
    {
      id: "dist_item_req_gm_2",
      distribution_id: "dist_request_gajah_mada",
      material_id: "mat_chilly_oil",
      quantity_sent: 3,
      quantity_received: null,
      note: "Minta tambah 3 botol chilly oil",
    },
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
  seed,
};
