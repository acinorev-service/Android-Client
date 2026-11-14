package ru.fet1sov.akinorev.feature.push

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.push.PushRegistrar

class PushTokenViewModel(
    private val authRepository: AuthRepository,
    private val pushRegistrar: PushRegistrar,
) : ViewModel() {
    init {
        viewModelScope.launch {
            authRepository.session.collect { state ->
                if (state is SessionState.Authorized) {
                    pushRegistrar.sync()
                }
            }
        }
    }

    fun refreshToken() {
        viewModelScope.launch { pushRegistrar.sync() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                PushTokenViewModel(
                    authRepository = application.container.authRepository,
                    pushRegistrar = application.container.pushRegistrar,
                )
            }
        }
    }
}
