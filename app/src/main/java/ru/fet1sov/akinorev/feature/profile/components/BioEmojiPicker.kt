package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.Emoji
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

data class BioEmojiPickerUiState(
    val emojis: List<Emoji> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

class BioEmojiPickerViewModel(
    private val repository: EmojisRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(BioEmojiPickerUiState())
    val uiState: StateFlow<BioEmojiPickerUiState> = mutableUiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true, hasError = false) }
            when (val result = repository.fetchPage(page = 1, limit = EmojiPickerLimit)) {
                is AuthResult.Success -> mutableUiState.update {
                    it.copy(
                        emojis = result.value.items,
                        isLoading = false,
                        hasError = false,
                    )
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        emojis = emptyList(),
                        isLoading = false,
                        hasError = true,
                    )
                }
            }
        }
    }

    companion object {
        private const val EmojiPickerLimit = 60

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                BioEmojiPickerViewModel(application.container.emojisRepository)
            }
        }
    }
}

@Composable
fun BioEmojiPickerDialog(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: BioEmojiPickerViewModel = viewModel(factory = BioEmojiPickerViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = palette.bar,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.emojis_title),
                            color = palette.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = androidx.compose.ui.graphics.Color.Transparent,
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AppIcon(
                                    kind = AppIconKind.Close,
                                    tint = palette.textPrimary,
                                    contentDescription = null,
                                    size = 18.dp,
                                )
                            }
                        }
                    }

                    when {
                        state.isLoading -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = palette.textPrimary,
                            )
                        }

                        state.hasError -> Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.emojis_unavailable),
                                color = palette.textMuted,
                                fontSize = 13.sp,
                            )
                            TextButton(onClick = viewModel::retry) {
                                Text(
                                    text = stringResource(R.string.emojis_retry),
                                    color = palette.textPrimary,
                                )
                            }
                        }

                        state.emojis.isEmpty() -> Text(
                            text = stringResource(R.string.emojis_empty),
                            color = palette.textMuted,
                            fontSize = 13.sp,
                        )

                        else -> LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(state.emojis, key = { it.id }) { emoji ->
                                Surface(
                                    onClick = { onSelect(emoji.name) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = androidx.compose.ui.graphics.Color.Transparent,
                                ) {
                                    RemoteImage(
                                        url = resolveMediaUrl(emoji.url),
                                        contentDescription = emoji.title.ifBlank { emoji.name },
                                        modifier = Modifier.size(44.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
