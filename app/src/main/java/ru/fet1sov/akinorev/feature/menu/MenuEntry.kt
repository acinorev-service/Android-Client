package ru.fet1sov.akinorev.feature.menu

import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.icons.AppIconKind

enum class MenuEntry(
    val labelRes: Int,
    val icon: AppIconKind,
) {
    Notifications(R.string.nav_notifications, AppIconKind.Notifications),
    Messages(R.string.nav_messages, AppIconKind.Messages),
    Community(R.string.nav_community, AppIconKind.Community),
    Games(R.string.nav_games, AppIconKind.Games),
    Emojis(R.string.nav_emojis, AppIconKind.Emojis),
    Settings(R.string.nav_settings, AppIconKind.Settings),
}
