package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.model.PostLocales
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun PostLocaleSelector(
    activeLocale: String,
    onLocaleChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PostLocales.forEach { locale ->
            val selected = locale == activeLocale
            Surface(
                onClick = { onLocaleChange(locale) },
                shape = RoundedCornerShape(50),
                color = if (selected) palette.idleContent else palette.bar,
            ) {
                Text(
                    text = locale.uppercase(),
                    color = if (selected) palette.bar else palette.textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                )
            }
        }
    }
}
