const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

const COLLECTION = COLLECTIONS.OUTLETS;

function buildOutlets(admin) {
  const GeoPoint = admin.firestore.GeoPoint;
  return [
    {
      id: "outlet_sudirman",
      name: "Sudirman",
      address: "Jl. Jenderal Sudirman No. 18",
      coordinate: new GeoPoint(-6.21462, 106.82215),
      is_active: true,
      is_open: true,
      target_percentage: 0.85,
      total_revenue: 2480000,
      total_pieces_sold: 496,
      open_time: "09.00",
      close_time: "21.00",
      daily_target: 2900000,
    },
    {
      id: "outlet_gajah_mada",
      name: "Gajah Mada",
      address: "Jl. Gajah Mada No. 42",
      coordinate: new GeoPoint(-6.16042, 106.81863),
      is_active: true,
      is_open: true,
      target_percentage: 0.72,
      total_revenue: 2120000,
      total_pieces_sold: 424,
      open_time: "08.00",
      close_time: "21.00",
      daily_target: 2500000,
    },
    {
      // Outlet ini sengaja belum memiliki distribusi pagi hari ini di distributions.seed.js
      // untuk mendemokan UI Gudang: "outlet yang belum diberikan distribusi rutin pagi hari"
      id: "outlet_cikini",
      name: "Cikini",
      address: "Jl. Cikini Raya No. 15",
      coordinate: new GeoPoint(-6.18975, 106.83901),
      is_active: true,
      is_open: true,
      target_percentage: 0.5,
      total_revenue: 1780000,
      total_pieces_sold: 356,
      open_time: "08.00",
      close_time: "21.00",
      daily_target: 2400000,
    },
    {
      // Outlet ini juga belum diberikan distribusi rutin pagi hari ini
      id: "outlet_tebet",
      name: "Tebet",
      address: "Jl. Tebet Utara Dalam No. 9",
      coordinate: new GeoPoint(-6.22611, 106.85422),
      is_active: true,
      is_open: true,
      target_percentage: 0.4,
      total_revenue: 1250000,
      total_pieces_sold: 250,
      open_time: "09.00",
      close_time: "21.00",
      daily_target: 2000000,
    },
  ];
}

async function seed(db, admin) {
  const records = buildOutlets(admin);
  const count = await batchUpsert(db, COLLECTION, records);
  console.log(`  [OK] ${COLLECTION}: ${count} outlet gerobak berhasil di-seed.`);
  return count;
}

module.exports = {
  name: "outlets",
  collection: COLLECTION,
  seed,
};
