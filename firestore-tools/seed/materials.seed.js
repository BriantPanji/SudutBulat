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
    is_active: true,
  },
  {
    id: "mat_tahu_pedas",
    name: "Tahu Bulat Pedas",
    cost: 600,
    unit: "pcs",
    is_active: true,
  },
  {
    id: "mat_tahu_keju",
    name: "Tahu Bulat Keju",
    cost: 700,
    unit: "pcs",
    is_active: true,
  },
  {
    id: "mat_saus_sambal",
    name: "Saus Sambal",
    cost: 3000,
    unit: "pcs",
    is_active: true,
  },
  {
    id: "mat_chilly_oil",
    name: "Chilly Oil (Botol)",
    cost: 15000,
    unit: "unit",
    is_active: true,
  },
  {
    id: "mat_bumbu_balado",
    name: "Bubuk Bumbu Balado",
    cost: 50,
    unit: "gr",
    is_active: true,
  },
  {
    id: "mat_bumbu_bbq",
    name: "Bubuk Bumbu BBQ",
    cost: 50,
    unit: "gr",
    is_active: true,
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
