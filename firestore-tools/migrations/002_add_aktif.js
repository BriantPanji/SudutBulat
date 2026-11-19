const { COLLECTIONS } = require("../scripts/helpers");

/**
 * Migration 002: Memastikan seluruh dokumen di collection `users`, `outlets`, dan `products`
 * memiliki field boolean `is_active` (serta `is_open` pada `outlets`) sesuai kontrak PRD.md.
 */
async function up(db) {
  const targetCollections = [
    COLLECTIONS.USERS,
    COLLECTIONS.OUTLETS,
    COLLECTIONS.PRODUCTS,
  ];

  for (const colName of targetCollections) {
    const snapshot = await db.collection(colName).get();
    if (snapshot.empty) continue;

    const batch = db.batch();
    let updated = 0;

    snapshot.docs.forEach((doc) => {
      const data = doc.data();
      const updates = {};

      if (typeof data.is_active !== "boolean") {
        updates.is_active = true;
      }

      if (colName === COLLECTIONS.OUTLETS && typeof data.is_open !== "boolean") {
        updates.is_open = true;
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
}

async function down(db, admin) {
  const targetCollections = [
    COLLECTIONS.USERS,
    COLLECTIONS.OUTLETS,
    COLLECTIONS.PRODUCTS,
  ];

  for (const colName of targetCollections) {
    const snapshot = await db.collection(colName).get();
    if (snapshot.empty) continue;

    const batch = db.batch();
    snapshot.docs.forEach((doc) => {
      const updates = {
        is_active: admin.firestore.FieldValue.delete(),
      };
      if (colName === COLLECTIONS.OUTLETS) {
        updates.is_open = admin.firestore.FieldValue.delete();
      }
      batch.update(doc.ref, updates);
    });
    await batch.commit();
  }
}

module.exports = {
  up,
  down,
};
