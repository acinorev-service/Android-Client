package ru.fet1sov.akinorev.ui.icons

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.min

enum class NavIconKind {
    Home,
    Chat,
    Logotype,
    Community,
    Gamepad,
}

private data class IconShape(
    val data: String,
    val filled: Boolean,
    val fillType: PathFillType = PathFillType.NonZero,
    val eraseHoldEnd: Float = 0f,
    val eraseReturnEnd: Float = 0f,
)

private data class IconSpec(
    val viewport: Float,
    val strokeWidth: Float,
    val shapes: List<IconShape>,
)

private class ShapeGeometry(
    val path: Path,
    val length: Float,
    val filled: Boolean,
    val eraseHoldEnd: Float,
    val eraseReturnEnd: Float,
)

private class IconGeometry(spec: IconSpec) {
    val viewport = spec.viewport
    val strokeWidth = spec.strokeWidth
    val shapes: List<ShapeGeometry> = spec.shapes.map { shape ->
        val path = PathParser().parsePathString(shape.data).toPath().apply {
            fillType = shape.fillType
        }
        val measure = PathMeasure().apply { setPath(path, false) }
        ShapeGeometry(path, measure.length, shape.filled, shape.eraseHoldEnd, shape.eraseReturnEnd)
    }
}

private const val HOME_PATH =
    "M17.0791 1.81597C17.6208 1.39468 18.3792 1.39468 18.9209 1.81597L32.4208 12.316C32.7862 12.6002 33 13.0371 33 13.5V30.75C33 31.9926 31.9926 33 30.75 33H22.5V21C22.5 20.1716 21.8284 19.5 21 19.5H15C14.1716 19.5 13.5 20.1716 13.5 21V33H5.25C4.00735 33 3 31.9926 3 30.75V13.5C3 13.0371 3.21371 12.6002 3.57909 12.316L17.0791 1.81597Z"

private const val CHAT_PATH =
    "M22 10.787c0 4.854-4.253 8.788-9.5 8.788a10.22 10.22 0 0 1-2.684-.355c-1.881 1.241-4.233 1.617-5.485 1.73-.286.027-.434-.347-.24-.559.595-.65 1.431-1.803 1.715-3.368C4.073 15.43 3 13.225 3 10.787 3 5.934 7.253 2 12.5 2S22 5.934 22 10.787z"

private const val LOGOTYPE_RING =
    "M38.4546 19.2993C48.7288 19.2993 56.9535 27.4118 56.9536 37.2915C56.9536 47.1713 48.7289 55.2847 38.4546 55.2847C28.1805 55.2844 19.9565 47.1712 19.9565 37.2915C19.9567 27.412 28.1806 19.2996 38.4546 19.2993Z"

private const val LOGOTYPE_SPARK_FORWARD =
    "M33.2389 33.2391C41.8461 24.6319 50.3129 17.6154 57.0085 13.2206C60.3738 11.0117 63.1798 9.53964 65.2653 8.8267C66.3133 8.46843 67.0443 8.34918 67.4991 8.3523C67.5546 8.35269 67.6019 8.35556 67.6421 8.35852C67.645 8.39852 67.6472 8.44566 67.6476 8.50077C67.6507 8.95561 67.5315 9.68662 67.1732 10.7346C66.4603 12.8201 64.989 15.6267 62.78 18.992C58.3851 25.6876 51.3679 34.1537 42.7607 42.7608C34.1535 51.368 25.6874 58.3852 18.9918 62.7801C15.6264 64.989 12.8198 66.4603 10.7344 67.1732C9.68638 67.5315 8.95533 67.6508 8.50049 67.6476C8.44539 67.6473 8.39824 67.6451 8.35824 67.6421C8.35528 67.602 8.35241 67.5546 8.35203 67.4992C8.34891 67.0443 8.46815 66.3133 8.82642 65.2653C9.53935 63.1799 11.0114 60.3739 13.2203 57.0086C17.6152 50.313 24.6317 41.8463 33.2389 33.2391Z"

