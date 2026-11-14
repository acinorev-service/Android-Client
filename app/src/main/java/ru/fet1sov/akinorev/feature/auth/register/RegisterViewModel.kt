package ru.fet1sov.akinorev.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.feature.auth.AuthMessage
import ru.fet1sov.akinorev.feature.auth.AuthValidation
import ru.fet1sov.akinorev.feature.auth.toAuthMessage

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isRepeatPasswordVisible: Boolean = false,
    val isInitializing: Boolean = true,
    val isSubmitting: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: AuthMessage? = null,
) {
    val canSubmit: Boolean
        get() = !isInitializing && !isSubmitting &&
            username.isNotBlank() && password.isNotEmpty() && repeatPassword.isNotEmpty()
}

class RegisterViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.session.collect { state ->
                mutableUiState.update {
                    it.copy(
                        isInitializing = state is SessionState.Loading,
                        isAuthenticated = state is SessionState.Authorized,
                    )
                }
            }
        }
        viewModelScope.launch { repository.restoreSession() }
    }

    fun onUsernameChange(value: String) {
        mutableUiState.update {
            it.copy(username = AuthValidation.sanitizeUsername(value), error = null)
        }
    }

    fun onEmailChange(value: String) {
        mutableUiState.update { it.copy(email = value.trim(), error = null) }
    }

    fun onPasswordChange(value: String) {
        mutableUiState.update { it.copy(password = value, error = null) }
    }

    fun onRepeatPasswordChange(value: String) {
        mutableUiState.update { it.copy(repeatPassword = value, error = null) }
    }

    fun onTogglePasswordVisibility() {
        mutableUiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleRepeatPasswordVisibility() {
        mutableUiState.update { it.copy(isRepeatPasswordVisible = !it.isRepeatPasswordVisible) }
    }

    fun onSubmit() {
        val state = mutableUiState.value
        if (state.isSubmitting || state.isInitializing) return

        val username = state.username.trim()
        val email = state.email.trim()

        val validationError = when {
            !AuthValidation.isUsernameValid(username) -> AuthMessage.InvalidUsername
            !AuthValidation.isPasswordValid(state.password) -> AuthMessage.ShortPassword
            state.password != state.repeatPassword -> AuthMessage.PasswordMismatch
            email.isNotEmpty() && !AuthValidation.isEmailValid(email) -> AuthMessage.InvalidEmail
            else -> null
        }
        if (validationError != null) {
            mutableUiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            mutableUiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.register(username, email, state.password)) {
                is AuthResult.Success -> mutableUiState.update { it.copy(isSubmitting = false) }
                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(isSubmitting = false, error = result.error.toAuthMessage())
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                RegisterViewModel(application.container.authRepository)
            }
        }
    }
}
