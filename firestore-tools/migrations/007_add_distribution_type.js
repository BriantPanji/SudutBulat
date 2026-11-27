const { COLLECTIONS } = require("../scripts/helpers");

function inferDistributionType(docId, data, userRoleById) {
  const existing = String(data.type || "").trim().toUpperCase();
  if (existing === "ROUTINE" || existing === "ADDITIONAL") {
    return existing;
  }

  const idLower = String(docId || "").toLowerCase();
  const noteLower = String(data.note || "").trim().toLowerCase();
  const statusUpper = String(data.status || "").trim().toUpperCase();
  const creatorRole = userRoleById.get(String(data.created_by || "")) || "";

  if (
    idLower.includes("request") ||
    noteLower.startsWith("[request]") ||
    statusUpper === "DRAFT" ||
    creatorRole === "outlet"
  ) {
    return "ADDITIONAL";
  }

  return "ROUTINE";
}

/**
 * Migration 007: Menambahkan field `type` (`ROUTINE` | `ADDITIONAL`) pada
 * seluruh dokumen di koleksi `material_distributions`.
 */
async function up(db) {
  const [distSnap, usersSnap] = await Promise.all([
    db.collection(COLLECTIONS.MATERIAL_DISTRIBUTIONS).get(),
    db.collection(COLLECTIONS.USERS).get(),
  ]);

  if (distSnap.empty) return;

  const userRoleById = new Map();
  usersSnap.docs.forEach((doc) => {
    const role = String(doc.data().role || "").trim().toLowerCase();
    userRoleById.set(doc.id, role);
  });

  const docs = distSnap.docs;
  const chunkSize = 450;

  for (let i = 0; i < docs.length; i += chunkSize) {
    const chunk = docs.slice(i, i + chunkSize);
    const batch = db.batch();
    let updated = 0;

    chunk.forEach((doc) => {
      const data = doc.data();
      const targetType = inferDistributionType(doc.id, data, userRoleById);
      if (data.type !== targetType) {
        batch.update(doc.ref, { type: targetType });
        updated++;
      }
    });

    if (updated > 0) {
      await batch.commit();
    }
  }
}

async function down(db, admin) {
  const snapshot = await db.collection(COLLECTIONS.MATERIAL_DISTRIBUTIONS).get();
  if (snapshot.empty) return;

  const docs = snapshot.docs;
  const chunkSize = 450;

  for (let i = 0; i < docs.length; i += chunkSize) {
    const chunk = docs.slice(i, i + chunkSize);
    const batch = db.batch();
    chunk.forEach((doc) => {
      batch.update(doc.ref, {
        type: admin.firestore.FieldValue.delete(),
      });
    });
    await batch.commit();
  }
}

module.exports = {
  up,
  down,
};
