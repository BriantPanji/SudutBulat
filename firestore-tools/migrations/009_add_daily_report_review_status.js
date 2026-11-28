const { COLLECTIONS } = require("../scripts/helpers");

/**
 * Migration 009: Menambahkan field status tinjauan Owner (`review_status`, `review_note`,
 * `reviewed_by`, `reviewed_at`) pada seluruh dokumen koleksi `daily_reports`.
 */
async function up(db) {
  const snapshot = await db.collection(COLLECTIONS.DAILY_REPORTS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  let updated = 0;

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    if (!data.review_status) {
      batch.update(doc.ref, {
        review_status: "PENDING",
        review_note: null,
        reviewed_by: null,
        reviewed_at: null,
      });
      updated++;
    }
  });

  if (updated > 0) {
    await batch.commit();
  }
}

async function down(db, admin) {
  const snapshot = await db.collection(COLLECTIONS.DAILY_REPORTS).get();
  if (snapshot.empty) return;

  const batch = db.batch();
  snapshot.docs.forEach((doc) => {
    batch.update(doc.ref, {
      review_status: admin.firestore.FieldValue.delete(),
      review_note: admin.firestore.FieldValue.delete(),
      reviewed_by: admin.firestore.FieldValue.delete(),
      reviewed_at: admin.firestore.FieldValue.delete(),
    });
  });
  await batch.commit();
}

module.exports = {
  up,
  down,
};
