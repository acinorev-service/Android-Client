package ru.fet1sov.akinorev.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.notifications.NotificationsRepository
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket
import ru.fet1sov.akinorev.data.remote.AcinorevJson
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.NotificationDto
import ru.fet1sov.akinorev.data.notifications.toDomain
import ru.fet1sov.akinorev.data.settings.AppSettingsRepository
import ru.fet1sov.akinorev.model.AppNotification
import ru.fet1sov.akinorev.model.NotificationActor
import ru.fet1sov.akinorev.model.NotificationType
import ru.fet1sov.akinorev.model.localizedBody
import ru.fet1sov.akinorev.model.localizedTitle
import java.util.Locale

data class NotificationUi(
    val id: String,
    val type: NotificationType,
    val actor: NotificationActor?,
    val actorId: String,
    val actorUsername: String,
    val postId: String?,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val createdAtMillis: Long,
)

data class NotificationsUiState(
    val isAuthPending: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<NotificationUi> = emptyList(),
    val unread: Int = 0,
    val hasMore: Boolean = false,
)

class NotificationsViewModel(
    private val authRepository: AuthRepository,
    private val notificationsRepository: NotificationsRepository,
    private val realtimeSocket: RealtimeSocket,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = mutableUiState.asStateFlow()

    private var loaded = false
    private val pendingRead = mutableSetOf<String>()
    private var readJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.session.collect { state ->
                val authorized = state is SessionState.Authorized
                mutableUiState.update {
                    it.copy(
                        isAuthPending = state is SessionState.Loading,
                        isAuthenticated = authorized,
                    )
                }
                when {
                    authorized && !loaded -> {
                        loaded = true
                        refresh()
                    }

                    state is SessionState.Guest -> {
                        loaded = false
                        mutableUiState.value = NotificationsUiState(isAuthPending = false)
                    }
                }
            }
        }

        viewModelScope.launch { authRepository.restoreSession() }

        viewModelScope.launch {
            realtimeSocket.events.collect { event ->
                if (event.type == NotificationEventType) onIncoming(event.payload)
            }
        }
    }

    fun refresh() {
        if (!mutableUiState.value.isAuthenticated) return
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = it.items.isEmpty()) }
            when (val result = notificationsRepository.fetchPage(limit = PageSize, offset = 0)) {
                is AuthResult.Success -> mutableUiState.update {
                    it.copy(
                        items = result.value.items.map(::toUi),
                        hasMore = result.value.hasMore,
                        unread = result.value.unread,
                        isLoading = false,
                    )
                }

                is AuthResult.Failure -> mutableUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadMore() {
        val state = mutableUiState.value
        if (!state.isAuthenticated || state.isLoading || state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingMore = true) }
            val offset = mutableUiState.value.items.size
            when (val result = notificationsRepository.fetchPage(limit = PageSize, offset = offset)) {
                is AuthResult.Success -> mutableUiState.update { current ->
                    val known = current.items.mapTo(HashSet()) { it.id }
                    val next = result.value.items
                        .filter { it.id !in known }
                        .map(::toUi)
                    current.copy(
                        items = current.items + next,
                        hasMore = result.value.hasMore,
                        isLoadingMore = false,
                    )
                }

                is AuthResult.Failure -> mutableUiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun markRead(ids: List<String>) {
        if (!mutableUiState.value.isAuthenticated || ids.isEmpty()) return

        val target = ids.toSet()
        val unreadIds = mutableUiState.value.items
            .filter { it.id in target && !it.isRead }
            .map { it.id }
        if (unreadIds.isEmpty()) return

        val unreadSet = unreadIds.toHashSet()
        mutableUiState.update { current ->
            current.copy(
                items = current.items.map { if (it.id in unreadSet) it.copy(isRead = true) else it },
                unread = (current.unread - unreadSet.size).coerceAtLeast(0),
            )
        }

        pendingRead.addAll(unreadIds)
        if (readJob?.isActive == true) return

        readJob = viewModelScope.launch {
            delay(ReadBatchDelayMillis)
            val batch = pendingRead.toList()
            pendingRead.clear()
            notificationsRepository.markRead(batch)
        }
    }

    fun markAllRead() {
        if (!mutableUiState.value.isAuthenticated || mutableUiState.value.unread == 0) return

        viewModelScope.launch {
            when (notificationsRepository.markAllRead()) {
                is AuthResult.Success -> mutableUiState.update { current ->
                    current.copy(
                        items = current.items.map { it.copy(isRead = true) },
                        unread = 0,
                    )
                }

                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun onIncoming(payload: JsonElement?) {
        val raw = payload?.toString() ?: return
        val notification = runCatching {
            AcinorevJson.instance.decodeFromString(NotificationDto.serializer(), raw)
        }.getOrNull()?.toDomain() ?: return

        if (mutableUiState.value.items.any { it.id == notification.id }) return

        val item = toUi(notification)
        mutableUiState.update { current ->
            current.copy(
                items = listOf(item) + current.items,
                unread = if (item.isRead) current.unread else current.unread + 1,
            )
        }
    }

    private fun toUi(notification: AppNotification): NotificationUi {
        val locale = currentLocale()
        return NotificationUi(
            id = notification.id,
            type = notification.type,
            actor = notification.actor,
            actorId = notification.actorId,
            actorUsername = notification.actor?.username.orEmpty(),
            postId = notification.postId,
            title = notification.localizedTitle(locale),
            body = notification.localizedBody(locale),
            isRead = notification.isRead,
            createdAtMillis = notification.createdAtMillis,
        )
    }

    private fun currentLocale(): String =
        appSettingsRepository.settings.value.language.tag.ifBlank { Locale.getDefault().language }

    companion object {
        private const val PageSize = 25
        private const val ReadBatchDelayMillis = 250L
        private const val NotificationEventType = "notification"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                NotificationsViewModel(
                    authRepository = application.container.authRepository,
                    notificationsRepository = application.container.notificationsRepository,
                    realtimeSocket = application.container.realtimeSocket,
                    appSettingsRepository = application.container.appSettingsRepository,
                )
            }
        }
    }
}
