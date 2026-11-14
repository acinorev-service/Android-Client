package ru.fet1sov.akinorev.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.SectionPlaceholder
import ru.fet1sov.akinorev.ui.icons.AppIconKind

@Composable
fun ProfileScreen(
    username: String,
    modifier: Modifier = Modifier,
) {
    SectionPlaceholder(
        title = username.ifBlank { stringResource(R.string.profile_title) },
        path = "/profile/$username",
        description = stringResource(R.string.section_profile_description),
        icon = AppIconKind.Community,
        modifier = modifier,
    )
}
