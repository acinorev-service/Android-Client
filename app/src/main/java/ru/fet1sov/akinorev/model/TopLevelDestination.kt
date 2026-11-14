package ru.fet1sov.akinorev.model

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.icons.NavIconKind

sealed interface TopLevelDestination {
    val path: String

    @get:StringRes
    val labelRes: Int

    @get:StringRes
    val descriptionRes: Int

    val icon: NavIconKind
}

@Serializable
data object HomeDestination : TopLevelDestination {
    override val path: String = "/"
    override val labelRes: Int = R.string.nav_home
    override val descriptionRes: Int = R.string.nav_home_description
    override val icon: NavIconKind = NavIconKind.Home
}

@Serializable
data object MessagesDestination : TopLevelDestination {
    override val path: String = "/messages"
    override val labelRes: Int = R.string.nav_messages
    override val descriptionRes: Int = R.string.nav_messages_description
    override val icon: NavIconKind = NavIconKind.Chat
}

@Serializable
data object CreateDestination : TopLevelDestination {
    override val path: String = "/action"
    override val labelRes: Int = R.string.nav_create
    override val descriptionRes: Int = R.string.nav_create_description
    override val icon: NavIconKind = NavIconKind.Logotype
}

@Serializable
data object MenuDestination : TopLevelDestination {
    override val path: String = "/menu"
    override val labelRes: Int = R.string.nav_menu
    override val descriptionRes: Int = R.string.nav_menu_description
    override val icon: NavIconKind = NavIconKind.Community
}

@Serializable
data object GamesDestination : TopLevelDestination {
    override val path: String = "/games"
    override val labelRes: Int = R.string.nav_games
    override val descriptionRes: Int = R.string.nav_games_description
    override val icon: NavIconKind = NavIconKind.Gamepad
}

val topLevelDestinations: List<TopLevelDestination> = listOf(
    HomeDestination,
    MessagesDestination,
    CreateDestination,
    MenuDestination,
    GamesDestination,
)
