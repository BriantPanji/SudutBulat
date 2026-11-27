const { COLLECTIONS, batchUpsert } = require("../scripts/helpers");

const COLLECTION = COLLECTIONS.MATERIAL_CATEGORIES;

const data = [
  {
    id: "cat_bumbu",
    name: "Bumbu",
    name_lower: "bumbu",
  },
  {
    id: "cat_saus",
    name: "Saus",
    name_lower: "saus",
  },
  {
    id: "cat_tahu",
    name: "Tahu",
    name_lower: "tahu",
  },
];

async function seed(db) {
  const count = await batchUpsert(db, COLLECTION, data);
  console.log(`  [OK] ${COLLECTION}: ${count} kategori material berhasil di-seed.`);
  return count;
}

module.exports = {
  name: "material_categories",
  collection: COLLECTION,
  data,
  seed,
};
