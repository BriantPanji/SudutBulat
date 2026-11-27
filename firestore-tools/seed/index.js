const fs = require("fs");
const path = require("path");

/**
 * Urutan default eksekusi seeder agar relasi foreign key konsisten:
 * 1. outlets
 * 2. users (merujuk ke outlet_id)
 * 3. materials (katalog stok bahan mentah)
 * 4. products (katalog produk siap jual)
 * 5. distributions (merujuk ke outlets, users, materials)
 * 6. daily_reports (merujuk ke outlets, products, materials)
 */
const DEFAULT_ORDER = [
  "outlets.seed.js",
  "users.seed.js",
  "material_categories.seed.js",
  "materials.seed.js",
  "products.seed.js",
  "distributions.seed.js",
  "daily_reports.seed.js",
];

function getSeederFiles(targetName) {
  const allFiles = fs
    .readdirSync(__dirname)
    .filter((f) => f.endsWith(".seed.js"));

  if (targetName) {
    const clean = targetName.toLowerCase().replace(/\.seed\.js$|\.js$/i, "");
    const matched = allFiles.filter(
      (f) => f.toLowerCase() === `${clean}.seed.js`
    );
    if (matched.length === 0) {
      throw new Error(
        `Seeder "${targetName}" tidak ditemukan di folder seed/. Pilihan yang ada: ${allFiles.join(", ")}`
      );
    }
    return matched;
  }

  const ordered = DEFAULT_ORDER.filter((f) => allFiles.includes(f));
  const extras = allFiles.filter((f) => !DEFAULT_ORDER.includes(f)).sort();
  return [...ordered, ...extras];
}

async function runAll(db, admin, auth, targetName) {
  const files = getSeederFiles(targetName);
  for (const file of files) {
    const seeder = require(path.join(__dirname, file));
    if (typeof seeder.seed === "function") {
      await seeder.seed(db, admin, auth);
    }
  }
}

module.exports = {
  runAll,
};
