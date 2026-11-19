const { COLLECTIONS } = require("../scripts/helpers");

/**
 * Migration 004: Menambahkan field `is_active: true` pada seluruh dokumen
 * di collection `materials` untuk mendukung fitur nonaktifkan/aktifkan bahan mentah.
 */
async function up(db) {
  const snapshot = await db.collection(COLLECTIONS.MATERIALS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  let updated = 0;

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    if (typeof data.is_active !== "boolean") {
      batch.update(doc.ref, { is_active: true });
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
      is_active: admin.firestore.FieldValue.delete(),
    });
  });
  await batch.commit();
}

module.exports = {
  up,
  down,
};
