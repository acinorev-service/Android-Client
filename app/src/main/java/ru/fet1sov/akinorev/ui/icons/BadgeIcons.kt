package ru.fet1sov.akinorev.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

@Composable
fun BadgeIcon(
    slug: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Int = 26,
) {
    val vector = badgeVector(slug)
    Icon(
        imageVector = vector,
        contentDescription = contentDescription,
        tint = Color.Unspecified,
        modifier = modifier.size(size.dp),
    )
}

fun badgeVector(slug: String): ImageVector = when (slug.lowercase()) {
    "badge-good" -> BadgeGood
    "badge-incredible" -> BadgeIncredible
    "badge-legendary" -> BadgeLegendary
    "badge-unreal" -> BadgeUnreal
    else -> BadgeCommon
}

private class BadgeShape(
    val data: String,
    val fill: Color? = null,
    val stroke: Color? = null,
    val strokeWidth: Float = 0f,
)

private fun badge(fill: Color, stroke: Color, vararg shapes: BadgeShape): ImageVector =
    ImageVector.Builder(
        name = "badge",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(CirclePath).toNodes(),
            fill = SolidColor(fill),
            stroke = SolidColor(stroke),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        shapes.forEach { shape ->
            addPath(
                pathData = PathParser().parsePathString(shape.data).toNodes(),
                fill = shape.fill?.let(::SolidColor),
                stroke = shape.stroke?.let(::SolidColor),
                strokeLineWidth = shape.strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private val BadgeCommon: ImageVector by lazy {
    badge(
        fill = Color(0xFFF3F4F6),
        stroke = Color(0xFF9BA3AF),
        BadgeShape(CircleDotPath, fill = Color(0xFF9BA3AF)),
    )
}

private val BadgeGood: ImageVector by lazy {
    badge(
        fill = Color(0xFFDCFCE7),
        stroke = Color(0xFF22C55E),
        BadgeShape(
            "M8 12.5l2.5 2.5L16 9.5",
            stroke = Color(0xFF16A34A),
            strokeWidth = 2f,
        ),
    )
}

private val BadgeIncredible: ImageVector by lazy {
    badge(
        fill = Color(0xFFDBEAFE),
        stroke = Color(0xFF3B82F6),
        BadgeShape("M12 6v4M12 13v1", stroke = Color(0xFF2563EB), strokeWidth = 2f),
        BadgeShape(SmallDotPath, fill = Color(0xFF2563EB)),
    )
}

private val BadgeLegendary: ImageVector by lazy {
    badge(
        fill = Color(0xFFFEF9C3),
        stroke = Color(0xFFEAB308),
        BadgeShape(
            "M12 5.5L13.4 9.1l3.6.3-2.7 2.4.8 3.5L12 13.6 9.9 15.3l.8-3.5-2.7-2.4 3.6-.3z",
            fill = Color(0xFFEAB308),
        ),
    )
}

private val BadgeUnreal: ImageVector by lazy {
    badge(
        fill = Color(0xFFF3E8FF),
        stroke = Color(0xFFA855F7),
        BadgeShape(
            "M12 6l1.2 3.2L16.5 10l-2.6 2 1 3.2-2.9-1.9-2.9 1.9 1-3.2-2.6-2 3.3-.8z",
            fill = Color(0xFFA855F7),
        ),
        BadgeShape(
            "M18.5 3.5a2 2 0 1 0 0 4 2 2 0 0 0 0-4z",
            fill = Color(0xFFA855F7),
        ),
    )
}

private const val CirclePath = "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z"
private const val CircleDotPath = "M12 8.5a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7z"
private const val SmallDotPath = "M12 16a1 1 0 1 0 0 2 1 1 0 0 0 0-2z"
