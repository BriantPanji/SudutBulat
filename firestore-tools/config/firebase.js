const fs = require("fs");
const path = require("path");
const dotenv = require("dotenv");
const admin = require("firebase-admin");

const ROOT_DIR = path.resolve(__dirname, "../..");
const ROOT_ENV_PATH = path.join(ROOT_DIR, ".env");

dotenv.config({ path: ROOT_ENV_PATH });

function resolveCredential() {
  if (process.env.FIRESTORE_EMULATOR_HOST) {
    return null;
  }

  const serviceAccountPathEnv = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (serviceAccountPathEnv) {
    const resolvedPath = path.isAbsolute(serviceAccountPathEnv)
      ? serviceAccountPathEnv
      : path.resolve(ROOT_DIR, serviceAccountPathEnv);

    if (fs.existsSync(resolvedPath)) {
      const serviceAccount = JSON.parse(fs.readFileSync(resolvedPath, "utf8"));
      return admin.credential.cert(serviceAccount);
    }
  }

  const projectId = process.env.FIREBASE_PROJECT_ID;
  const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
  const privateKey = process.env.FIREBASE_PRIVATE_KEY
    ? process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, "\n")
    : undefined;

  if (projectId && clientEmail && privateKey && !privateKey.includes("ISI_PRIVATE_KEY_DI_SINI")) {
    return admin.credential.cert({
      projectId,
      clientEmail,
      privateKey,
    });
  }

  throw new Error(
    [
      "Kredensial Firebase Admin belum dikonfigurasi!",
      `Pastikan file .env di root project (${ROOT_ENV_PATH}) sudah diisi:`,
      "  1. Taruh file serviceAccountKey.json di root project (FIREBASE_SERVICE_ACCOUNT_PATH=./serviceAccountKey.json), ATAU",
      "  2. Isi FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL, dan FIREBASE_PRIVATE_KEY di .env, ATAU",
      "  3. Isi FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 jika menggunakan emulator lokal.",
    ].join("\n")
  );
}

let initialized = false;

function getFirebase() {
  if (!initialized) {
    const credential = resolveCredential();
    const projectId = process.env.FIREBASE_PROJECT_ID || "pemobb-9f67e";

    admin.initializeApp(
      credential
        ? { credential, projectId }
        : { projectId }
    );
    initialized = true;
  }

  return {
    admin,
    db: admin.firestore(),
    auth: admin.auth(),
  };
}

module.exports = {
  getFirebase,
  ROOT_DIR,
  ROOT_ENV_PATH,
};
