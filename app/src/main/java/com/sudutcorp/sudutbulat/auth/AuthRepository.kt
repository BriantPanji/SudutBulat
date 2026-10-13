package com.sudutcorp.sudutbulat.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    firebaseAuth: FirebaseAuth? = null,
    firestore: FirebaseFirestore? = null
) {
    private val auth: FirebaseAuth? by lazy {
        firebaseAuth ?: runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun loginWithCodeOrEmail(
        login: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanLogin = login.trim()

        if (cleanLogin.isBlank() || password.isBlank()) {
            onError("Email/Kode Login dan password wajib diisi.")
            return
        }

        val currentAuth = auth
        if (currentAuth == null) {
            onError("Layanan autentikasi tidak tersedia.")
            return
        }

        if (isEmail(cleanLogin)) {
            signInWithEmail(
                authInstance = currentAuth,
                email = cleanLogin,
                password = password,
                onSuccess = onSuccess,
                onError = onError
            )
        } else {
            val currentDb = db
            if (currentDb == null) {
                onError("Layanan database tidak tersedia.")
                return
            }

            val loginCode = cleanLogin.lowercase()
            currentDb.collection("logincode")
                .document(loginCode)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        onError("Email/Kode Login atau password salah.")
                        return@addOnSuccessListener
                    }

                    val email = document.getString("email")

                    if (email.isNullOrBlank()) {
                        onError("Data akun tidak valid.")
                        return@addOnSuccessListener
                    }

                    signInWithEmail(
                        authInstance = currentAuth,
                        email = email,
                        password = password,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
                .addOnFailureListener {
                    onError("Gagal menghubungi server. Coba lagi.")
                }
        }
    }

    private fun isEmail(input: String): Boolean {
        return emailRegex.matches(input)
    }

    private fun signInWithEmail(
        authInstance: FirebaseAuth,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authInstance.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError("Email/Kode Login atau password salah.")
            }
    }
}