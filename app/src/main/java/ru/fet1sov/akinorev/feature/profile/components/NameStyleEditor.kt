package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.ProfileName
import ru.fet1sov.akinorev.ui.components.parseHexColor
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NameStyleEditor(
    username: String,
    color: String,
    animation: String,
    subscribed: Boolean,
    onColorChange: (String) -> Unit,
    onAnimationChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    val preview = username.ifBlank { stringResource(R.string.community_profile_edit_name_preview) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.community_profile_edit_name_style),
                color = palette.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.weight(1f))
            SubscriptionBadge(subscribed = subscribed)
        }

        Column(
            modifier = Modifier.alpha(if (subscribed) 1f else 0.55f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, palette.barBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                ProfileName(
                    username = preview,
                    nameColor = color,
                    nameAnimation = animation,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NameColorPresets.forEach { preset ->
                    val selected = preset.equals(color, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(preset) ?: Color.White)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) palette.textPrimary else palette.barBorder,
                                shape = CircleShape,
                            )
                            .clickable(enabled = subscribed) { onColorChange(preset) },
                    )
                }
            }

            OutlinedTextField(
                value = color,
                onValueChange = { value ->
                    if (value.length <= 7 && HexInputRegex.matches(value)) onColorChange(value)
                },
                modifier = Modifier.width(140.dp),
                singleLine = true,
                enabled = subscribed,
                placeholder = {
                    Text(text = "#RRGGBB", color = palette.textMuted, fontSize = 14.sp)
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.textMuted,
                    unfocusedBorderColor = palette.barBorder,
                    focusedContainerColor = palette.bar,
                    unfocusedContainerColor = palette.bar,
                    cursorColor = palette.textPrimary,
                    focusedTextColor = palette.textPrimary,
                    unfocusedTextColor = palette.textPrimary,
                    disabledTextColor = palette.textMuted,
                ),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NameAnimations.forEach { preset ->
                    NameAnimationChip(
                        label = stringResource(preset.labelRes),
                        selected = preset.value == animation,
                        enabled = subscribed,
                        onClick = { onAnimationChange(preset.value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionBadge(subscribed: Boolean) {
    val palette = navigationPalette()
    val color = if (subscribed) Color(0xFF16A34A) else palette.textMuted

    Surface(
        shape = RoundedCornerShape(50),
        color = if (subscribed) color.copy(alpha = 0.1f) else palette.barBorder.copy(alpha = 0.2f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (subscribed) {
                AppIcon(
                    kind = AppIconKind.Diamond,
                    tint = color,
                    modifier = Modifier.size(13.dp),
                )
            }
            Text(
                text = stringResource(
                    if (subscribed) {
                        R.string.community_profile_edit_name_available
                    } else {
                        R.string.community_profile_edit_name_locked
                    },
                ),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun NameAnimationChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val palette = navigationPalette()
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        color = if (selected) palette.idleContent else palette.bar,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, palette.barBorder),
    ) {
        Text(
            text = label.uppercase(),
            color = if (selected) palette.bar else palette.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

private data class NameAnimationOption(
    val value: String,
    val labelRes: Int,
)

private val NameColorPresets = listOf(
    "#ffffff",
    "#10b981",
    "#06b6d4",
    "#3b82f6",
    "#8b5cf6",
    "#ec4899",
    "#f43f5e",
    "#f59e0b",
    "#84cc16",
    "#14b8a6",
)

private val NameAnimations = listOf(
    NameAnimationOption("", R.string.community_profile_edit_name_solid),
    NameAnimationOption("gradient", R.string.community_profile_edit_name_gradient),
    NameAnimationOption("rainbow", R.string.community_profile_edit_name_rainbow),
)

private val HexInputRegex = Regex("^#?[0-9a-fA-F]{0,6}$")
