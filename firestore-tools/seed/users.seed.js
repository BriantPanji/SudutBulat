const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

const COLLECTION = COLLECTIONS.USERS;

/**
 * Sesuai PRD.md & AuthRepository.kt:
 * - ID dokumen adalah loginCode (dev, owner, gudang, gudang2, staff, staff2, dst.)
 * - Role disimpan lowercase ("dev", "owner", "warehouse", "outlet") sesuai @PropertyName di Role.kt
 * - Gudang hanya ada 1 secara fisik, tetapi memiliki >= 2 pegawai (gudang & gudang2)
 * - Setiap staf outlet hanya bertugas di 1 outlet_id, namun 1 outlet bisa memiliki beberapa staf (staff & staff2 di outlet_sudirman)
 */
const data = [
  {
    id: "dev",
    name: "Developer SudutBulat",
    email: "sdb.dev@gmail.com",
    username: "dev",
    role: "dev",
    outlet_id: null,
    is_active: true,
  },
  {
    id: "owner",
    name: "Hendra Wijaya (Owner)",
    email: "sdb.owner@gmail.com",
    username: "owner",
    role: "owner",
    outlet_id: null,
    is_active: true,
  },
  {
    id: "gudang",
    name: "Budi Santoso (Admin Gudang 1)",
    email: "sdb.gudang@gmail.com",
    username: "gudang",
    role: "warehouse",
    outlet_id: null,
    is_active: true,
  },
  {
    id: "gudang2",
    name: "Riko Pratama (Admin Gudang 2)",
    email: "sdb.gudang2@gmail.com",
    username: "gudang2",
    role: "warehouse",
    outlet_id: null,
    is_active: true,
  },
  {
    id: "staff",
    name: "Dimas Prasetyo",
    email: "sdb.staff@gmail.com",
    username: "staff",
    role: "outlet",
    outlet_id: "outlet_sudirman",
    is_active: true,
  },
  {
    id: "staff2",
    name: "Andi Saputra",
    email: "sdb.staff2@gmail.com",
    username: "staff2",
    role: "outlet",
    outlet_id: "outlet_sudirman",
    is_active: true,
  },
  {
    id: "staff_gm",
    name: "Siti Rahma",
    email: "sdb.staff.gm@gmail.com",
    username: "staff_gm",
    role: "outlet",
    outlet_id: "outlet_gajah_mada",
    is_active: true,
  },
  {
    id: "staff_cikini",
    name: "Fajar Nugroho",
    email: "sdb.staff.cikini@gmail.com",
    username: "staff_cikini",
    role: "outlet",
    outlet_id: "outlet_cikini",
    is_active: true,
  },
  {
    id: "staff_tebet",
    name: "Reza Mahendra",
    email: "sdb.staff.tebet@gmail.com",
    username: "staff_tebet",
    role: "outlet",
    outlet_id: "outlet_tebet",
    is_active: true,
  },
];

async function syncFirebaseAuthUsers(auth) {
  const shouldSync = String(process.env.SYNC_FIREBASE_AUTH || "true").toLowerCase() === "true";
  if (!shouldSync || !auth) return;

  const defaultPassword = process.env.DEFAULT_USER_PASSWORD || "password";
  let synced = 0;

  for (const user of data) {
    try {
      await auth.getUserByEmail(user.email);
      synced++;
    } catch (err) {
      if (err && err.code === "auth/user-not-found") {
        try {
          await auth.createUser({
            uid: user.id,
            email: user.email,
            password: defaultPassword,
            displayName: user.name,
            disabled: !user.is_active,
          });
          synced++;
        } catch (createErr) {
          console.warn(`  [WARN] Gagal membuat user Firebase Auth (${user.email}): ${createErr.message}`);
        }
      }
    }
  }

  console.log(`  [OK] Firebase Auth: ${synced}/${data.length} akun login disinkronkan (default password: "${defaultPassword}").`);
}

async function seed(db, admin, auth) {
  const count = await batchUpsert(db, COLLECTION, data);
  console.log(`  [OK] ${COLLECTION}: ${count} akun user berhasil di-seed.`);
  await syncFirebaseAuthUsers(auth);
  return count;
}

module.exports = {
  name: "users",
  collection: COLLECTION,
  data,
  seed,
};
