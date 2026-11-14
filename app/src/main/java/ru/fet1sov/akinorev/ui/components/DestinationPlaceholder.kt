package ru.fet1sov.akinorev.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.model.TopLevelDestination
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.icons.NavIconKind
import ru.fet1sov.akinorev.ui.icons.NavIconImage
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun SectionPlaceholder(
    title: String,
    path: String,
    description: String,
    icon: AppIconKind,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(palette.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                kind = icon,
                tint = palette.accent,
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = title,
            color = palette.textPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = path,
            color = palette.accent,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.6.sp,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = description,
            color = palette.textMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun DestinationPlaceholder(
    destination: TopLevelDestination,
    modifier: Modifier = Modifier,
) {
    SectionPlaceholder(
        title = stringResource(destination.labelRes),
        path = destination.path,
        description = stringResource(destination.descriptionRes),
        icon = destination.icon.toAppIconKind(),
        modifier = modifier,
    )
}

fun NavIconKind.toAppIconKind(): AppIconKind = when (this) {
    NavIconKind.Home -> AppIconKind.Home
    NavIconKind.Chat -> AppIconKind.Messages
    NavIconKind.Logotype -> AppIconKind.Logotype
    NavIconKind.Community -> AppIconKind.Community
    NavIconKind.Gamepad -> AppIconKind.Games
}
