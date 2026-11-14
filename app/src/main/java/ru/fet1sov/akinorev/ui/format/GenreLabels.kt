package ru.fet1sov.akinorev.ui.format

import androidx.annotation.StringRes
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.icons.AppIconKind

@StringRes
fun genreLabelRes(slug: String): Int? = when (slug.lowercase()) {
    "rpg" -> R.string.genres_rpg
    "action" -> R.string.genres_action
    "puzzle" -> R.string.genres_puzzle
    "racing" -> R.string.genres_racing
    "casual" -> R.string.genres_casual
    "strategy" -> R.string.genres_strategy
    "shooter" -> R.string.genres_shooter
    "sports" -> R.string.genres_sports
    "adventure" -> R.string.genres_adventure
    "simulation" -> R.string.genres_simulation
    "platformer" -> R.string.genres_platformer
    "horror" -> R.string.genres_horror
    "indie" -> R.string.genres_indie
    "openworld" -> R.string.genres_openworld
    "fighting" -> R.string.genres_fighting
    "sandbox" -> R.string.genres_sandbox
    "roleplay" -> R.string.genres_roleplay
    "arcade" -> R.string.genres_arcade
    else -> null
}

fun genreIconKind(slug: String): AppIconKind = when (slug.lowercase()) {
    "shooter" -> AppIconKind.Crosshair
    "sandbox" -> AppIconKind.Community
    "roleplay" -> AppIconKind.Messages
    "arcade" -> AppIconKind.Joystick
    "racing" -> AppIconKind.ArrowRight
    "horror" -> AppIconKind.Eye
    "action" -> AppIconKind.Crosshair
    "fighting" -> AppIconKind.Crosshair
    "sports" -> AppIconKind.ArrowRight
    else -> AppIconKind.Games
}
