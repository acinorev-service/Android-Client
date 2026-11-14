package ru.fet1sov.akinorev.feature.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.fet1sov.akinorev.model.CreateDestination
import ru.fet1sov.akinorev.ui.components.DestinationPlaceholder

@Composable
fun CreateScreen(modifier: Modifier = Modifier) {
    DestinationPlaceholder(
        destination = CreateDestination,
        modifier = modifier,
    )
}
