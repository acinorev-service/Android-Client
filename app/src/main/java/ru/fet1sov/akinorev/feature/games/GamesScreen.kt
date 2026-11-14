package ru.fet1sov.akinorev.feature.games

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.fet1sov.akinorev.model.GamesDestination
import ru.fet1sov.akinorev.ui.components.DestinationPlaceholder

@Composable
fun GamesScreen(modifier: Modifier = Modifier) {
    DestinationPlaceholder(
        destination = GamesDestination,
        modifier = modifier,
    )
}
