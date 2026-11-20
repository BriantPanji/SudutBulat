const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

const COLLECTION = COLLECTIONS.OUTLETS;

function buildOutlets(admin) {
  const GeoPoint = admin.firestore.GeoPoint;
  return [
    {
      id: "outlet_sudirman",
      name: "Sudirman",
      address: "Jl. Jenderal Sudirman No. 28, Medan Maimun, Kota Medan",
      coordinate: new GeoPoint(3.5778, 98.6749),
      is_active: true,
      is_open: true,
      target_percentage: 0.87,
      total_revenue: 2525000,
      total_pieces_sold: 488,
      open_time: "09.00",
      close_time: "21.00",
      daily_target: 2900000,
    },
    {
      id: "outlet_gajah_mada",
      name: "Gajah Mada",
      address: "Jl. Gajah Mada No. 45, Petisah Hulu, Medan Baru, Kota Medan",
      coordinate: new GeoPoint(3.5882, 98.6606),
      is_active: true,
      is_open: true,
      target_percentage: 0.82,
      total_revenue: 2055000,
      total_pieces_sold: 398,
      open_time: "08.00",
      close_time: "21.00",
      daily_target: 2500000,
    },
    {
      // ID tetap `outlet_cikini` agar FK di users.seed.js (staff_cikini) tidak putus.
      // Sengaja belum memiliki distribusi pagi hari ini untuk demo UI Gudang.
      id: "outlet_cikini",
      name: "Dr. Mansyur",
      address: "Jl. Dr. Mansyur No. 68, Padang Bulan Selayang I, Medan Selayang, Kota Medan",
      coordinate: new GeoPoint(3.5673, 98.6533),
      is_active: true,
      is_open: true,
      target_percentage: 0.86,
      total_revenue: 2236000,
      total_pieces_sold: 432,
      open_time: "10.00",
      close_time: "22.00",
      daily_target: 2600000,
    },
    {
      // ID tetap `outlet_tebet` agar FK di users.seed.js (staff_tebet) tidak putus.
      // Sengaja belum memiliki distribusi pagi hari ini untuk demo UI Gudang.
      id: "outlet_tebet",
      name: "Ringroad Setia Budi",
      address: "Jl. Gagak Hitam (Ringroad) No. 112, Tanjung Rejo, Medan Sunggal, Kota Medan",
      coordinate: new GeoPoint(3.5701, 98.6258),
      is_active: true,
      is_open: true,
      target_percentage: 0.78,
      total_revenue: 1716000,
      total_pieces_sold: 334,
      open_time: "10.00",
      close_time: "22.00",
      daily_target: 2200000,
    },
  ];
}

async function seed(db, admin) {
  const records = buildOutlets(admin);
  const count = await batchUpsert(db, COLLECTION, records);
  console.log(`  [OK] ${COLLECTION}: ${count} outlet gerobak (Medan) berhasil di-seed.`);
  return count;
}

module.exports = {
  name: "outlets",
  collection: COLLECTION,
  buildOutlets,
  seed,
};
