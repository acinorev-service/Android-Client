package ru.fet1sov.akinorev.feature.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.fet1sov.akinorev.model.MenuDestination
import ru.fet1sov.akinorev.ui.components.DestinationPlaceholder

@Composable
fun MenuScreen(modifier: Modifier = Modifier) {
    DestinationPlaceholder(
        destination = MenuDestination,
        modifier = modifier,
    )
}
