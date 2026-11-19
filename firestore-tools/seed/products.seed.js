const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

const COLLECTION = COLLECTIONS.PRODUCTS;

/**
 * Sesuai PRD.md:
 * - Product adalah barang matang siap jual di outlet yang memiliki harga jual (`price`).
 * - Dijual dalam bentuk satuan maupun bungkus (misal 5 pcs/bungkus).
 */
const data = [
  {
    id: "prod_tahu_ori_satuan",
    name: "Tahu Bulat Original (Satuan)",
    price: 1000,
    is_active: true,
  },
  {
    id: "prod_tahu_ori_bungkus",
    name: "Tahu Bulat Original (Bungkus 5 pcs)",
    price: 5000,
    is_active: true,
  },
  {
    id: "prod_tahu_pedas_satuan",
    name: "Tahu Bulat Pedas (Satuan)",
    price: 1200,
    is_active: true,
  },
  {
    id: "prod_tahu_pedas_bungkus",
    name: "Tahu Bulat Pedas (Bungkus 5 pcs)",
    price: 6000,
    is_active: true,
  },
  {
    id: "prod_tahu_keju_satuan",
    name: "Tahu Bulat Keju (Satuan)",
    price: 1500,
    is_active: true,
  },
  {
    id: "prod_tahu_keju_bungkus",
    name: "Tahu Bulat Keju (Bungkus 5 pcs)",
    price: 7000,
    is_active: true,
  },
  {
    id: "prod_addon_chilly_oil",
    name: "Extra Chilly Oil Cup",
    price: 2000,
    is_active: true,
  },
];

async function seed(db) {
  const count = await batchUpsert(db, COLLECTION, data);
  console.log(`  [OK] ${COLLECTION}: ${count} katalog produk siap jual berhasil di-seed.`);
  return count;
}

module.exports = {
  name: "products",
  collection: COLLECTION,
  data,
  seed,
};
