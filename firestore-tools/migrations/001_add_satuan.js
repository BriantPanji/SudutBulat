const { COLLECTIONS } = require("../scripts/helpers");

const VALID_UNITS = ["pcs", "gr", "ml", "unit"];

/**
 * Migration 001: Memastikan seluruh dokumen di collection `materials`
 * memiliki field `unit` ("pcs", "gr", "ml", "unit") dan `cost` (number >= 0)
 * sesuai kontrak PRD.md & UnitType.kt.
 */
async function up(db) {
  const snapshot = await db.collection(COLLECTIONS.MATERIALS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  let updated = 0;

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    const updates = {};

    // Normalisasi jika ada field lama `satuan` atau `unit` belum valid
    const rawUnit = String(data.unit || data.satuan || "").toLowerCase();
    if (!VALID_UNITS.includes(rawUnit)) {
      updates.unit = "pcs";
    } else if (data.unit !== rawUnit) {
      updates.unit = rawUnit;
    }

    if (typeof data.cost !== "number" || data.cost < 0) {
      updates.cost = 0;
    }

    if (Object.keys(updates).length > 0) {
      batch.update(doc.ref, updates);
      updated++;
    }
  });

  if (updated > 0) {
    await batch.commit();
  }
}

async function down(db, admin) {
  const snapshot = await db.collection(COLLECTIONS.MATERIALS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  snapshot.docs.forEach((doc) => {
    batch.update(doc.ref, {
      unit: admin.firestore.FieldValue.delete(),
    });
  });
  await batch.commit();
}

module.exports = {
  up,
  down,
};
