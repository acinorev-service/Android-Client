package ru.fet1sov.akinorev.feature.presence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket

class PresenceViewModel(
    private val authRepository: AuthRepository,
    private val realtimeSocket: RealtimeSocket,
) : ViewModel() {
    val isOnline: StateFlow<Boolean> = realtimeSocket.isConnected

    private var active = false

    init {
        viewModelScope.launch {
            authRepository.session.collect { state ->
                when (state) {
                    is SessionState.Authorized -> if (active) realtimeSocket.connect()
                    SessionState.Guest -> realtimeSocket.disconnect()
                    SessionState.Loading -> Unit
                }
            }
        }
    }

    fun start() {
        active = true
        if (authRepository.session.value is SessionState.Authorized) {
            realtimeSocket.connect()
        }
    }

    fun stop() {
        active = false
        realtimeSocket.disconnect()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                PresenceViewModel(
                    authRepository = application.container.authRepository,
                    realtimeSocket = application.container.realtimeSocket,
                )
            }
        }
    }
}
