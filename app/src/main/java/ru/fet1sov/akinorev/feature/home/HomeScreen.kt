package ru.fet1sov.akinorev.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.fet1sov.akinorev.model.HomeDestination
import ru.fet1sov.akinorev.ui.components.DestinationPlaceholder

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    DestinationPlaceholder(
        destination = HomeDestination,
        modifier = modifier,
    )
}
