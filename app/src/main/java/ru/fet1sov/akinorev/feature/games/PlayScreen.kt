package ru.fet1sov.akinorev.feature.games

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.SectionPlaceholder
import ru.fet1sov.akinorev.ui.icons.AppIconKind

@Composable
fun PlayScreen(
    gameId: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    SectionPlaceholder(
        title = title.ifBlank { stringResource(R.string.game_play) },
        path = "/play/$gameId",
        description = stringResource(R.string.game_play_placeholder),
        icon = AppIconKind.Games,
        modifier = modifier,
    )
}
