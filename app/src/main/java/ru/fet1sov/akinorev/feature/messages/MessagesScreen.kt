package ru.fet1sov.akinorev.feature.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.fet1sov.akinorev.model.MessagesDestination
import ru.fet1sov.akinorev.ui.components.DestinationPlaceholder

@Composable
fun MessagesScreen(modifier: Modifier = Modifier) {
    DestinationPlaceholder(
        destination = MessagesDestination,
        modifier = modifier,
    )
}
