const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

const DEFAULT_CATEGORIES = [
  { id: "cat_bumbu", name: "Bumbu", name_lower: "bumbu" },
  { id: "cat_saus", name: "Saus", name_lower: "saus" },
  { id: "cat_tahu", name: "Tahu", name_lower: "tahu" },
];

function inferCategoryId(materialName) {
  const lower = String(materialName || "").trim().toLowerCase();
  if (lower.includes("tahu")) return "cat_tahu";
  if (lower.includes("saus") || lower.includes("chilly") || lower.includes("oil")) {
    return "cat_saus";
  }
  return "cat_bumbu";
}

/**
 * Migration 006: Membuat collection `material_categories` (bumbu, saus, tahu)
 * dan melakukan backfill `category_id` pada seluruh dokumen di `materials`.
 */
async function up(db) {
  await batchUpsert(db, COLLECTIONS.MATERIAL_CATEGORIES, DEFAULT_CATEGORIES);

  const snapshot = await db.collection(COLLECTIONS.MATERIALS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  let updated = 0;

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    const existingCategoryId = String(data.category_id || "").trim();
    if (!existingCategoryId) {
      batch.update(doc.ref, { category_id: inferCategoryId(data.name) });
      updated++;
    }
  });

  if (updated > 0) {
    await batch.commit();
  }
}

async function down(db, admin) {
  const matSnapshot = await db.collection(COLLECTIONS.MATERIALS).get();
  if (!matSnapshot.empty) {
    const batch = db.batch();
    matSnapshot.docs.forEach((doc) => {
      batch.update(doc.ref, {
        category_id: admin.firestore.FieldValue.delete(),
      });
    });
    await batch.commit();
  }

  const catBatch = db.batch();
  DEFAULT_CATEGORIES.forEach((cat) => {
    catBatch.delete(db.collection(COLLECTIONS.MATERIAL_CATEGORIES).doc(cat.id));
  });
  await catBatch.commit();
}

module.exports = {
  up,
  down,
};
