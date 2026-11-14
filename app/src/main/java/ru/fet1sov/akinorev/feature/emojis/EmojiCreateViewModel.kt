package ru.fet1sov.akinorev.feature.emojis

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
import ru.fet1sov.akinorev.data.emojis.EmojiFileResult
import ru.fet1sov.akinorev.data.emojis.EmojiFileSource
import ru.fet1sov.akinorev.data.emojis.EmojiUploadFile
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.EmojiMetadata
import ru.fet1sov.akinorev.model.MaxEmojiDescriptionLength
import ru.fet1sov.akinorev.model.MaxEmojiTitleLength
import ru.fet1sov.akinorev.model.emojiMarker
import ru.fet1sov.akinorev.model.isValidEmojiName
import ru.fet1sov.akinorev.model.normalizeEmojiName

data class EmojiCreateUiState(
    val isAuthPending: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isSubscribed: Boolean = false,
    val name: String = "",
    val title: String = "",
    val description: String = "",
    val isNameRejected: Boolean = false,
    val pickedUri: String = "",
    val pickedFileName: String = "",
    val isReadingFile: Boolean = false,
    val isUploading: Boolean = false,
    val error: EmojiCreateError? = null,
    val isCreated: Boolean = false,
) {
    val nameMarker: String
        get() = name.takeIf { it.isNotBlank() }?.let(::emojiMarker).orEmpty()

    val canSubmit: Boolean
        get() = isSubscribed &&
            pickedUri.isNotBlank() &&
            isValidEmojiName(name) &&
            !isUploading &&
            !isReadingFile

    val hasDraft: Boolean
        get() = name.isNotBlank() ||
            title.isNotBlank() ||
            description.isNotBlank() ||
            pickedUri.isNotBlank()
}

sealed interface EmojiCreateError {
    data object Format : EmojiCreateError

    data object Size : EmojiCreateError

    data object FileUnreadable : EmojiCreateError

    data object NameTaken : EmojiCreateError

    data object SubscriptionRequired : EmojiCreateError

    data object Server : EmojiCreateError

    data object Unknown : EmojiCreateError
}

class EmojiCreateViewModel(
    private val authRepository: AuthRepository,
    private val emojisRepository: EmojisRepository,
    private val emojiFileSource: EmojiFileSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(EmojiCreateUiState())
    val uiState: StateFlow<EmojiCreateUiState> = mutableUiState.asStateFlow()

    private var pickedFile: EmojiUploadFile? = null

    init {
        viewModelScope.launch {
            authRepository.session.collect { state ->
                val authorized = state as? SessionState.Authorized
                mutableUiState.update {
                    it.copy(
                        isAuthPending = state is SessionState.Loading,
                        isAuthenticated = authorized != null,
                        isSubscribed = authorized?.user?.isSubscribed == true,
                    )
                }
            }
        }

        viewModelScope.launch { authRepository.restoreSession() }
    }

    fun onNameChange(value: String) {
        val normalized = normalizeEmojiName(value)
        mutableUiState.update {
            it.copy(
                name = normalized,
                isNameRejected = normalized.isEmpty() && value.isNotBlank(),
            )
        }
    }

    fun onTitleChange(value: String) {
        mutableUiState.update { it.copy(title = value.take(MaxEmojiTitleLength)) }
    }

    fun onDescriptionChange(value: String) {
        mutableUiState.update { it.copy(description = value.take(MaxEmojiDescriptionLength)) }
    }

    fun onFilePicked(uri: String) {
        if (uri.isBlank()) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(isReadingFile = true, error = null) }
            when (val result = emojiFileSource.read(uri)) {
                is EmojiFileResult.Ready -> {
                    pickedFile = result.file
                    mutableUiState.update {
                        it.copy(
                            pickedUri = uri,
                            pickedFileName = result.file.fileName,
                            isReadingFile = false,
                            error = null,
                        )
                    }
                }

                EmojiFileResult.Unsupported -> {
                    pickedFile = null
                    mutableUiState.update {
                        it.copy(
                            pickedUri = "",
                            pickedFileName = "",
                            isReadingFile = false,
                            error = EmojiCreateError.Format,
                        )
                    }
                }

                EmojiFileResult.TooLarge -> {
                    pickedFile = null
                    mutableUiState.update {
                        it.copy(
                            pickedUri = "",
                            pickedFileName = "",
                            isReadingFile = false,
                            error = EmojiCreateError.Size,
                        )
                    }
                }

                EmojiFileResult.Unreadable -> {
                    pickedFile = null
                    mutableUiState.update {
                        it.copy(
                            pickedUri = "",
                            pickedFileName = "",
                            isReadingFile = false,
                            error = EmojiCreateError.FileUnreadable,
                        )
                    }
                }
            }
        }
    }

    fun clearFile() {
        pickedFile = null
        mutableUiState.update {
            it.copy(
                pickedUri = "",
                pickedFileName = "",
                isReadingFile = false,
                error = null,
            )
        }
    }

    fun submit() {
        val state = mutableUiState.value
        val file = pickedFile ?: return
        if (!state.canSubmit) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(isUploading = true, error = null) }
            val metadata = EmojiMetadata(
                name = normalizeEmojiName(state.name),
                title = state.title.trim(),
                description = state.description.trim(),
            )

            when (val result = emojisRepository.upload(file, metadata)) {
                is AuthResult.Success -> mutableUiState.update {
                    it.copy(isUploading = false, isCreated = true)
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(isUploading = false, error = result.error.toCreateError())
                }
            }
        }
    }

    fun dismissError() {
        mutableUiState.update { it.copy(error = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                EmojiCreateViewModel(
                    authRepository = application.container.authRepository,
                    emojisRepository = application.container.emojisRepository,
                    emojiFileSource = application.container.emojiFileSource,
                )
            }
        }
    }
}

private fun AuthError.toCreateError(): EmojiCreateError = when (this) {
    is AuthError.Server -> when (code) {
        "emoji_name_taken" -> EmojiCreateError.NameTaken
        "subscription_required" -> EmojiCreateError.SubscriptionRequired
        "unsafe_content" -> EmojiCreateError.Format
        else -> EmojiCreateError.Server
    }

    AuthError.Network -> EmojiCreateError.Server
    AuthError.Unexpected -> EmojiCreateError.Unknown
}
