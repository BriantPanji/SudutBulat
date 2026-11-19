const fs = require("fs");
const path = require("path");
const { getFirebase } = require("../config/firebase");
const { COLLECTIONS, batchUpsert, clearCollection } = require("./helpers");

const SEED_DIR = path.resolve(__dirname, "../seed");
const MIGRATIONS_DIR = path.resolve(__dirname, "../migrations");

function sanitizeName(raw) {
  return String(raw || "")
    .trim()
    .toLowerCase()
    .replace(/\.seed\.js$|\.js$/i, "")
    .replace(/[^a-z0-9_]+/g, "_")
    .replace(/^_+|_+$/g, "");
}

function handleMakeSeed(rawName) {
  const name = sanitizeName(rawName);
  if (!name) {
    console.error("Usage: npm run make:seed <nama_koleksi>");
    process.exit(1);
  }

  fs.mkdirSync(SEED_DIR, { recursive: true });
  const fileName = `${name}.seed.js`;
  const filePath = path.join(SEED_DIR, fileName);

  if (fs.existsSync(filePath)) {
    console.error(`File seeder sudah ada: seed/${fileName}`);
    process.exit(1);
  }

  const template = `const { batchUpsert } = require("../scripts/helpers");

const COLLECTION = "${name}";

const data = [
  // {
  //   id: "${name}_1",
  //   name: "Contoh ${name}",
  //   is_active: true,
  // },
];

/**
 * Seeder untuk collection \`${name}\`
 * @param {FirebaseFirestore.Firestore} db
 * @param {typeof import("firebase-admin")} admin
 */
async function seed(db, admin) {
  if (data.length === 0) {
    console.log(\`  [SKIP] \${COLLECTION}: belum ada data di seed/${fileName}\`);
    return 0;
  }
  const count = await batchUpsert(db, COLLECTION, data);
  console.log(\`  [OK] \${COLLECTION}: \${count} dokumen berhasil di-seed.\`);
  return count;
}

module.exports = {
  name: "${name}",
  collection: COLLECTION,
  data,
  seed,
};
`;

  fs.writeFileSync(filePath, template, "utf8");
  console.log(`Seeder berhasil dibuat: firestore-tools/seed/${fileName}`);
}

function handleMakeMigration(rawName) {
  const name = sanitizeName(rawName);
  if (!name) {
    console.error("Usage: npm run make:migrations <nama_migrasi>");
    process.exit(1);
  }

  fs.mkdirSync(MIGRATIONS_DIR, { recursive: true });
  const existingFiles = fs
    .readdirSync(MIGRATIONS_DIR)
    .filter((f) => /^\d{3}_.+\.js$/.test(f))
    .sort();

  let nextNumber = 1;
  if (existingFiles.length > 0) {
    const lastFile = existingFiles[existingFiles.length - 1];
    const lastNum = parseInt(lastFile.slice(0, 3), 10);
    if (!Number.isNaN(lastNum)) {
      nextNumber = lastNum + 1;
    }
  }

  const prefix = String(nextNumber).padStart(3, "0");
  const fileName = `${prefix}_${name}.js`;
  const filePath = path.join(MIGRATIONS_DIR, fileName);

  const template = `/**
 * Migration: ${fileName}
 */

/**
 * Jalankan perubahan skema/dokumen (UP)
 * @param {FirebaseFirestore.Firestore} db
 * @param {typeof import("firebase-admin")} admin
 */
async function up(db, admin) {
  // Contoh: menambahkan field baru ke semua dokumen di sebuah collection
  // const snapshot = await db.collection("materials").get();
  // const batch = db.batch();
  // snapshot.docs.forEach((doc) => {
  //   batch.update(doc.ref, { updated_field: true });
  // });
  // await batch.commit();
}

/**
 * Batalkan perubahan skema/dokumen (DOWN / Rollback)
 * @param {FirebaseFirestore.Firestore} db
 * @param {typeof import("firebase-admin")} admin
 */
async function down(db, admin) {
  // Contoh: menghapus field saat rollback
  // const snapshot = await db.collection("materials").get();
  // const batch = db.batch();
  // snapshot.docs.forEach((doc) => {
  //   batch.update(doc.ref, { updated_field: admin.firestore.FieldValue.delete() });
  // });
  // await batch.commit();
}

module.exports = {
  up,
  down,
};
`;

  fs.writeFileSync(filePath, template, "utf8");
  console.log(`Migration berhasil dibuat: firestore-tools/migrations/${fileName}`);
}

async function handleSeed(targetArg) {
  const { db, auth, admin } = getFirebase();
  const seedIndex = require("../seed");

  console.log("=== Menjalankan Firestore Seeder (SudutBulat) ===");
  await seedIndex.runAll(db, admin, auth, targetArg);
  console.log("=== Seeding Selesai ===");
}

function getMigrationFiles() {
  if (!fs.existsSync(MIGRATIONS_DIR)) return [];
  return fs
    .readdirSync(MIGRATIONS_DIR)
    .filter((f) => f.endsWith(".js"))
    .sort();
}

