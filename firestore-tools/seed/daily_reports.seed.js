const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");
const { data: PRODUCTS } = require("./products.seed");

const PRODUCT_PRICE = Object.fromEntries(PRODUCTS.map((p) => [p.id, p.price]));

// Deterministic PRNG (Mulberry32) agar hasil seed selalu konsisten & idempotent tanpa dependency eksternal
function createRng(seed) {
  let a = seed >>> 0;
  return function next() {
    a |= 0;
    a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function randInt(rng, min, max) {
  return min + Math.floor(rng() * (max - min + 1));
}

function pick(rng, list) {
  return list[Math.floor(rng() * list.length)];
}

const OUTLET_PROFILES = [
  {
    id: "outlet_sudirman",
    slug: "sudirman",
    staffIds: ["staff", "staff2"],
    baseScale: 1.08,
    qrisRatio: 0.48,
    closeHour: 21,
    hasTodayReport: true,
    todayHour: 18,
    todayMinute: 45,
    gasNotes: [
      "Beli Gas LPG 3kg di pangkalan Jl. Imam Bonjol",
      "Isi ulang tabung Gas LPG 3kg dekat Simpang Juanda",
      "Beli Gas LPG 3kg di warung dekat Medan Maimun",
    ],
    plasticNotes: [
      "Plastik kresek & kertas minyak bungkus beli di Pajak Petisah",
      "Beli plastik bungkus tahu & 2 pak lidi tusuk di Pajak Petisah",
      "Plastik kantong ukuran sedang & tusuk lidi",
    ],
    otherNotes: [
      "Minyak goreng curah 2 liter & saus sambal bantal di Pajak Petisah",
      "Minyak goreng 2 liter, lap dapur & retribusi parkir gerobak",
      "Beli minyak goreng 2 liter & es batu untuk minuman staf",
    ],
    damagedNotes: [
      "Tahu pecah saat penggorengan jam ramai pulang kantor",
      "Tahu kempes karena hujan deras sore di area Sudirman",
      "Tahu sisa gorengan mengeras menjelang tutup",
    ],
  },
  {
    id: "outlet_gajah_mada",
    slug: "gm",
    staffIds: ["staff_gm"],
    baseScale: 0.88,
    qrisRatio: 0.42,
    closeHour: 21,
    hasTodayReport: true,
    todayHour: 18,
    todayMinute: 20,
    gasNotes: [
      "Beli Gas LPG 3kg di pangkalan Jl. Iskandar Muda",
      "Isi ulang Gas LPG 3kg dekat Petisah Hulu",
      "Beli tabung Gas LPG 3kg di warung Jl. Gajah Mada",
    ],
    plasticNotes: [
      "Plastik kresek, kertas minyak & tusuk lidi di Pajak Pringgan",
      "Beli plastik pembungkus & kantong kresek di Pajak Pringgan",
      "Plastik cup sambal & tusuk lidi 2 pak",
    ],
    otherNotes: [
      "Minyak goreng 2 liter & saus sambal umum bantal di Pajak Pringgan",
      "Minyak goreng 2 liter & retribusi kebersihan lapak",
      "Beli minyak goreng 2 liter & sabun cuci kuali",
    ],
    damagedNotes: [
      "Tahu asam/basi saat sore hari karena cuaca panas Medan",
      "Tahu pecah waktu dibalik di penggorengan",
      "Tahu lembek kena tampias hujan sore di Jl. Gajah Mada",
    ],
  },
  {
    // ID tetap `outlet_cikini` (Dr. Mansyur / area USU) agar FK users.seed.js tetap valid
    id: "outlet_cikini",
    slug: "mansyur",
    staffIds: ["staff_cikini"],
    baseScale: 0.96,
    qrisRatio: 0.56,
    closeHour: 22,
    hasTodayReport: true,
    todayHour: 19,
    todayMinute: 15,
    gasNotes: [
      "Beli Gas LPG 3kg di pangkalan Padang Bulan",
      "Isi ulang Gas LPG 3kg dekat Jl. Dr. Mansyur",
      "Beli Gas LPG 3kg di warung simpang Pintu 1 USU",
    ],
    plasticNotes: [
      "Plastik bungkus, kresek & lidi tusuk beli di Pajak USU (Pajus)",
      "Beli kertas bungkus & plastik kresek di Pajak Sore Padang Bulan",
      "Plastik cup chilly oil & tusuk lidi 3 pak",
    ],
    otherNotes: [
      "Minyak goreng 2 liter & tisu makan di Pajak USU",
      "Minyak goreng 2 liter & retribusi parkir gerobak Dr. Mansyur",
      "Beli minyak goreng 2 liter, saus sambal bantal & sabun cuci",
    ],
    damagedNotes: [
      "Tahu pecah saat antrean ramai mahasiswa USU sore hari",
      "Tahu kempes karena hujan lebat daerah Padang Bulan",
      "Sisa tahu mentah agak asam menjelang tutup jam 22.00",
    ],
  },
  {
    // ID tetap `outlet_tebet` (Ringroad Setia Budi) agar FK users.seed.js tetap valid
    id: "outlet_tebet",
    slug: "ringroad",
    staffIds: ["staff_tebet"],
    baseScale: 0.78,
    qrisRatio: 0.38,
    closeHour: 22,
    hasTodayReport: false,
    todayHour: 19,
    todayMinute: 0,
    gasNotes: [
      "Beli Gas LPG 3kg di pangkalan Jl. Setia Budi",
      "Isi ulang Gas LPG 3kg dekat Simpang Ringroad",
      "Beli Gas LPG 3kg di warung Tanjung Rejo",
    ],
    plasticNotes: [
      "Plastik kresek & kertas bungkus di Pajak Melati / Simpang Selayang",
      "Beli plastik bungkus tahu & tusuk lidi di Jl. Setia Budi",
      "Kantong plastik 2 ukuran & lidi tusuk",
    ],
    otherNotes: [
      "Minyak goreng 2 liter & saus sambal di Simpang Selayang",
      "Minyak goreng 2 liter & iuran lampu/keamanan lapak Ringroad",
      "Beli minyak goreng 2 liter & saringan minyak baru",
    ],
    damagedNotes: [
      "Tahu kempes kena angin kencang & hujan di area Ringroad",
      "Tahu pecah saat penggorengan kloter malam",
      "Tahu sisa tidak layak jual menjelang tutup",
    ],
  },
];

// Kasus khusus pengeluaran besar (>= Rp200.000) agar indikator alert di Owner Dashboard teruji nyata
const SPECIAL_EXPENSES = {
  "sudirman_4": {
    amount: 235000,
    note: "Servis kompor mawar & ganti selang regulator di bengkel Jl. Juanda",
  },
  "mansyur_9": {
    amount: 215000,
    note: "Ganti terpal tenda gerobak sobek kena angin hujan deras di Dr. Mansyur",
  },
};

/**
 * Sesuai PRD.md & OutletViewModel.kt:
 * - Menghasilkan 14 hari laporan harian (hari ini s/d 13 hari lalu) untuk 4 outlet Medan.
 * - Setiap laporan 100% konsisten secara matematis:
 *   total_sold = sum(report_products.quantity)
 *   cash_revenue + qris_revenue = sum(report_products.quantity * price)
 */
function buildDailyReports(admin) {
  const Timestamp = admin.firestore.Timestamp;
  const now = new Date();
  const rng = createRng(20260515);

  const reports = [];
  const reportProducts = [];
  const reportExpenses = [];
  const reportDamagedMaterials = [];

  for (let daysAgo = 0; daysAgo < 14; daysAgo++) {
    const baseDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() - daysAgo);
    const dayOfWeek = baseDate.getDay(); // 0 = Minggu, 5 = Jumat, 6 = Sabtu
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 5 || dayOfWeek === 6;
    const isRainyDay = daysAgo > 0 && rng() < 0.18;

    for (const profile of OUTLET_PROFILES) {
      if (daysAgo === 0 && !profile.hasTodayReport) continue;

      const reportId = `report_${profile.slug}_d${daysAgo}`;
      const reportDate =
        daysAgo === 0
          ? new Date(baseDate.getFullYear(), baseDate.getMonth(), baseDate.getDate(), profile.todayHour, profile.todayMinute, 0)
          : new Date(
              baseDate.getFullYear(),
              baseDate.getMonth(),
              baseDate.getDate(),
              profile.closeHour,
              randInt(rng, 5, 45),
              0
            );

      // Faktor harian: akhir pekan lebih ramai, hujan sore di Medan sedikit menurunkan penjualan
      let dayFactor = profile.baseScale * (0.9 + rng() * 0.2);
      if (isWeekend) dayFactor *= 1.16;
      if (isRainyDay) dayFactor *= 0.82;
      if (daysAgo === 0) dayFactor *= 0.88; // laporan sore sebelum jam tutup penuh

      const isStudentArea = profile.slug === "mansyur";
      const productSpecs = [
        { id: "prod_tahu_ori_bungkus", qty: Math.round(randInt(rng, 185, 245) * dayFactor) },
        { id: "prod_tahu_pedas_bungkus", qty: Math.round(randInt(rng, isStudentArea ? 115 : 95, isStudentArea ? 150 : 130) * dayFactor) },
        { id: "prod_tahu_keju_bungkus", qty: Math.round(randInt(rng, isStudentArea ? 45 : 32, isStudentArea ? 70 : 52) * dayFactor) },
        { id: "prod_tahu_ori_satuan", qty: Math.round(randInt(rng, 25, 55) * dayFactor) },
        { id: "prod_tahu_pedas_satuan", qty: Math.round(randInt(rng, 15, 35) * dayFactor) },
        { id: "prod_tahu_keju_satuan", qty: rng() < 0.7 ? Math.round(randInt(rng, 8, 20) * dayFactor) : 0 },
        { id: "prod_addon_chilly_oil", qty: Math.round(randInt(rng, 14, 34) * dayFactor) },
      ];

      let totalRevenue = 0;
      let totalSold = 0;
      const productItems = [];

      for (const spec of productSpecs) {
        if (spec.qty <= 0) continue;
        const price = PRODUCT_PRICE[spec.id];
        totalRevenue += spec.qty * price;
        totalSold += spec.qty;

        productItems.push({
          product_id: spec.id,
          quantity: spec.qty,
          price,
        });
      }

      if (productItems.length > 0) {
        reportProducts.push({
          id: reportId,
          daily_report_id: reportId,
          items: productItems,
        });
      }

      const qrisShare = Math.min(0.7, Math.max(0.25, profile.qrisRatio + (rng() - 0.5) * 0.1));
      const qrisRevenue = Math.min(totalRevenue, Math.round((totalRevenue * qrisShare) / 1000) * 1000);
      const cashRevenue = totalRevenue - qrisRevenue;

      const reportDoc = {
        id: reportId,
        outlet_id: profile.id,
        created_by: pick(rng, profile.staffIds),
        date: Timestamp.fromDate(reportDate),
        cash_revenue: cashRevenue,
        qris_revenue: qrisRevenue,
        total_sold: totalSold,
        review_status: "PENDING",
        review_note: null,
        reviewed_by: null,
        reviewed_at: null,
      };
      reports.push(reportDoc);

      // Pengeluaran harian outlet (1 dokumen per laporan berisi array items)
      const expenseItems = [];
      if (rng() < 0.78 || daysAgo <= 1) {
        expenseItems.push({
          category: "GAS",
          amount: randInt(rng, 20, 23) * 1000,
          note: pick(rng, profile.gasNotes),
        });
      }

      expenseItems.push({
        category: "PLASTIC_BAG",
        amount: randInt(rng, 14, 22) * 1000,
        note: pick(rng, profile.plasticNotes),
      });

      const specialKey = `${profile.slug}_${daysAgo}`;
      if (SPECIAL_EXPENSES[specialKey]) {
        expenseItems.push({
          category: "OTHER",
          amount: SPECIAL_EXPENSES[specialKey].amount,
          note: SPECIAL_EXPENSES[specialKey].note,
        });
      } else {
        expenseItems.push({
          category: "OTHER",
          amount: randInt(rng, 34, 56) * 1000,
          note: pick(rng, profile.otherNotes),
        });
      }

      reportExpenses.push({
        id: reportId,
        daily_report_id: reportId,
        items: expenseItems,
      });

      // Bahan rusak (~48% laporan, 1 dokumen per laporan berisi array items)
      const hasDamage = isRainyDay ? rng() < 0.75 : rng() < 0.42;
      if (hasDamage) {
        const damagedItems = [];
        const primaryMat = pick(rng, ["mat_tahu_original", "mat_tahu_pedas", "mat_tahu_keju"]);
        // Sesekali >= 10 pcs agar alert bahan rusak di Owner Dashboard teruji
        const isHighDamage = isRainyDay && rng() < 0.45;
        const qty = isHighDamage ? randInt(rng, 10, 15) : randInt(rng, 3, 9);

        damagedItems.push({
          material_id: primaryMat,
          quantity: qty,
          note: pick(rng, profile.damagedNotes),
        });

        if (rng() < 0.22) {
          damagedItems.push({
            material_id: pick(rng, ["mat_bumbu_balado", "mat_bumbu_bbq", "mat_saus_sambal"]),
            quantity: randInt(rng, 1, 3),
            note: "Bumbu menggumpal / kemasan bocor saat operasional",
          });
        }

        reportDamagedMaterials.push({
          id: reportId,
          daily_report_id: reportId,
          items: damagedItems,
        });
      }

      // Status tinjauan Owner untuk demo end-to-end (PENDING, APPROVED, REJECTED):
      if (profile.slug === "sudirman" && daysAgo === 1) {
        reportDoc.review_status = "REJECTED";
        reportDoc.review_note =
          "Mohon periksa kembali nota pembelian minyak goreng & plastik, nominal pengeluaran belum sesuai buku kas outlet.";
        reportDoc.reviewed_by = "owner";
        reportDoc.reviewed_at = Timestamp.fromDate(new Date(reportDate.getTime() + 30 * 60 * 1000));
      } else if (specialKey === "mansyur_9") {
        reportDoc.review_status = "REJECTED";
        reportDoc.review_note =
          "Pengeluaran ganti terpal Rp215.000 cukup besar, mohon cantumkan rincian harga terpal dan biaya pasang di catatan.";
        reportDoc.reviewed_by = "owner";
        reportDoc.reviewed_at = Timestamp.fromDate(new Date(reportDate.getTime() + 45 * 60 * 1000));
      } else if (daysAgo === 2 || daysAgo === 5 || daysAgo === 7) {
        reportDoc.review_status = "APPROVED";
        reportDoc.review_note = "Laporan sesuai rekap mutasi kas & QRIS.";
        reportDoc.reviewed_by = "owner";
        reportDoc.reviewed_at = Timestamp.fromDate(new Date(reportDate.getTime() + 60 * 60 * 1000));
      }
    }
  }

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
    `  [OK] ${COLLECTIONS.DAILY_REPORTS}: ${rCount} doc laporan, ${pCount} doc produk, ${eCount} doc pengeluaran, ${dCount} doc bahan rusak berhasil di-seed.`
  );
  return rCount + pCount + eCount + dCount;
}

module.exports = {
  name: "daily_reports",
  collection: COLLECTIONS.DAILY_REPORTS,
  buildDailyReports,
  seed,
};
