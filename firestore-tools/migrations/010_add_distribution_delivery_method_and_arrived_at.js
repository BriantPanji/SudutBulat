const { COLLECTIONS } = require("../scripts/helpers");

const VALID_DELIVERY_METHODS = new Set([
  "WAREHOUSE_COURIER",
  "SELF_PICKUP",
  "GOJEK",
  "GRAB",
  "MAXIM",
  "OTHER",
]);

/**
 * Migration 010:
 * 1. Menambahkan field `delivery_method` (default `"WAREHOUSE_COURIER"`) dan `arrived_at` (default `null`)
 *    pada seluruh dokumen koleksi `material_distributions` yang belum memilikinya.
 * 2. Memastikan field `photo_url` tersedia (default `null`) pada dokumen koleksi `users` yang belum memilikinya.
 */
async function up(db) {
  const chunkSize = 450;

  // 1. Backfill material_distributions
  const distSnap = await db.collection(COLLECTIONS.MATERIAL_DISTRIBUTIONS).get();
  if (!distSnap.empty) {
    const docs = distSnap.docs;
    for (let i = 0; i < docs.length; i += chunkSize) {
      const chunk = docs.slice(i, i + chunkSize);
      const batch = db.batch();
      let updated = 0;

      chunk.forEach((doc) => {
        const data = doc.data();
        const patch = {};

        const existingMethod = String(data.delivery_method || "").trim().toUpperCase();
        if (!VALID_DELIVERY_METHODS.has(existingMethod)) {
          patch.delivery_method = "WAREHOUSE_COURIER";
        }
        if (!Object.prototype.hasOwnProperty.call(data, "arrived_at")) {
          patch.arrived_at = null;
        }

        if (Object.keys(patch).length > 0) {
          batch.update(doc.ref, patch);
          updated++;
        }
      });

      if (updated > 0) {
        await batch.commit();
      }
    }
  }

  // 2. Backfill users.photo_url (optional, keeps schema consistent)
  const usersSnap = await db.collection(COLLECTIONS.USERS).get();
  if (!usersSnap.empty) {
    const docs = usersSnap.docs;
    for (let i = 0; i < docs.length; i += chunkSize) {
      const chunk = docs.slice(i, i + chunkSize);
      const batch = db.batch();
      let updated = 0;

      chunk.forEach((doc) => {
        const data = doc.data();
        if (!Object.prototype.hasOwnProperty.call(data, "photo_url")) {
          batch.update(doc.ref, { photo_url: null });
          updated++;
        }
      });

      if (updated > 0) {
        await batch.commit();
      }
    }
  }
}

async function down(db, admin) {
  const chunkSize = 450;

  const distSnap = await db.collection(COLLECTIONS.MATERIAL_DISTRIBUTIONS).get();
  if (!distSnap.empty) {
    const docs = distSnap.docs;
    for (let i = 0; i < docs.length; i += chunkSize) {
      const chunk = docs.slice(i, i + chunkSize);
      const batch = db.batch();

      chunk.forEach((doc) => {
        const data = doc.data();
        const patch = {
          delivery_method: admin.firestore.FieldValue.delete(),
          arrived_at: admin.firestore.FieldValue.delete(),
        };
        if (String(data.status || "").trim().toUpperCase() === "ARRIVED") {
          patch.status = "SENT";
        }
        batch.update(doc.ref, patch);
      });

      await batch.commit();
    }
  }
}

module.exports = {
  up,
  down,
};
