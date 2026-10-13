package com.sudutcorp.sudutbulat.feature.auth

import androidx.lifecycle.ViewModel
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val loginInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginChanged(value: String) {
        _uiState.update { it.copy(loginInput = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val loginText = _uiState.value.loginInput.trim()
        val passwordText = _uiState.value.passwordInput

        if (loginText.isBlank() || passwordText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kode Login/Email dan password wajib diisi.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        repository.loginWithCodeOrEmail(
            login = loginText,
            password = passwordText,
            onSuccess = {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            },
            onError = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
