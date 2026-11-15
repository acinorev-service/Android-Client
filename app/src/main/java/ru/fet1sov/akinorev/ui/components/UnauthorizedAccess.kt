package ru.fet1sov.akinorev.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun UnauthorizedAccess(
    title: String,
    description: String,
    buttonLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    icon: AppIconKind = AppIconKind.Lock,
    danger: Boolean = false,
    hideButton: Boolean = false,
) {
    val palette = navigationPalette()
    val accent = if (danger) DangerColor else palette.textPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (danger) DangerColor.copy(alpha = 0.05f) else palette.screen)
                .border(
                    BorderStroke(
                        width = 1.dp,
                        color = if (danger) DangerColor.copy(alpha = 0.5f) else palette.barBorder,
                    ),
                    RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        if (danger) DangerColor.copy(alpha = 0.15f) else palette.bar,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    kind = icon,
                    tint = accent,
                    modifier = Modifier.size(40.dp),
                    size = 40.dp,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title.uppercase(),
                    color = if (danger) DangerColor else palette.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = description,
                    color = if (danger) DangerColor.copy(alpha = 0.9f) else palette.textMuted,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                )
            }

            if (!hideButton) {
                Surface(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    color = palette.idleContent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 240.dp),
                ) {
                    Text(
                        text = buttonLabel.uppercase(),
                        color = palette.bar,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

private val DangerColor = Color(0xFFE5484D)
