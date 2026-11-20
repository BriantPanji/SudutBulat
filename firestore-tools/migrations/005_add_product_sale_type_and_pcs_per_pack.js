const { COLLECTIONS } = require("../scripts/helpers");

const VALID_SALE_TYPES = ["satuan", "bungkus"];

/**
 * Migration 005: Menambahkan field `sale_type` ("satuan" | "bungkus") dan
 * `pcs_per_pack` (integer >= 1; 1 untuk satuan, >= 2 untuk bungkus)
 * pada seluruh dokumen di collection `products`.
 */
async function up(db) {
  const snapshot = await db.collection(COLLECTIONS.PRODUCTS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  let updated = 0;

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    const name = String(data.name || "");
    const rawSaleType = String(data.sale_type || "").trim().toLowerCase();

    const inferredSaleType = VALID_SALE_TYPES.includes(rawSaleType)
      ? rawSaleType
      : /bungkus/i.test(name)
        ? "bungkus"
        : "satuan";

    let inferredPcs;
    if (inferredSaleType === "bungkus") {
      if (Number.isInteger(data.pcs_per_pack) && data.pcs_per_pack >= 2) {
        inferredPcs = data.pcs_per_pack;
      } else {
        const match = name.match(/(\d+)\s*pcs/i);
        const parsed = match ? parseInt(match[1], 10) : 5;
        inferredPcs = Number.isInteger(parsed) && parsed >= 2 ? parsed : 5;
      }
    } else {
      inferredPcs = 1;
    }

    const updates = {};
    if (data.sale_type !== inferredSaleType) {
      updates.sale_type = inferredSaleType;
    }
    if (data.pcs_per_pack !== inferredPcs) {
      updates.pcs_per_pack = inferredPcs;
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
  const snapshot = await db.collection(COLLECTIONS.PRODUCTS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  snapshot.docs.forEach((doc) => {
    batch.update(doc.ref, {
      sale_type: admin.firestore.FieldValue.delete(),
      pcs_per_pack: admin.firestore.FieldValue.delete(),
    });
  });
  await batch.commit();
}

module.exports = {
  up,
  down,
};