private const val LOGOTYPE_SPARK_BACKWARD =
    "M42.761 33.2388C51.3682 41.846 58.3847 50.3127 62.7796 57.0083C64.9885 60.3735 66.4605 63.1796 67.1735 65.265C67.5318 66.313 67.651 67.044 67.6479 67.4989C67.6475 67.5543 67.6446 67.6016 67.6417 67.6418C67.6017 67.6447 67.5545 67.6469 67.4994 67.6473C67.0446 67.6504 66.3136 67.5312 65.2655 67.1729C63.1801 66.46 60.3735 64.9887 57.0081 62.7798C50.3125 58.3849 41.8464 51.3677 33.2392 42.7605C24.632 34.1534 17.6148 25.6873 13.2199 18.9917C11.0109 15.6264 9.53962 12.8198 8.82671 10.7343C8.46845 9.68634 8.34919 8.95529 8.35231 8.50046C8.3527 8.44535 8.3549 8.39821 8.35783 8.35821C8.398 8.35525 8.44535 8.35238 8.50077 8.35199C8.95562 8.34887 9.68663 8.46812 10.7347 8.82639C12.8201 9.53931 15.6261 11.0113 18.9914 13.2202C25.687 17.6151 34.1538 24.6316 42.761 33.2388Z"

private const val COMMUNITY_HEAD =
    "M9.66667 10.3333C9.66667 12.013 10.3339 13.6239 11.5217 14.8117C12.7094 15.9994 14.3203 16.6667 16 16.6667C17.6797 16.6667 19.2906 15.9994 20.4783 14.8117C21.6661 13.6239 22.3333 12.013 22.3333 10.3333C22.3333 8.65363 21.6661 7.04272 20.4783 5.85499C19.2906 4.66726 17.6797 4 16 4C14.3203 4 12.7094 4.66726 11.5217 5.85499C10.3339 7.04272 9.66667 8.65363 9.66667 10.3333Z"

private const val COMMUNITY_BODY =
    "M26 27.3333C26 24.6811 24.9464 22.1376 23.0711 20.2622C21.1957 18.3869 18.6522 17.3333 16 17.3333C13.3478 17.3333 10.8043 18.3869 8.92893 20.2622C7.05357 22.1376 6 24.6811 6 27.3333C6 27.5101 6.07024 27.6797 6.19526 27.8047C6.32029 27.9297 6.48986 28 6.66667 28H25.3333C25.5101 28 25.6797 27.9297 25.8047 27.8047C25.9298 27.6797 26 27.5101 26 27.3333Z"

private const val GAMEPAD_BODY =
    "M22.7879 10.1818H17.4545V4.84847C17.4545 4.04517 16.8033 3.39392 16 3.39392C15.1967 3.39392 14.5455 4.04517 14.5455 4.84847V10.1818H9.21212C4.13256 10.1818 0 14.3144 0 19.3939C0 24.4735 4.13256 28.606 9.21212 28.606C11.266 28.606 13.2518 27.921 14.8639 26.6666H17.1362C18.7482 27.921 20.734 28.606 22.788 28.606C27.8675 28.606 32.0001 24.4735 32.0001 19.3939C32.0001 14.3144 27.8674 10.1818 22.7879 10.1818ZM22.7879 25.697C21.2527 25.697 19.7732 25.1374 18.6221 24.1215C18.3563 23.887 18.0141 23.7576 17.6596 23.7576H14.3405C13.986 23.7576 13.6437 23.887 13.378 24.1215C12.2268 25.1374 10.7473 25.697 9.21212 25.697C5.73663 25.697 2.90909 22.8694 2.90909 19.3939C2.90909 15.9184 5.73663 13.0909 9.21212 13.0909H22.7879C26.2634 13.0909 29.0909 15.9184 29.0909 19.3939C29.0909 22.8694 26.2634 25.697 22.7879 25.697Z"

private const val GAMEPAD_DPAD =
    "M11.1515 17.9394H10.6667V17.4545C10.6667 16.6512 10.0154 16 9.21212 16C8.40882 16 7.75757 16.6512 7.75757 17.4545V17.9394H7.27272C6.46942 17.9394 5.81818 18.5906 5.81818 19.3939C5.81818 20.1972 6.46942 20.8485 7.27272 20.8485H7.75757V21.3333C7.75757 22.1366 8.40882 22.7879 9.21212 22.7879C10.0154 22.7879 10.6667 22.1366 10.6667 21.3333V20.8485H11.1515C11.9548 20.8485 12.6061 20.1972 12.6061 19.3939C12.6061 18.5906 11.9548 17.9394 11.1515 17.9394Z"

