package ru.fet1sov.akinorev.ui.system

import android.content.res.Configuration
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

fun ComponentActivity.applyAcinorevEdgeToEdge() {
    val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    val barStyle = if (isNightMode) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
}