async function handleMigrate() {
  const { db, admin } = getFirebase();
  const files = getMigrationFiles();

  if (files.length === 0) {
    console.log("Tidak ada file migrasi di folder migrations/.");
    return;
  }

  const migCol = db.collection(COLLECTIONS.MIGRATIONS);
  const snapshot = await migCol.get();
  const executedMap = new Map();
  let maxBatch = 0;

  snapshot.docs.forEach((doc) => {
    const data = doc.data();
    executedMap.set(doc.id, data);
    if (typeof data.batch === "number" && data.batch > maxBatch) {
      maxBatch = data.batch;
    }
  });

  const pendingFiles = files.filter((f) => !executedMap.has(f));
  if (pendingFiles.length === 0) {
    console.log("Nothing to migrate. Semua migrasi sudah dijalankan.");
    return;
  }

  const currentBatch = maxBatch + 1;
  console.log(`=== Menjalankan Migrasi (Batch #${currentBatch}) ===`);

  for (const file of pendingFiles) {
    const migration = require(path.join(MIGRATIONS_DIR, file));
    if (typeof migration.up !== "function") {
      throw new Error(`File migrasi ${file} tidak mengekspor fungsi up(db, admin).`);
    }

    process.stdout.write(`  Migrating: ${file} ... `);
    await migration.up(db, admin);
    await migCol.doc(file).set({
      name: file,
      batch: currentBatch,
      migrated_at: admin.firestore.FieldValue.serverTimestamp(),
    });
    console.log("DONE");
  }

  console.log(`=== ${pendingFiles.length} Migrasi Berhasil Dijalankan ===`);
}

async function handleMigrateRollback() {
  const { db, admin } = getFirebase();
  const migCol = db.collection(COLLECTIONS.MIGRATIONS);
  const snapshot = await migCol.get();

  if (snapshot.empty) {
    console.log("Belum ada migrasi yang pernah dijalankan.");
    return;
  }

  let maxBatch = 0;
  const records = snapshot.docs.map((doc) => {
    const data = doc.data();
    if (typeof data.batch === "number" && data.batch > maxBatch) {
      maxBatch = data.batch;
    }
    return { id: doc.id, ...data };
  });

  const lastBatchRecords = records
    .filter((r) => r.batch === maxBatch)
    .sort((a, b) => b.id.localeCompare(a.id));

  console.log(`=== Rollback Migrasi (Batch #${maxBatch}) ===`);

  for (const rec of lastBatchRecords) {
    const filePath = path.join(MIGRATIONS_DIR, rec.id);
    if (fs.existsSync(filePath)) {
      const migration = require(filePath);
      if (typeof migration.down === "function") {
        process.stdout.write(`  Rolling back: ${rec.id} ... `);
        await migration.down(db, admin);
        console.log("DONE");
      }
    }
    await migCol.doc(rec.id).delete();
  }

  console.log(`=== Rollback Batch #${maxBatch} Selesai ===`);
}

async function handleMigrateStatus() {
  const { db } = getFirebase();
  const files = getMigrationFiles();
  const snapshot = await db.collection(COLLECTIONS.MIGRATIONS).get();
  const executedMap = new Map();

  snapshot.docs.forEach((doc) => {
    executedMap.set(doc.id, doc.data());
  });

  console.log("=== Status Migrasi Firestore ===");
  if (files.length === 0) {
    console.log("Belum ada file migrasi.");
    return;
  }

  for (const file of files) {
    const info = executedMap.get(file);
    if (info) {
      console.log(`  [RAN]     ${file} (Batch #${info.batch})`);
    } else {
      console.log(`  [PENDING] ${file}`);
    }
  }
}

async function handleMigrateFresh(extraArgs) {
  const { db } = getFirebase();
  console.log("=== Menghapus Seluruh Koleksi Firestore (migrate:fresh) ===");

  const allCollections = Object.values(COLLECTIONS);
  for (const colName of allCollections) {
    const deletedCount = await clearCollection(db, colName);
    console.log(`  [CLEARED] ${colName}: ${deletedCount} dokumen dihapus.`);
  }

  await handleMigrate();

  if (extraArgs.includes("--seed") || extraArgs.includes("seed")) {
    await handleSeed();
  }
}

async function main() {
  const [, , command, arg1, ...restArgs] = process.argv;

  try {
    switch (command) {
      case "make:seed":
        handleMakeSeed(arg1);
        break;
      case "make:migration":
      case "make:migrations":
        handleMakeMigration(arg1);
        break;
      case "seed":
        await handleSeed(arg1);
        break;
      case "migrate":
        await handleMigrate();
        break;
      case "migrate:rollback":
        await handleMigrateRollback();
        break;
      case "migrate:status":
        await handleMigrateStatus();
        break;
      case "migrate:fresh":
        await handleMigrateFresh([arg1, ...restArgs].filter(Boolean));
        break;
      default:
        console.log("SudutBulat Firestore Tools CLI:");
        console.log("  npm run make:seed <name>        Buat file seeder baru di seed/<name>.seed.js");
        console.log("  npm run make:migrations <name>  Buat file migrasi baru di migrations/NNN_<name>.js");
        console.log("  npm run seed [name]             Jalankan semua seeder (atau 1 seeder spesifik)");
        console.log("  npm run migrate                 Jalankan semua migrasi yang belum dieksekusi");
        console.log("  npm run migrate:rollback        Rollback batch migrasi terakhir");
        console.log("  npm run migrate:status          Lihat status migrasi");
        console.log("  npm run migrate:fresh [--seed]  Bersihkan seluruh koleksi, jalankan migrasi (& seeder)");
        break;
    }
  } catch (err) {
    console.error("\n[ERROR]", err.message || err);
    process.exit(1);
  }
}

main();
