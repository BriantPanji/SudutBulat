const COLLECTIONS = {
  USERS: "users",
  OUTLETS: "outlets",
  MATERIAL_CATEGORIES: "material_categories",
  MATERIALS: "materials",
  PRODUCTS: "products",
  MATERIAL_DISTRIBUTIONS: "material_distributions",
  DAILY_REPORTS: "daily_reports",
  DAILY_REPORT_PRODUCTS: "daily_report_products",
  DAILY_REPORT_EXPENSES: "daily_report_expenses",
  DAILY_REPORT_DAMAGED_MATERIALS: "daily_report_damaged_materials",
  MIGRATIONS: "_migrations",
};

/**
 * Upsert multiple documents into a Firestore collection in batches of 450.
 * If record contains `id`, it uses that deterministic document ID (idempotent).
 */
async function batchUpsert(db, collectionName, records, options = { merge: true }) {
  const chunkSize = 450;
  let count = 0;

  for (let i = 0; i < records.length; i += chunkSize) {
    const chunk = records.slice(i, i + chunkSize);
    const batch = db.batch();

    for (const record of chunk) {
      const { id, ...data } = record;
      const docRef = id
        ? db.collection(collectionName).doc(String(id))
        : db.collection(collectionName).doc();
      batch.set(docRef, data, options);
      count++;
    }

    await batch.commit();
  }

  return count;
}

/**
 * Delete all documents in a collection in batches.
 */
async function clearCollection(db, collectionName) {
  const snapshot = await db.collection(collectionName).get();
  if (snapshot.empty) return 0;

  const chunkSize = 450;
  const docs = snapshot.docs;
  for (let i = 0; i < docs.length; i += chunkSize) {
    const chunk = docs.slice(i, i + chunkSize);
    const batch = db.batch();
    for (const doc of chunk) {
      batch.delete(doc.ref);
    }
    await batch.commit();
  }

  return docs.length;
}

module.exports = {
  COLLECTIONS,
  batchUpsert,
  clearCollection,
};
