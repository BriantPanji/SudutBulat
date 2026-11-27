const { COLLECTIONS } = require("../scripts/helpers");

const DEFAULT_PRESET_BY_OUTLET_ID = {
  outlet_sudirman: [
    { material_id: "mat_tahu_original", quantity: 550 },
    { material_id: "mat_tahu_pedas", quantity: 320 },
    { material_id: "mat_tahu_keju", quantity: 200 },
    { material_id: "mat_saus_sambal", quantity: 50 },
    { material_id: "mat_chilly_oil", quantity: 5 },
  ],
  outlet_gajah_mada: [
    { material_id: "mat_tahu_original", quantity: 420 },
    { material_id: "mat_tahu_pedas", quantity: 260 },
    { material_id: "mat_tahu_keju", quantity: 140 },
    { material_id: "mat_saus_sambal", quantity: 40 },
    { material_id: "mat_chilly_oil", quantity: 4 },
  ],
  outlet_cikini: [
    { material_id: "mat_tahu_original", quantity: 480 },
    { material_id: "mat_tahu_pedas", quantity: 320 },
    { material_id: "mat_tahu_keju", quantity: 180 },
    { material_id: "mat_saus_sambal", quantity: 45 },
    { material_id: "mat_bumbu_balado", quantity: 400 },
  ],
  outlet_tebet: [
    { material_id: "mat_tahu_original", quantity: 380 },
    { material_id: "mat_tahu_pedas", quantity: 220 },
    { material_id: "mat_tahu_keju", quantity: 120 },
    { material_id: "mat_saus_sambal", quantity: 35 },
    { material_id: "mat_bumbu_bbq", quantity: 350 },
  ],
};

function buildFallbackPreset(activeMaterials) {
  return activeMaterials.slice(0, 4).map((mat) => {
    const unit = String(mat.unit || "pcs").toLowerCase();
    const defaultQty = unit === "gr" ? 300 : unit === "ml" ? 250 : unit === "unit" ? 3 : 250;
    return { material_id: mat.id, quantity: defaultQty };
  });
}

/**
 * Migration 008: Menambahkan field `routine_preset` (array of { material_id, quantity })
 * pada seluruh dokumen koleksi `outlets` agar setiap outlet memiliki preset distribusi rutin.
 */
async function up(db) {
  const [outletsSnap, materialsSnap] = await Promise.all([
    db.collection(COLLECTIONS.OUTLETS).get(),
    db.collection(COLLECTIONS.MATERIALS).get(),
  ]);

  if (outletsSnap.empty) return;

  const activeMaterials = materialsSnap.docs
    .map((doc) => ({ id: doc.id, ...doc.data() }))
    .filter((mat) => mat.is_active !== false);

  const fallbackPreset = buildFallbackPreset(activeMaterials);
  const batch = db.batch();
  let updated = 0;

  outletsSnap.docs.forEach((doc) => {
    const data = doc.data();
    if (!Array.isArray(data.routine_preset) || data.routine_preset.length === 0) {
      const preset = DEFAULT_PRESET_BY_OUTLET_ID[doc.id] || fallbackPreset;
      batch.update(doc.ref, { routine_preset: preset });
      updated++;
    }
  });

  if (updated > 0) {
    await batch.commit();
  }
}

async function down(db, admin) {
  const snapshot = await db.collection(COLLECTIONS.OUTLETS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  snapshot.docs.forEach((doc) => {
    batch.update(doc.ref, {
      routine_preset: admin.firestore.FieldValue.delete(),
    });
  });
  await batch.commit();
}

module.exports = {
  up,
  down,
};
