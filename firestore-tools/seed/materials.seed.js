const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

const COLLECTION = COLLECTIONS.MATERIALS;

/**
 * Sesuai PRD.md:
 * - Material adalah STOK (barang/bahan mentah dari gudang, bukan produk siap jual).
 * - Gudang TIDAK mencatat jumlah sisa stok di gudang; koleksi ini adalah Katalog Master Bahan Mentah.
 * - Field `unit` sesuai UnitType.kt (@PropertyName: "pcs", "gr", "ml", "unit").
 */
const data = [
  {
    id: "mat_tahu_original",
    name: "Tahu Bulat Original",
    cost: 500,
    unit: "pcs",
  },
  {
    id: "mat_tahu_pedas",
    name: "Tahu Bulat Pedas",
    cost: 600,
    unit: "pcs",
  },
  {
    id: "mat_tahu_keju",
    name: "Tahu Bulat Keju",
    cost: 700,
    unit: "pcs",
  },
  {
    id: "mat_saus_sambal",
    name: "Saus Sambal",
    cost: 3000,
    unit: "pcs",
  },
  {
    id: "mat_chilly_oil",
    name: "Chilly Oil (Botol)",
    cost: 15000,
    unit: "unit",
  },
  {
    id: "mat_bumbu_balado",
    name: "Bubuk Bumbu Balado",
    cost: 50,
    unit: "gr",
  },
  {
    id: "mat_bumbu_bbq",
    name: "Bubuk Bumbu BBQ",
    cost: 50,
    unit: "gr",
  },
];

async function seed(db) {
  const count = await batchUpsert(db, COLLECTION, data);
  console.log(`  [OK] ${COLLECTION}: ${count} katalog bahan mentah (stok) berhasil di-seed.`);
  return count;
}

module.exports = {
  name: "materials",
  collection: COLLECTION,
  data,
  seed,
};