private const val GAMEPAD_ACTION =
    "M23.1307 16.9941L20.3948 19.73C20.3925 19.7322 20.39 19.7341 20.3879 19.7362C20.3533 19.7708 20.321 19.8071 20.2903 19.8449C19.8438 20.3897 19.854 21.1823 20.3186 21.7171C20.3411 21.7432 20.3634 21.7695 20.3879 21.7939C20.422 21.8281 20.4579 21.8604 20.4951 21.8907C20.7623 22.1101 21.0893 22.2199 21.4164 22.2199C21.7432 22.2199 22.0698 22.1104 22.3369 21.8915C22.3746 21.8609 22.4109 21.8285 22.4455 21.7939C22.4476 21.7917 22.4494 21.7894 22.4516 21.7873L25.1877 19.0512C25.7557 18.4832 25.7557 17.5622 25.1877 16.9941C24.6197 16.4262 23.6987 16.4262 23.1307 16.9941Z"

private fun specFor(kind: NavIconKind): IconSpec = when (kind) {
    NavIconKind.Home -> IconSpec(
        viewport = 36f,
        strokeWidth = 0f,
        shapes = listOf(IconShape(HOME_PATH, filled = true)),
    )

    NavIconKind.Chat -> IconSpec(
        viewport = 24f,
        strokeWidth = 0f,
        shapes = listOf(IconShape(CHAT_PATH, filled = true, fillType = PathFillType.EvenOdd)),
    )

    NavIconKind.Logotype -> IconSpec(
        viewport = 76f,
        strokeWidth = 5f,
        shapes = listOf(
            IconShape(LOGOTYPE_RING, filled = false, eraseHoldEnd = 0.24f, eraseReturnEnd = 0.34f),
            IconShape(LOGOTYPE_SPARK_FORWARD, filled = false, eraseHoldEnd = 0.15f, eraseReturnEnd = 0.21f),
            IconShape(LOGOTYPE_SPARK_BACKWARD, filled = false, eraseHoldEnd = 0.15f, eraseReturnEnd = 0.21f),
        ),
    )

    NavIconKind.Community -> IconSpec(
        viewport = 32f,
        strokeWidth = 0f,
        shapes = listOf(
            IconShape(COMMUNITY_HEAD, filled = true),
            IconShape(COMMUNITY_BODY, filled = true),
        ),
    )

    NavIconKind.Gamepad -> IconSpec(
        viewport = 32f,
        strokeWidth = 0f,
        shapes = listOf(
            IconShape(GAMEPAD_BODY, filled = true),
            IconShape(GAMEPAD_DPAD, filled = true),
            IconShape(GAMEPAD_ACTION, filled = true),
        ),
    )
}

private fun eraseFraction(time: Float, holdEnd: Float, returnEnd: Float): Float {
    val eraseEnd = 0.13f
    return when {
        time <= 0f -> 0f
        time < eraseEnd -> time / eraseEnd
        time < holdEnd -> 1f
        time < returnEnd -> 1f - (time - holdEnd) / (returnEnd - holdEnd)
        else -> 0f
    }
}

@Composable
private fun logotypeProgress(): Float {
    val transition = rememberInfiniteTransition(label = "logotype")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 13000, easing = LinearEasing)),
        label = "logotypeStroke",
    )
    return progress
}

@Composable
fun NavIconImage(
    kind: NavIconKind,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val geometry = remember(kind) { IconGeometry(specFor(kind)) }
    val progress = if (kind == NavIconKind.Logotype) logotypeProgress() else 0f

    Canvas(modifier = modifier) {
        val factor = min(size.width / geometry.viewport, size.height / geometry.viewport)
        val left = (size.width - geometry.viewport * factor) / 2f
        val top = (size.height - geometry.viewport * factor) / 2f

        translate(left, top) {
            scale(factor, factor, pivot = Offset.Zero) {
                geometry.shapes.forEach { shape ->
                    if (shape.filled) {
                        drawPath(path = shape.path, color = tint)
                    } else {
                        val phase = eraseFraction(progress, shape.eraseHoldEnd, shape.eraseReturnEnd)
                        drawPath(
                            path = shape.path,
                            color = tint,
                            style = Stroke(
                                width = geometry.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(shape.length, shape.length),
                                    phase = phase * shape.length,
                                ),
                            ),
                        )
                    }
                }
            }
        }
    }
}
