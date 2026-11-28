const { COLLECTIONS } = require("../scripts/helpers");

const DEFAULT_MEDAN_COORDINATE = { lat: 3.5952, lng: 98.6722 };

// Known Medan coordinates for seeded outlet IDs if their coordinate was missing or reset to (0, 0).
const KNOWN_MEDAN_OUTLET_COORDS = {
  outlet_sudirman: { lat: 3.5778, lng: 98.6749 },
  outlet_gajah_mada: { lat: 3.5882, lng: 98.6606 },
  outlet_cikini: { lat: 3.5673, lng: 98.6533 },
  outlet_tebet: { lat: 3.5701, lng: 98.6258 },
};

function isValidGeoPoint(coord) {
  if (!coord || typeof coord.latitude !== "number" || typeof coord.longitude !== "number") {
    return false;
  }
  if (Number.isNaN(coord.latitude) || Number.isNaN(coord.longitude)) {
    return false;
  }
  if (coord.latitude < -90 || coord.latitude > 90 || coord.longitude < -180 || coord.longitude > 180) {
    return false;
  }
  if (Math.abs(coord.latitude) < 1e-6 && Math.abs(coord.longitude) < 1e-6) {
    return false;
  }
  return true;
}

/**
 * Migration 011:
 * Memastikan seluruh dokumen koleksi `outlets` memiliki field `coordinate` bertipe GeoPoint valid.
 * Jika kosong, tidak valid, atau bernilai (0, 0), diisi dengan koordinat Medan (default pusat Kota Medan).
 */
async function up(db, admin) {
  const GeoPoint = admin.firestore.GeoPoint;
  const snap = await db.collection(COLLECTIONS.OUTLETS).get();
  if (snap.empty) return;

  const chunkSize = 450;
  const docs = snap.docs;

  for (let i = 0; i < docs.length; i += chunkSize) {
    const chunk = docs.slice(i, i + chunkSize);
    const batch = db.batch();
    let updated = 0;

    chunk.forEach((doc) => {
      const data = doc.data();
      if (!isValidGeoPoint(data.coordinate)) {
        const target = KNOWN_MEDAN_OUTLET_COORDS[doc.id] || DEFAULT_MEDAN_COORDINATE;
        batch.update(doc.ref, {
          coordinate: new GeoPoint(target.lat, target.lng),
        });
        updated++;
      }
    });

    if (updated > 0) {
      await batch.commit();
    }
  }
}

async function down() {
  // Safe no-op: do not delete valid outlet coordinates on rollback.
}

module.exports = {
  up,
  down,
};
