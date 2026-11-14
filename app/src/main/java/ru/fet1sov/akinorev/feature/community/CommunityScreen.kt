package ru.fet1sov.akinorev.feature.community

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.model.CommunityDestination
import ru.fet1sov.akinorev.ui.components.SectionPlaceholder
import ru.fet1sov.akinorev.ui.icons.AppIconKind

@Composable
fun CommunityScreen(modifier: Modifier = Modifier) {
    SectionPlaceholder(
        title = stringResource(R.string.nav_community),
        path = CommunityDestination.path,
        description = stringResource(R.string.section_community_description),
        icon = AppIconKind.Community,
        modifier = modifier,
    )
}
