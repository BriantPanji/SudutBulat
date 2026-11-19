const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

/**
 * Sesuai PRD.md:
 * Setiap hari berakhir / outlet tutup, staf outlet melakukan laporan harian yang mencatat:
 * 1. Penjualan Produk (cash_revenue, qris_revenue, total_sold, & rincian daily_report_products)
 * 2. Pengeluaran Mandiri Outlet (daily_report_expenses: gas LPG, plastik pembungkus/kantong, lidi tusuk, minyak goreng, saus sambal umum)
 * 3. Kerugian / Bahan Mentah Rusak (daily_report_damaged_materials)
 */
function buildDailyReports(admin) {
  const Timestamp = admin.firestore.Timestamp;
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 18, 30, 0);
  const yesterday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1, 21, 0, 0);
  const threeDaysAgo = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 3, 21, 0, 0);
  const tenDaysAgo = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 10, 21, 0, 0);

  const reports = [
    {
      id: "report_sudirman_today",
      outlet_id: "outlet_sudirman",
      date: Timestamp.fromDate(today),
      cash_revenue: 1520000,
      qris_revenue: 960000,
      total_sold: 496,
    },
    {
      id: "report_cikini_today",
      outlet_id: "outlet_cikini",
      date: Timestamp.fromDate(today),
      cash_revenue: 1020000,
      qris_revenue: 760000,
      total_sold: 356,
    },
    {
      id: "report_sudirman_yesterday",
      outlet_id: "outlet_sudirman",
      date: Timestamp.fromDate(yesterday),
      cash_revenue: 1480000,
      qris_revenue: 1000000,
      total_sold: 496,
    },
    {
      id: "report_gajah_mada_yesterday",
      outlet_id: "outlet_gajah_mada",
      date: Timestamp.fromDate(yesterday),
      cash_revenue: 1320000,
      qris_revenue: 800000,
      total_sold: 424,
    },
    {
      id: "report_tebet_3d",
      outlet_id: "outlet_tebet",
      date: Timestamp.fromDate(threeDaysAgo),
      cash_revenue: 750000,
      qris_revenue: 500000,
      total_sold: 250,
    },
    {
      id: "report_gajah_mada_10d",
      outlet_id: "outlet_gajah_mada",
      date: Timestamp.fromDate(tenDaysAgo),
      cash_revenue: 1180000,
      qris_revenue: 870000,
      total_sold: 410,
    },
  ];

  const reportProducts = [
    // Penjualan Outlet Sudirman
    {
      id: "rep_prod_sudirman_1",
      daily_report_id: "report_sudirman_yesterday",
      product_id: "prod_tahu_ori_bungkus",
      quantity: 250,
      price: 5000,
    },
    {
      id: "rep_prod_sudirman_2",
      daily_report_id: "report_sudirman_yesterday",
      product_id: "prod_tahu_pedas_bungkus",
      quantity: 130,
      price: 6000,
    },
    {
      id: "rep_prod_sudirman_3",
      daily_report_id: "report_sudirman_yesterday",
      product_id: "prod_tahu_keju_bungkus",
      quantity: 50,
      price: 7000,
    },
    {
      id: "rep_prod_sudirman_4",
      daily_report_id: "report_sudirman_yesterday",
      product_id: "prod_tahu_ori_satuan",
      quantity: 50,
      price: 1000,
    },
    {
      id: "rep_prod_sudirman_5",
      daily_report_id: "report_sudirman_yesterday",
      product_id: "prod_addon_chilly_oil",
      quantity: 25,
      price: 2000,
    },

    // Penjualan Outlet Gajah Mada
    {
      id: "rep_prod_gm_1",
      daily_report_id: "report_gajah_mada_yesterday",
      product_id: "prod_tahu_ori_bungkus",
      quantity: 220,
      price: 5000,
    },
    {
      id: "rep_prod_gm_2",
      daily_report_id: "report_gajah_mada_yesterday",
      product_id: "prod_tahu_pedas_bungkus",
      quantity: 120,
      price: 6000,
    },
    {
      id: "rep_prod_gm_3",
      daily_report_id: "report_gajah_mada_yesterday",
      product_id: "prod_tahu_keju_bungkus",
      quantity: 40,
      price: 7000,
    },
    {
      id: "rep_prod_gm_4",
      daily_report_id: "report_gajah_mada_yesterday",
      product_id: "prod_tahu_ori_satuan",
      quantity: 20,
      price: 1000,
    },
  ];

  const reportExpenses = [
    // Pengeluaran Mandiri Outlet Sudirman
    {
      id: "rep_exp_sudirman_1",
      daily_report_id: "report_sudirman_yesterday",
      category: "GAS",
      amount: 22000,
      note: "Beli tabung Gas LPG 3kg",
    },
    {
      id: "rep_exp_sudirman_2",
      daily_report_id: "report_sudirman_yesterday",
      category: "PLASTIC_BAG",
      amount: 18000,
      note: "Plastik pembungkus kertas & plastik kantong kresek",
    },
    {
      id: "rep_exp_sudirman_3",
      daily_report_id: "report_sudirman_yesterday",
      category: "OTHER",
      amount: 45000,
      note: "Minyak goreng 2 liter & 2 pak lidi tusuk",
    },

    // Pengeluaran Mandiri Outlet Gajah Mada
    {
      id: "rep_exp_gm_1",
      daily_report_id: "report_gajah_mada_yesterday",
      category: "OTHER",
      amount: 52000,
      note: "Minyak goreng 2 liter & saus sambal umum bantal",
    },
    {
      id: "rep_exp_gm_2",
      daily_report_id: "report_gajah_mada_yesterday",
      category: "PLASTIC_BAG",
      amount: 15000,
      note: "Plastik kantong & tusuk lidi",
    },
  ];

  const reportDamagedMaterials = [
    {
      id: "rep_dmg_sudirman_1",
      daily_report_id: "report_sudirman_yesterday",
      material_id: "mat_tahu_original",
      quantity: 12,
      note: "Tahu pecah/kempes saat penggorengan",
    },
    {
      id: "rep_dmg_gm_1",
      daily_report_id: "report_gajah_mada_yesterday",
      material_id: "mat_tahu_pedas",
      quantity: 8,
      note: "Tahu asam/basi saat sore hari",
    },
  ];

  return {
    reports,
    reportProducts,
    reportExpenses,
    reportDamagedMaterials,
  };
}

async function seed(db, admin) {
  const { reports, reportProducts, reportExpenses, reportDamagedMaterials } =
    buildDailyReports(admin);

  const rCount = await batchUpsert(db, COLLECTIONS.DAILY_REPORTS, reports);
  const pCount = await batchUpsert(db, COLLECTIONS.DAILY_REPORT_PRODUCTS, reportProducts);
  const eCount = await batchUpsert(db, COLLECTIONS.DAILY_REPORT_EXPENSES, reportExpenses);
  const dCount = await batchUpsert(
    db,
    COLLECTIONS.DAILY_REPORT_DAMAGED_MATERIALS,
    reportDamagedMaterials
  );

  console.log(
    `  [OK] ${COLLECTIONS.DAILY_REPORTS}: ${rCount} laporan, ${pCount} item produk, ${eCount} pengeluaran, ${dCount} bahan rusak berhasil di-seed.`
  );
  return rCount + pCount + eCount + dCount;
}

module.exports = {
  name: "daily_reports",
  collection: COLLECTIONS.DAILY_REPORTS,
  seed,
};
