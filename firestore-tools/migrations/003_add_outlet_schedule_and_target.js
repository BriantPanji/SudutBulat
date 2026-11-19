const { COLLECTIONS } = require("../scripts/helpers");

/**
 * Migration 003: Menambahkan field `open_time`, `close_time`, dan `daily_target`
 * pada seluruh dokumen di collection `outlets` agar selaras dengan model Outlet.kt
 * dan form Tambah Outlet / Detail Outlet.
 */
async function up(db) {
  const snapshot = await db.collection(COLLECTIONS.OUTLETS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  let updated = 0;

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    const updates = {};

    if (typeof data.open_time !== "string" || !data.open_time.trim()) {
      updates.open_time = "08.00";
    }
    if (typeof data.close_time !== "string" || !data.close_time.trim()) {
      updates.close_time = "21.00";
    }
    if (typeof data.daily_target !== "number" || data.daily_target <= 0) {
      updates.daily_target = 2000000;
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
  const snapshot = await db.collection(COLLECTIONS.OUTLETS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  snapshot.docs.forEach((doc) => {
    batch.update(doc.ref, {
      open_time: admin.firestore.FieldValue.delete(),
      close_time: admin.firestore.FieldValue.delete(),
      daily_target: admin.firestore.FieldValue.delete(),
    });
  });
  await batch.commit();
}

module.exports = {
  up,
  down,
};
