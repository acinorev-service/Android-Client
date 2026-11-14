package ru.fet1sov.akinorev.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.fet1sov.akinorev.model.TopLevelDestination
import ru.fet1sov.akinorev.ui.icons.NavIconImage
import ru.fet1sov.akinorev.ui.icons.NavIconKind
import kotlin.math.roundToInt

private val BarShape = RoundedCornerShape(percent = 50)
private val TabShape = RoundedCornerShape(percent = 50)

@Composable
fun AcinorevNavigationBar(
    items: List<TopLevelDestination>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badges: Map<TopLevelDestination, Int> = emptyMap(),
) {
    val palette = navigationPalette()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BarShape,
        color = palette.bar,
        border = BorderStroke(1.dp, palette.barBorder),
        shadowElevation = 18.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(6.dp),
        ) {
            val itemWidth = maxWidth / items.size
            val pillOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                label = "pillOffset",
            )

            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(TabShape)
                    .background(palette.activePill)
                    .border(1.dp, palette.activePillBorder, TabShape),
            )

            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        NavigationBarTab(
                            item = item,
                            index = index,
                            selected = index == selectedIndex,
                            badge = badges[item] ?: 0,
                            palette = palette,
                            onClick = { onSelect(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationBarTab(
    item: TopLevelDestination,
    index: Int,
    selected: Boolean,
    badge: Int,
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 50L)
        appeared = true
    }

    val appear by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "appear",
    )

    val selection by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 520f),
        label = "selection",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "press",
    )

    val contentColor = lerp(palette.idleContent, palette.accent, selection)
    val iconScale = 1f + 0.12f * selection - 0.08f * press
    val iconRotation = when (item.icon) {
        NavIconKind.Gamepad -> -8f * selection
        NavIconKind.Logotype -> 10f * selection
        else -> 0f
    }
    val labelWeight = FontWeight((500f + 200f * selection).roundToInt())
    val density = LocalDensity.current
    val slideOffset = with(density) { ((1f - appear) * 12.dp.toPx()).roundToInt() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(-slideOffset, 0) }
            .alpha(appear)
            .clip(TabShape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = ripple(color = palette.accent),
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            NavIconImage(
                kind = item.icon,
                tint = contentColor,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        rotationZ = iconRotation
                    },
            )
            if (badge > 0) {
                CountBadge(
                    count = badge,
                    palette = palette,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 9.dp, y = (-5).dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(item.labelRes),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = labelWeight,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            letterSpacing = 0.2.sp,
        )
    }
}

@Composable
private fun CountBadge(
    count: Int,
    palette: NavigationPalette,
    modifier: Modifier = Modifier,
) {
    val label = if (count > 99) "99+" else count.toString()

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
            .clip(CircleShape)
            .background(palette.badge)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = palette.badgeText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}
