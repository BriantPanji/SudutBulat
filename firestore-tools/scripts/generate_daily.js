const { getFirebase } = require("../config/firebase");
const { COLLECTIONS, batchUpsert } = require("./helpers");

function randInt(min, max) {
  return min + Math.floor(Math.random() * (max - min + 1));
}

function pick(list) {
  return list[Math.floor(Math.random() * list.length)];
}

function shuffle(list) {
  const copy = [...list];
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

function formatDateKey(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}${m}${d}`;
}

function todayTimestamp(admin, hour, minute) {
  const now = new Date();
  const clampedHour = Math.min(hour, now.getHours());
  const clampedMinute = clampedHour === now.getHours() ? Math.min(minute, now.getMinutes()) : minute;
  return admin.firestore.Timestamp.fromDate(
    new Date(now.getFullYear(), now.getMonth(), now.getDate(), clampedHour, clampedMinute, randInt(0, 59))
  );
}

function randomSuffix() {
  return Math.random().toString(36).slice(2, 6);
}

const REQUEST_NOTES = [
  "Stok menipis karena pesanan siang ramai",
  "Permintaan tambahan stok sore untuk antisipasi jam pulang kerja",
  "Pesanan rombongan mendadak, butuh tambahan bahan",
  "Stok tahu pedas cepat habis hari ini",
];

const DELIVERY_METHODS = ["WAREHOUSE_COURIER", "SELF_PICKUP", "GOJEK", "GRAB", "MAXIM", "OTHER"];

const ROUTINE_NOTES = [
  "Distribusi rutin pagi otomatis dari Gudang Pusat",
  "Pengiriman rutin harian sesuai preset outlet",
  "Stok rutin pagi via kurir gudang",
];

const GAS_NOTES = [
  "Isi ulang Gas LPG 3kg di pangkalan terdekat",
  "Beli tabung Gas LPG 3kg operasional gerobak",
];

const PLASTIC_NOTES = [
  "Beli plastik kresek, kertas bungkus, dan tusuk lidi",
  "Plastik pembungkus tahu & cup sambal",
];

const OTHER_EXPENSE_NOTES = [
  "Minyak goreng 2 liter & kebutuhan kebersihan gerobak",
  "Minyak goreng curah 2 liter & retribusi lapak",
];

const DAMAGED_NOTES = [
  "Tahu pecah saat penggorengan jam ramai",
  "Tahu kempes kena hujan sore",
  "Sisa bahan tidak layak jual menjelang tutup",
];

/**
 * Generator data operasional HARIAN (tanggal otomatis HARI INI).
 * HANYA mengenerate data operasional harian (distribusi rutin, request tambahan, laporan harian).
 * TIDAK PERNAH membuat atau mengubah master data (akun/users, outlets, materials, products).
 */
async function loadMasterData(db) {
  const [outletsSnap, usersSnap, materialsSnap, productsSnap] = await Promise.all([
    db.collection(COLLECTIONS.OUTLETS).get(),
    db.collection(COLLECTIONS.USERS).get(),
    db.collection(COLLECTIONS.MATERIALS).get(),
    db.collection(COLLECTIONS.PRODUCTS).get(),
  ]);

  const activeOutlets = outletsSnap.docs
    .map((d) => ({ id: d.id, ...d.data() }))
    .filter((o) => o.is_active !== false);

  const activeUsers = usersSnap.docs
    .map((d) => ({ id: d.id, ...d.data() }))
    .filter((u) => u.is_active !== false);

  const activeMaterials = materialsSnap.docs
    .map((d) => ({ id: d.id, ...d.data() }))
    .filter((m) => m.is_active !== false);

  const activeProducts = productsSnap.docs
    .map((d) => ({ id: d.id, ...d.data() }))
    .filter((p) => p.is_active !== false);

  if (activeOutlets.length === 0) throw new Error("Tidak ada outlet aktif di Firestore. Jalankan seed master terlebih dahulu.");
  if (activeMaterials.length === 0) throw new Error("Tidak ada material aktif di Firestore.");

  return { activeOutlets, activeUsers, activeMaterials, activeProducts };
}

function buildItemsFromOutletPreset(outlet, activeMaterials) {
  const activeIds = new Set(activeMaterials.map((m) => m.id));
  const validPreset = Array.isArray(outlet.routine_preset)
    ? outlet.routine_preset.filter((p) => p && activeIds.has(p.material_id) && Number(p.quantity) > 0)
    : [];

  if (validPreset.length > 0) {
    return validPreset.map((p) => {
      // Variasi kecil +/- 10% agar terasa nyata
      const factor = 0.9 + Math.random() * 0.2;
      const qty = Math.max(1, Math.round(Number(p.quantity) * factor));
      return { material_id: p.material_id, quantity_sent: qty, quantity_received: null, note: null };
    });
  }

  return shuffle(activeMaterials).slice(0, Math.min(3, activeMaterials.length)).map((mat) => {
    const unit = String(mat.unit || "pcs").toLowerCase();
    const qty = unit === "unit" ? randInt(2, 6) : unit === "gr" ? randInt(200, 500) : randInt(150, 450);
    return { material_id: mat.id, quantity_sent: qty, quantity_received: null, note: null };
  });
}

async function generateRoutineDistributions(db, admin, master, todayKey) {
  const warehouseUsers = master.activeUsers.filter((u) => String(u.role || "").toLowerCase() === "warehouse");
  const staffByOutlet = new Map();
  master.activeUsers
    .filter((u) => String(u.role || "").toLowerCase() === "outlet" && u.outlet_id)
    .forEach((u) => {
      if (!staffByOutlet.has(u.outlet_id)) staffByOutlet.set(u.outlet_id, []);
      staffByOutlet.get(u.outlet_id).push(u.id);
    });

  // Biarkan 1 outlet tidak tergenerate jika outlet >= 3 agar tab "Belum dikirim hari ini" tetap terlihat
  const targetCount = master.activeOutlets.length >= 3 ? master.activeOutlets.length - 1 : master.activeOutlets.length;
  const chosenOutlets = shuffle(master.activeOutlets).slice(0, targetCount);

  const docs = chosenOutlets.map((outlet) => {
    const status = pick(["SENT", "ARRIVED", "RECEIVED", "RECEIVED", "PARTIALLY_RECEIVED"]);
    const deliveryMethod = pick(DELIVERY_METHODS);
    const createdBy = warehouseUsers.length > 0 ? pick(warehouseUsers).id : "gudang";
    const outletStaff = staffByOutlet.get(outlet.id) || [];
    const confirmedBy = status === "SENT" || status === "ARRIVED" ? null : outletStaff.length > 0 ? pick(outletStaff) : "staff";

    const rawItems = buildItemsFromOutletPreset(outlet, master.activeMaterials);
    const items = rawItems.map((item, idx) => {
      if (status === "RECEIVED") return { ...item, quantity_received: item.quantity_sent };
      if (status === "PARTIALLY_RECEIVED") {
        const received = idx === 0 ? Math.max(0, item.quantity_sent - randInt(5, 15)) : item.quantity_sent;
        return { ...item, quantity_received: received };
      }
      return item;
    });

    return {
      id: `dist_gen_routine_${outlet.id}_${todayKey}_${randomSuffix()}`,
      outlet_id: outlet.id,
      created_by: createdBy,
      confirmed_by: confirmedBy,
      status,
      type: "ROUTINE",
      delivery_method: deliveryMethod,
      note: pick(ROUTINE_NOTES),
      confirmation_note:
        status === "RECEIVED"
          ? "Stok rutin diterima lengkap"
          : status === "PARTIALLY_RECEIVED"
          ? "Sebagian bahan berkurang saat pengiriman"
          : null,
      created_at: todayTimestamp(admin, 6, randInt(15, 45)),
      sent_at: todayTimestamp(admin, 7, randInt(0, 25)),
      arrived_at: status === "SENT" ? null : todayTimestamp(admin, 7, randInt(26, 40)),
      confirmed_at: status === "SENT" || status === "ARRIVED" ? null : todayTimestamp(admin, 7, randInt(41, 55)),
      items,
    };
  });

  const count = await batchUpsert(db, COLLECTIONS.MATERIAL_DISTRIBUTIONS, docs);
  console.log(`  [OK] routine distributions (hari ini): ${count} dokumen berhasil di-generate.`);
  return count;
}

async function generateAdditionalRequests(db, admin, master, todayKey, requestCount) {
  const warehouseUsers = master.activeUsers.filter((u) => String(u.role || "").toLowerCase() === "warehouse");
  const staffUsers = master.activeUsers.filter((u) => String(u.role || "").toLowerCase() === "outlet" && u.outlet_id);

  const docs = [];
  for (let i = 0; i < requestCount; i++) {
    const outlet = pick(master.activeOutlets);
    const outletStaff = staffUsers.filter((u) => u.outlet_id === outlet.id);
    const requester = outletStaff.length > 0 ? pick(outletStaff).id : "staff";
    const whAdmin = warehouseUsers.length > 0 ? pick(warehouseUsers).id : "gudang";

    // Mayoritas DRAFT agar langsung bisa diuji Approve/Reject di tab Tambahan
    const status = i === 0 ? "DRAFT" : pick(["DRAFT", "DRAFT", "SENT", "ARRIVED", "RECEIVED", "CANCELLED"]);
    const deliveryMethod = pick(DELIVERY_METHODS);
    const pickedMaterials = shuffle(master.activeMaterials).slice(0, randInt(1, Math.min(3, master.activeMaterials.length)));
    const items = pickedMaterials.map((mat) => {
      const unit = String(mat.unit || "pcs").toLowerCase();
      const qty = unit === "unit" ? randInt(1, 5) : unit === "gr" ? randInt(100, 300) : randInt(50, 200);
      return {
        material_id: mat.id,
        quantity_sent: qty,
        quantity_received: status === "RECEIVED" ? qty : null,
        note: null,
      };
    });

    const createdHour = randInt(9, 14);
    docs.push({
      id: `dist_gen_req_${outlet.id}_${todayKey}_${randomSuffix()}`,
      outlet_id: outlet.id,
      created_by: requester,
      confirmed_by: status === "RECEIVED" ? requester : status === "CANCELLED" ? whAdmin : null,
      status,
      type: "ADDITIONAL",
      delivery_method: deliveryMethod,
      note: pick(REQUEST_NOTES),
      confirmation_note:
        status === "CANCELLED"
          ? "Stok bahan sedang terbatas di gudang"
          : status === "RECEIVED"
          ? "Tambahan stok sudah diterima di outlet"
          : null,
      created_at: todayTimestamp(admin, createdHour, randInt(0, 50)),
      sent_at:
        status === "SENT" || status === "ARRIVED" || status === "RECEIVED"
          ? todayTimestamp(admin, createdHour + 1, randInt(0, 25))
          : null,
      arrived_at:
        status === "ARRIVED" || status === "RECEIVED"
          ? todayTimestamp(admin, createdHour + 1, randInt(26, 40))
          : null,
      confirmed_at:
        status === "RECEIVED" || status === "CANCELLED"
          ? todayTimestamp(admin, createdHour + 1, randInt(41, 55))
          : null,
      items,
    });
  }

  const count = await batchUpsert(db, COLLECTIONS.MATERIAL_DISTRIBUTIONS, docs);
  console.log(`  [OK] additional requests (hari ini): ${count} dokumen berhasil di-generate.`);
  return count;
}

async function generateTodayReports(db, admin, master, todayKey, overwrite) {
  if (master.activeProducts.length === 0) {
    console.log("  [SKIP] daily_reports: tidak ada produk aktif di Firestore.");
    return 0;
  }

  const now = new Date();
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
  const existingSnap = await db.collection(COLLECTIONS.DAILY_REPORTS).get();
  const outletsWithTodayReport = new Set();

  existingSnap.docs.forEach((doc) => {
    const data = doc.data();
    if (data.date && typeof data.date.toDate === "function") {
      const d = data.date.toDate();
      if (d >= startOfToday) {
        outletsWithTodayReport.add(data.outlet_id);
      }
    }
  });

  const targetOutlets = overwrite
    ? master.activeOutlets
    : master.activeOutlets.filter((o) => !outletsWithTodayReport.has(o.id));

  if (targetOutlets.length === 0) {
    console.log("  [SKIP] daily_reports: semua outlet aktif sudah memiliki laporan hari ini (gunakan --overwrite untuk menimpa).");
    return 0;
  }

  const reports = [];
  const reportProducts = [];
  const reportExpenses = [];
  const reportDamaged = [];

  for (const outlet of targetOutlets) {
    const reportId = `report_gen_${outlet.id}_${todayKey}`;
    const productItems = [];
    let totalRevenue = 0;
    let totalSold = 0;

    for (const prod of master.activeProducts) {
      const qty = randInt(12, 95);
      const price = Number(prod.price) || 0;
      totalRevenue += qty * price;
      totalSold += qty;
      productItems.push({
        product_id: prod.id,
        quantity: qty,
        price,
      });
    }

    const qrisRatio = 0.35 + Math.random() * 0.25;
    const qrisRevenue = Math.min(totalRevenue, Math.round((totalRevenue * qrisRatio) / 1000) * 1000);
    const cashRevenue = totalRevenue - qrisRevenue;

    reports.push({
      id: reportId,
      outlet_id: outlet.id,
      date: todayTimestamp(admin, randInt(15, 20), randInt(0, 55)),
      cash_revenue: cashRevenue,
      qris_revenue: qrisRevenue,
      total_sold: totalSold,
    });

    reportProducts.push({
      id: reportId,
      daily_report_id: reportId,
      items: productItems,
    });

    reportExpenses.push({
      id: reportId,
      daily_report_id: reportId,
      items: [
        { category: "GAS", amount: randInt(20, 23) * 1000, note: pick(GAS_NOTES) },
        { category: "PLASTIC_BAG", amount: randInt(14, 22) * 1000, note: pick(PLASTIC_NOTES) },
        { category: "OTHER", amount: randInt(32, 55) * 1000, note: pick(OTHER_EXPENSE_NOTES) },
      ],
    });

    if (Math.random() < 0.6) {
      const damagedMat = pick(master.activeMaterials);
      reportDamaged.push({
        id: reportId,
        daily_report_id: reportId,
        items: [
          {
            material_id: damagedMat.id,
            quantity: randInt(2, 12),
            note: pick(DAMAGED_NOTES),
          },
        ],
      });
    }

    // Update cache ringkasan harian pada dokumen outlet yang sudah ada (tidak membuat outlet baru)
    const dailyTarget = Math.max(1, Number(outlet.daily_target) || 2000000);
    const targetPct = Math.min(1, Math.max(0, totalRevenue / dailyTarget));
    await db.collection(COLLECTIONS.OUTLETS).doc(outlet.id).update({
      total_revenue: totalRevenue,
      total_pieces_sold: totalSold,
      target_percentage: targetPct,
    });
  }

  await batchUpsert(db, COLLECTIONS.DAILY_REPORTS, reports);
  await batchUpsert(db, COLLECTIONS.DAILY_REPORT_PRODUCTS, reportProducts);
  await batchUpsert(db, COLLECTIONS.DAILY_REPORT_EXPENSES, reportExpenses);
  if (reportDamaged.length > 0) {
    await batchUpsert(db, COLLECTIONS.DAILY_REPORT_DAMAGED_MATERIALS, reportDamaged);
  }

  console.log(
    `  [OK] daily_reports (hari ini): ${reports.length} laporan, ${reportProducts.length} produk, ${reportExpenses.length} pengeluaran, ${reportDamaged.length} bahan rusak.`
  );
  return reports.length;
}

async function main() {
  const args = process.argv.slice(2);
  const targetArg = args.find((a) => !a.startsWith("--")) || "all";
  const overwrite = args.includes("--overwrite");
  const countFlag = args.find((a) => a.startsWith("--count="));
  const requestCount = countFlag ? Math.max(1, parseInt(countFlag.split("=")[1], 10) || 2) : randInt(2, 3);

  const validTargets = new Set(["all", "routine", "requests", "distributions", "reports"]);
  if (!validTargets.has(targetArg)) {
    console.error(
      "Usage: npm run generate:daily -- [all|routine|requests|distributions|reports] [--count=N] [--overwrite]"
    );
    process.exit(1);
  }

  const { db, admin } = getFirebase();
  const todayKey = formatDateKey(new Date());
  console.log(`=== SudutBulat Random Daily Generator (Tanggal: ${todayKey}, Target: ${targetArg}) ===`);

  const master = await loadMasterData(db);

  if (targetArg === "all" || targetArg === "routine" || targetArg === "distributions") {
    await generateRoutineDistributions(db, admin, master, todayKey);
  }
  if (targetArg === "all" || targetArg === "requests" || targetArg === "distributions") {
    await generateAdditionalRequests(db, admin, master, todayKey, requestCount);
  }
  if (targetArg === "all" || targetArg === "reports") {
    await generateTodayReports(db, admin, master, todayKey, overwrite);
  }

  console.log("=== Generate Data Harian Selesai ===");
}

main().catch((err) => {
  console.error("\n[ERROR]", err.message || err);
  process.exit(1);
});
