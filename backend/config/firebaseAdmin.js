
const admin = require("firebase-admin");

let firebaseApp = null;

function getFirebaseAdmin() {
    if (firebaseApp) {
        return admin;
    }

    const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;

    if (!serviceAccountJson) {
        throw new Error(
            "Firebase Admin is not configured. Set FIREBASE_SERVICE_ACCOUNT_JSON."
        );
    }

    const serviceAccount = JSON.parse(serviceAccountJson);

    if (serviceAccount.private_key) {
        serviceAccount.private_key =
            serviceAccount.private_key.replace(/\\n/g, "\n");
    }

    firebaseApp = admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });

    return admin;
}

async function verifyFirebaseIdToken(idToken) {
    const firebaseAdmin = getFirebaseAdmin();

    return firebaseAdmin.auth().verifyIdToken(idToken);
}

module.exports = {
    verifyFirebaseIdToken
};
