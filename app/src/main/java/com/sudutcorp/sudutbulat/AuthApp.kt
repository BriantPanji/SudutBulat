package com.sudutcorp.sudutbulat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthApp(auth: FirebaseAuth) {
    var userEmail by remember {
        mutableStateOf(auth.currentUser?.email)
    }

    // Memantau perubahan status login Firebase.
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            userEmail = firebaseAuth.currentUser?.email
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (userEmail != null) {
        HomeScreen(
            email = userEmail.orEmpty(),
            onLogout = { auth.signOut() }
        )
    } else {
        LoginScreen(auth = auth)
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSignUp) "Buat Akun" else "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = ""
            },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                message = ""
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val cleanEmail = email.trim()

                if (cleanEmail.isBlank() || password.isBlank()) {
                    message = "Email dan password wajib diisi."
                } else if (isSignUp && password.length < 6) {
                    message = "Password minimal 6 karakter."
                } else {
                    isLoading = true
                    message = ""

                    val task = if (isSignUp) {
                        auth.createUserWithEmailAndPassword(
                            cleanEmail,
                            password
                        )
                    } else {
                        auth.signInWithEmailAndPassword(
                            cleanEmail,
                            password
                        )
                    }

                    task.addOnCompleteListener { result ->
                        isLoading = false

                        if (result.isSuccessful) {
                            // AuthStateListener akan membuka Home.
                            message = ""
                        } else {
                            message = result.exception?.localizedMessage
                                ?: "Autentikasi gagal."
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isSignUp) "Daftar" else "Login"
            )
        }

        if (isLoading) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                isSignUp = !isSignUp
                message = ""
            },
            enabled = !isLoading
        ) {
            Text(
                if (isSignUp) {
                    "Sudah punya akun? Login"
                } else {
                    "Belum punya akun? Daftar"
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    email: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selamat Datang!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        Text(text = "Kamu berhasil login.")

        Spacer(Modifier.height(8.dp))

        Text(text = email)

        Spacer(Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}