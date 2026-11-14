package ru.fet1sov.akinorev.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

enum class AppIconKind {
    Home,
    Notifications,
    Messages,
    Community,
    Games,
    Logotype,
    Emojis,
    Settings,
    Appearance,
    Account,
    Diamond,
    ChevronRight,
    ArrowLeft,
    Plus,
    Trashbin,
    Logout,
    Play,
    Cross,
    People,
    Joystick,
    ArrowRight,
    Eye,
    Expand,
    Collapse,
    Copy,
    Users,
    CloudOff,
    Refresh,
    Search,
    Close,
}

@Composable
fun AppIcon(
    kind: AppIconKind,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val vector = kind.toVector()
    if (vector != null) {
        Icon(
            imageVector = vector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier.size(24.dp),
        )
    } else {
        NavIconImage(
            kind = kind.toNavIconKind(),
            tint = tint,
            modifier = modifier,
        )
    }
}

private fun AppIconKind.toVector(): ImageVector? = when (this) {
    AppIconKind.Notifications -> AppIcons.Bell
    AppIconKind.Messages -> AppIcons.Chat
    AppIconKind.Community -> AppIcons.Unity
    AppIconKind.Games -> AppIcons.Gamepad
    AppIconKind.Emojis -> AppIcons.Emoji
    AppIconKind.Settings -> AppIcons.Gear
    AppIconKind.Appearance -> AppIcons.Pen
    AppIconKind.Account -> AppIcons.Person
    AppIconKind.Diamond -> AppIcons.Diamond
    AppIconKind.ChevronRight -> AppIcons.ChevronRight
    AppIconKind.ArrowLeft -> AppIcons.ArrowLeft
    AppIconKind.Plus -> AppIcons.Plus
    AppIconKind.Trashbin -> AppIcons.Trashbin
    AppIconKind.Logout -> AppIcons.DoorOut
    AppIconKind.Play -> AppIcons.Play
    AppIconKind.Cross -> AppIcons.Cross
    AppIconKind.People -> AppIcons.People
    AppIconKind.Joystick -> AppIcons.Joystick
    AppIconKind.ArrowRight -> AppIcons.ArrowRight
    AppIconKind.Eye -> AppIcons.Eye
    AppIconKind.Expand -> AppIcons.Expand
    AppIconKind.Collapse -> AppIcons.Collapse
    AppIconKind.Copy -> AppIcons.Copy
    AppIconKind.Users -> AppIcons.Users
    AppIconKind.CloudOff -> AppIcons.CloudOff
    AppIconKind.Refresh -> AppIcons.Refresh
    AppIconKind.Search -> AppIcons.Search
    AppIconKind.Close -> AppIcons.Close
    AppIconKind.Home,
    AppIconKind.Logotype,
    -> null
}

private fun AppIconKind.toNavIconKind(): NavIconKind = when (this) {
    AppIconKind.Logotype -> NavIconKind.Logotype
    else -> NavIconKind.Home
}

object AppIcons {
    val Bell: ImageVector by lazy {
        vector(
            viewport = 24f,
            BellBody,
            BellClapper,
        )
    }

    val Chat: ImageVector by lazy {
        vector(
            viewport = 24f,
            ChatBody,
        )
    }

    val Unity: ImageVector by lazy {
        vector(
            viewport = 502.648f,
            UnityTopCircle,
            UnityBody,
            UnitySideCircle,
            UnityLeftCircle,
        )
    }

    val Gamepad: ImageVector by lazy {
        vector(
            viewport = 32f,
            GamepadBody,
            GamepadDpad,
            GamepadAction,
        )
    }

    val Emoji: ImageVector by lazy {
        vector(
            viewport = 32f,
            EmojiFace,
        )
    }

    val Gear: ImageVector by lazy {
        vector(
            viewport = 36f,
            GearBody,
        )
    }

    val Pen: ImageVector by lazy {
        vector(
            viewport = 36f,
            PenBody,
        )
    }

    val Person: ImageVector by lazy {
        vector(
            viewport = 24f,
            PersonHead,
            PersonBody,
        )
    }

    val Diamond: ImageVector by lazy {
        vector(
            viewport = 24f,
            DiamondBody,
        )
    }

    val DoorOut: ImageVector by lazy {
        vector(
            viewport = 24f,
            DoorOutArrow,
            DoorOutDoor,
        )
    }

    val ChevronRight: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M9 6L15 12L9 18L9 15.4L12.4 12L9 8.6Z"),
        )
    }

    val ArrowLeft: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape(
                "M16.0605 2.45401C16.3418 2.7353 16.4997 3.11676 16.4997 3.51451C16.4997 3.91226 16.3418 4.29372 " +
                    "16.0605 4.57501L8.6355 12L16.0605 19.425C16.3337 19.7079 16.4849 20.0868 16.4815 20.4801" +
                    "C16.4781 20.8734 16.3203 21.2496 16.0422 21.5277C15.7641 21.8059 15.3879 21.9636 14.9946 21.967" +
                    "C14.6013 21.9704 14.2224 21.8192 13.9395 21.546L5.454 13.0605C5.17279 12.7792 5.01482 12.3978 " +
                    "5.01482 12C5.01482 11.6023 5.17279 11.2208 5.454 10.9395L13.9395 2.45401C14.2208 2.1728 " +
                    "14.6022 2.01483 15 2.01483C15.3977 2.01483 15.7792 2.1728 16.0605 2.45401Z",
            ),
        )
    }

    val Plus: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape(
                "M11 5C11 4.44772 11.4477 4 12 4C12.5523 4 13 4.44772 13 5V11H19C19.5523 11 20 11.4477 20 12" +
                    "C20 12.5523 19.5523 13 19 13H13V19C13 19.5523 12.5523 20 12 20C11.4477 20 11 19.5523 11 19V13" +
                    "H5C4.44772 13 4 12.5523 4 12C4 11.4477 4.44772 11 5 11H11V5Z",
            ),
        )
    }

    val Trashbin: ImageVector by lazy {
        vector(
            viewport = 24f,
            TrashbinLid,
            TrashbinLidStroke,
            TrashbinHandle,
            TrashbinBarLeft,
            TrashbinBarRight,
        )
    }

    val Play: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape(
                "M8 5.14v13.72c0 .79.87 1.27 1.53.81l10.05-6.86c.56-.38.56-1.24 0-1.62" +
                    "L9.53 4.33C8.87 3.87 8 4.35 8 5.14Z",
            ),
        )
    }

    val Cross: ImageVector by lazy {
        ImageVector.Builder(
            name = "app_icon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 25f,
            viewportHeight = 25f,
        ).apply {
            addGroup(translationX = -469f, translationY = -1041f)
            addPath(
                pathData = PathParser().parsePathString(CrossBody).toNodes(),
                fill = SolidColor(Color.Black),
            )
            clearGroup()
        }.build()
    }

    val People: ImageVector by lazy {
        vector(
            viewport = 32f,
            PeopleHead,
            PeopleBody,
        )
    }

    val Joystick: ImageVector by lazy {
        vectorSized(
            viewportWidth = 36f,
            viewportHeight = 33f,
            VectorShape(JoystickBody),
        )
    }

    val ArrowRight: ImageVector by lazy {
        vector(
            viewport = 24f,
            ArrowRightBody,
        )
    }

    val Eye: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape(
                "M12 4.5C7 4.5 3 9 2 12c1 3 5 7.5 10 7.5S21 15 22 12c-1-3-5-7.5-10-7.5z" +
                    "m0 13a4.5 4.5 0 1 1 0-9 4.5 4.5 0 0 1 0 9z" +
                    "m0-2.5a2 2 0 1 0 0-4 2 2 0 0 0 0 4z",
                PathFillType.EvenOdd,
            ),
        )
    }

    val Expand: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5", strokeWidth = 2f),
        )
    }

    val Collapse: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M9 4v5H4M15 4v5h5M9 20v-5H4M15 20v-5h5", strokeWidth = 2f),
        )
    }

    val Copy: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M9 9h10v10H9z", strokeWidth = 2f),
            VectorShape("M6 15H4V4h12v2", strokeWidth = 2f),
        )
    }

    val Users: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M8 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z", PathFillType.EvenOdd),
            VectorShape("M1.5 20a6.5 6.5 0 0 1 13 0Z", PathFillType.EvenOdd),
            VectorShape("M16 11.5a3 3 0 1 0 0-6", strokeWidth = 2f),
            VectorShape("M17.5 13.5c3 0 5 2.3 5 6h-4.6", strokeWidth = 2f),
        )
    }

    val CloudOff: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M3 3l18 18", strokeWidth = 2f),
            VectorShape(
                "M7.5 18a4.5 4.5 0 0 1-.4-8.98M10.5 18h7a3.5 3.5 0 0 0 2.2-6.22M9 9a5.5 5.5 0 0 1 8 4.3",
                strokeWidth = 2f,
            ),
        )
    }

    val Refresh: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M20 12a8 8 0 1 1-2.34-5.66", strokeWidth = 2f),
            VectorShape("M20 4v5h-5", strokeWidth = 2f),
        )
    }

    val Search: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M10.5 4a6.5 6.5 0 1 0 0 13 6.5 6.5 0 0 0 0-13", strokeWidth = 2f),
            VectorShape("M15.4 15.4L21 21", strokeWidth = 2f),
        )
    }

    val Close: ImageVector by lazy {
        vector(
            viewport = 24f,
            VectorShape("M5 5l14 14M19 5L5 19", strokeWidth = 2f),
        )
    }
}

private class VectorShape(
    val data: String,
    val fillType: PathFillType = PathFillType.NonZero,
    val strokeWidth: Float? = null,
)

private fun vector(
    viewport: Float,
    vararg shapes: VectorShape,
): ImageVector = vectorSized(
    viewportWidth = viewport,
    viewportHeight = viewport,
    shapes = shapes,
)

private fun vectorSized(
    viewportWidth: Float,
    viewportHeight: Float,
    vararg shapes: VectorShape,
): ImageVector = ImageVector.Builder(
    name = "app_icon",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = viewportWidth,
    viewportHeight = viewportHeight,
).apply {
    shapes.forEach { shape ->
        val nodes = PathParser().parsePathString(shape.data).toNodes()
        if (shape.strokeWidth == null) {
            addPath(
                pathData = nodes,
                pathFillType = shape.fillType,
                fill = SolidColor(Color.Black),
            )
        } else {
            addPath(
                pathData = nodes,
                pathFillType = shape.fillType,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = shape.strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
}.build()

private val BellBody = VectorShape(
    "M7.25036 4.66355C8.1201 3.36729 9.62323 2 12.0001 2C14.377 2 15.8802 3.36729 16.7499 4.66355" +
        "C17.6075 5.94174 17.9254 7.2474 17.9712 7.44762C17.9776 7.47579 17.9823 7.49925 17.9854 7.51539" +
        "L19.5201 15.4288C19.8792 17.2803 18.461 19 16.575 19H7.42528C5.53928 19 4.12108 17.2803 4.48016 15.4288" +
        "L6.01489 7.51539C6.01802 7.49925 6.02265 7.47579 6.0291 7.44762C6.07491 7.2474 6.39275 5.94174 7.25036 4.66355Z",
    PathFillType.EvenOdd,
)

private val BellClapper = VectorShape(
    "M8.51367 20C8.76564 20.4807 9.1515 20.9831 9.73654 21.3672C10.3463 21.7675 11.1131 22 12.0466 22" +
        "C12.98 22 13.7468 21.7675 14.3566 21.3672C14.9416 20.9831 15.3275 20.4807 15.5795 20H8.51367Z",
    PathFillType.EvenOdd,
)

private val ChatBody = VectorShape(
    "M22 10.787c0 4.854-4.253 8.788-9.5 8.788a10.22 10.22 0 0 1-2.684-.355c-1.881 1.241-4.233 1.617-5.485 1.73" +
        "-.286.027-.434-.347-.24-.559.595-.65 1.431-1.803 1.715-3.368C4.073 15.43 3 13.225 3 10.787 3 5.934 7.253 2 12.5 2" +
        "S22 5.934 22 10.787z",
    PathFillType.EvenOdd,
)

private val UnityTopCircle = VectorShape(
    "M250.399 32.855A58.694 58.694 0 1 0 250.399 150.243A58.694 58.694 0 1 0 250.399 32.855Z",
)

private val UnityBody = VectorShape(
    "M455.861,253.028l-54.703-11.411c-18.637-3.904-37.037,4.638-46.765,19.824 " +
        "c-9.448-4.853-19.608-9.038-30.415-12.511v-32.529c0.022-24.612-20.126-44.738-44.651-44.738h-55.933 " +
        "c-24.655,0-44.716,20.126-44.716,44.738v32.701c-10.699,3.408-20.751,7.593-30.264,12.468 " +
        "c-9.728-15.251-28.15-23.857-46.809-19.953l-54.747,11.411c-24.03,5.026-39.626,28.862-34.6,52.978l13.741,65.64 " +
        "c4.983,24.051,28.84,39.647,52.892,34.621l17.321-3.624c8.671,12.813,20.665,24.569,36.023,34.621 " +
        "c31.989,20.967,74.247,32.529,119.092,32.529c68.617,0,127.721-27.589,154.943-67.215l17.602,3.689 " +
        "c24.03,5.004,47.887-10.57,52.87-34.621l13.762-65.64C495.508,281.89,479.912,258.054,455.861,253.028z " +
        "M251.305,447.381 c-40.51,0-78.475-10.203-106.797-28.862c-9.707-6.342-17.753-13.395-24.202-20.945l13.266-2.783 " +
        "c24.073-5.004,39.669-28.84,34.643-52.913l-12.317-59.018c7.183-3.861,14.733-7.248,22.757-10.138v10.764 " +
        "c0,24.569,20.104,44.695,44.716,44.695h55.933c24.548,0,44.652-20.147,44.652-44.695v-11.325 " +
        "c8.175,2.912,15.854,6.256,22.973,10.052L334.439,341.9c-4.983,24.073,10.591,47.909,34.664,52.913l13.395,2.804 " +
        "C357.52,427.191,308.101,447.381,251.305,447.381z",
)

private val UnitySideCircle = VectorShape(
    "M443.954 110.014A58.694 58.694 0 1 0 443.954 227.402A58.694 58.694 0 1 0 443.954 110.014Z",
)

private val UnityLeftCircle = VectorShape(
    "M70.736,226.172c31.752-6.644,52.029-37.77,45.471-69.501 " +
        "c-6.687-31.709-37.749-52.072-69.523-45.428c-31.709,6.622-52.072,37.727-45.428,69.458 " +
        "C7.879,212.453,38.984,232.795,70.736,226.172z",
)

private val GamepadBody = VectorShape(
    "M22.7879 10.1818H17.4545V4.84847C17.4545 4.04517 16.8033 3.39392 16 3.39392C15.1967 3.39392 14.5455 4.04517 " +
        "14.5455 4.84847V10.1818H9.21212C4.13256 10.1818 0 14.3144 0 19.3939C0 24.4735 4.13256 28.606 9.21212 28.606" +
        "C11.266 28.606 13.2518 27.921 14.8639 26.6666H17.1362C18.7482 27.921 20.734 28.606 22.788 28.606" +
        "C27.8675 28.606 32.0001 24.4735 32.0001 19.3939C32.0001 14.3144 27.8674 10.1818 22.7879 10.1818z" +
        "M22.7879 25.697C21.2527 25.697 19.7732 25.1374 18.6221 24.1215C18.3563 23.887 18.0141 23.7576 17.6596 23.7576" +
        "H14.3405C13.986 23.7576 13.6437 23.887 13.378 24.1215C12.2268 25.1374 10.7473 25.697 9.21212 25.697" +
        "C5.73663 25.697 2.90909 22.8694 2.90909 19.3939C2.90909 15.9184 5.73663 13.0909 9.21212 13.0909H22.7879" +
        "C26.2634 13.0909 29.0909 15.9184 29.0909 19.3939C29.0909 22.8694 26.2634 25.697 22.7879 25.697z",
)

private val GamepadDpad = VectorShape(
    "M11.1515 17.9394H10.6667V17.4545C10.6667 16.6512 10.0154 16 9.21212 16C8.40882 16 7.75757 16.6512 7.75757 17.4545" +
        "V17.9394H7.27272C6.46942 17.9394 5.81818 18.5906 5.81818 19.3939C5.81818 20.1972 6.46942 20.8485 7.27272 20.8485" +
        "H7.75757V21.3333C7.75757 22.1366 8.40882 22.7879 9.21212 22.7879C10.0154 22.7879 10.6667 22.1366 10.6667 21.3333" +
        "V20.8485H11.1515C11.9548 20.8485 12.6061 20.1972 12.6061 19.3939C12.6061 18.5906 11.9548 17.9394 11.1515 17.9394z",
)

private val GamepadAction = VectorShape(
    "M23.1307 16.9941L20.3948 19.73C20.3925 19.7322 20.39 19.7341 20.3879 19.7362C20.3533 19.7708 20.321 19.8071 " +
        "20.2903 19.8449C19.8438 20.3897 19.854 21.1823 20.3186 21.7171C20.3411 21.7432 20.3634 21.7695 20.3879 21.7939" +
        "C20.422 21.8281 20.4579 21.8604 20.4951 21.8907C20.7623 22.1101 21.0893 22.2199 21.4164 22.2199" +
        "C21.7432 22.2199 22.0698 22.1104 22.3369 21.8915C22.3746 21.8609 22.4109 21.8285 22.4455 21.7939" +
        "C22.4476 21.7917 22.4494 21.7894 22.4516 21.7873L25.1877 19.0512C25.7557 18.4832 25.7557 17.5622 25.1877 16.9941" +
        "C24.6197 16.4262 23.6987 16.4262 23.1307 16.9941z",
)

private val EmojiFace = VectorShape(
    "M16,2C8.3,2,2,8.3,2,16s6.3,14,14,14s14-6.3,14-14S23.7,2,16,2z " +
        "M20,11c1.7,0,3,1.3,3,3c0,0.6-0.4,1-1,1s-1-0.4-1-1 c0-0.6-0.4-1-1-1s-1,0.4-1,1c0,0.6-0.4,1-1,1s-1-0.4-1-1" +
        "C17,12.3,18.3,11,20,11z " +
        "M12,11c1.7,0,3,1.3,3,3c0,0.6-0.4,1-1,1s-1-0.4-1-1 c0-0.6-0.4-1-1-1s-1,0.4-1,1c0,0.6-0.4,1-1,1s-1-0.4-1-1" +
        "C9,12.3,10.3,11,12,11z " +
        "M16,25c-4.4,0-8-3.6-8-8c0-0.1,0-0.2,0-0.3 c0-0.3,0.2-0.7,0.5-0.9c0.3-0.2,0.6-0.3,0.9-0.1" +
        "c1.9,0.8,4.2,1.2,6.6,1.2s4.7-0.4,6.6-1.2c0.3-0.1,0.6-0.1,0.9,0.1 c0.3,0.2,0.4,0.5,0.5,0.8c0,0.2,0,0.3,0,0.4" +
        "C24,21.4,20.4,25,16,25z",
)

private val GearBody = VectorShape(
    "M19.1772 0.674927C20.7358 0.674927 22.0751 1.74814 22.3683 3.23238L22.4986 3.89238" +
        "C23.0112 6.48658 25.7789 8.03578 28.3524 7.16905L29.0075 6.94845C30.4799 6.45258 32.1081 7.04032 32.8874 8.34903" +
        "L34.0645 10.3257C34.8439 11.6345 34.5548 13.2954 33.3756 14.2837L32.8512 14.7233" +
        "C30.7901 16.4508 30.7901 19.5492 32.8512 21.2766L33.3756 21.7161C34.5548 22.7044 34.8439 24.3654 34.0645 25.6741" +
        "L32.8875 27.6508C32.1081 28.9596 30.4798 29.5473 29.0073 29.0514L28.3525 28.8307" +
        "C25.7788 27.9639 23.0112 29.5131 22.4986 32.1075L22.3683 32.7675C22.0751 34.2517 20.7358 35.325 19.1772 35.325" +
        "H16.8228C15.2642 35.325 13.925 34.2517 13.6317 32.7676L13.5012 32.1072" +
        "C12.9886 29.5131 10.2209 27.9639 7.64733 28.8307L6.99268 29.0512C5.52024 29.5473 3.89197 28.9594 3.11257 27.6507" +
        "L1.93545 25.6741C1.15608 24.3654 1.44517 22.7044 2.62434 21.7161L3.14883 21.2766" +
        "C5.20986 19.549 5.20986 16.4508 3.14883 14.7234L2.62434 14.2838C1.44517 13.2954 1.15608 11.6345 1.93545 10.3258" +
        "L3.11262 8.34907C3.89199 7.04034 5.52021 6.45259 6.99264 6.94849L7.64746 7.16904" +
        "C10.221 8.03578 12.9886 6.48669 13.5012 3.89254L13.6317 3.23226C13.925 1.74808 15.264 0.674927 16.8228 0.674927" +
        "H19.1772Z" +
        "M18 22.95C20.7337 22.95 22.95 20.7337 22.95 18C22.95 15.2661 20.7337 13.0499 18 13.0499" +
        "C15.2661 13.0499 13.05 15.2661 13.05 18C13.05 20.7337 15.2661 22.95 18 22.95Z",
)

private val PenBody = VectorShape(
    "M20.4421 6.55633L29.4435 15.5577L9.89734 35.1039L1.87187 35.9898" +
        "C0.797498 36.1087 -0.110236 35.2002 0.00929512 34.1259L0.902264 26.0948L20.4421 6.55633Z" +
        "M35.0109 5.21617L30.7844 0.989685C29.466 -0.328674 27.3278 -0.328674 26.0095 0.989685" +
        "L22.0333 4.96586L31.0347 13.9673L35.0109 9.99109C36.3292 8.67203 36.3292 6.53453 35.0109 5.21617Z",
)

private val PersonHead = VectorShape(
    "M12 2A4 4 0 1 0 12 10A4 4 0 1 0 12 2Z",
    PathFillType.EvenOdd,
)

private val PersonBody = VectorShape(
    "M12 11a6 6 0 0 1 6 6v1H6v-1a6 6 0 0 1 6-6z",
    PathFillType.EvenOdd,
)

private val DiamondBody = VectorShape(
    "M15.9575 3H8.04251H8.04245C7.56651 2.99998 7.14462 2.99996 6.78932 3.0324" +
        "C6.4039 3.0676 6.02795 3.14535 5.65587 3.33907C5.2838 3.53279 5.00451 3.7962 4.75466 4.09176" +
        "C4.52432 4.36424 4.28239 4.70989 4.00946 5.09982L3.98646 5.13269L3.38928 5.9858L3.35846 6.02983" +
        "C2.97332 6.57997 2.63623 7.06146 2.40507 7.48985C2.15531 7.9527 1.97719 8.44362 1.99402 9.01492" +
        "C2.01085 9.58621 2.21757 10.0658 2.49415 10.5131C2.75014 10.9272 3.11499 11.388 3.53186 11.9145" +
        "L8.86406 18.6499L8.86416 18.65C9.21107 19.0883 9.50591 19.4607 9.7686 19.7453" +
        "C10.0411 20.0406 10.3437 20.3151 10.7291 20.4954C11.5345 20.872 12.4655 20.872 13.2709 20.4954" +
        "C13.6563 20.3151 13.9589 20.0406 14.2314 19.7453C14.4941 19.4607 14.789 19.0882 15.1359 18.6499" +
        "L20.4681 11.9145L20.4682 11.9145C20.885 11.388 21.2499 10.9272 21.5058 10.5131" +
        "C21.7824 10.0658 21.9891 9.58621 22.006 9.01492C22.0228 8.44362 21.8447 7.9527 21.5949 7.48985" +
        "C21.3638 7.06146 21.0267 6.57997 20.6415 6.02983L19.9905 5.09985C19.7176 4.7099 19.4757 4.36424 " +
        "19.2453 4.09176C18.9955 3.7962 18.7162 3.53279 18.3441 3.33907C17.972 3.14535 17.5961 3.0676 " +
        "17.2107 3.0324C16.8554 2.99996 16.4335 2.99998 15.9575 3Z" +
        "M9.83205 8.0547C10.1384 7.59517 10.0142 6.9743 9.5547 6.66795C9.09517 6.3616 8.4743 6.48577 8.16795 6.9453" +
        "L7.66795 7.6953C7.12573 8.50863 7.18306 9.58172 7.80884 10.3327L9.73178 12.6402" +
        "C10.0853 13.0645 10.7159 13.1218 11.1402 12.7682C11.5645 12.4147 11.6218 11.7841 11.2682 11.3598" +
        "L9.34528 9.05229C9.28653 8.98179 9.28115 8.88105 9.33205 8.8047L9.83205 8.0547Z",
    PathFillType.EvenOdd,
)

private val DoorOutArrow = VectorShape(
    "M17.2929 14.2929C16.9024 14.6834 16.9024 15.3166 17.2929 15.7071C17.6834 16.0976 18.3166 16.0976 18.7071 15.7071" +
        "L21.6201 12.7941C21.6351 12.7791 21.6497 12.7637 21.6637 12.748C21.87 12.5648 22 12.2976 22 12" +
        "C22 11.7024 21.87 11.4352 21.6637 11.252C21.6497 11.2363 21.6351 11.2209 21.6201 11.2059L18.7071 8.29289" +
        "C18.3166 7.90237 17.6834 7.90237 17.2929 8.29289C16.9024 8.68342 16.9024 9.31658 17.2929 9.70711" +
        "L18.5858 11H13C12.4477 11 12 11.4477 12 12C12 12.5523 12.4477 13 13 13H18.5858L17.2929 14.2929Z",
)

private val DoorOutDoor = VectorShape(
    "M5 2C3.34315 2 2 3.34315 2 5V19C2 20.6569 3.34315 22 5 22H14.5C15.8807 22 17 20.8807 17 19.5V16.7326" +
        "C16.8519 16.647 16.7125 16.5409 16.5858 16.4142C15.9314 15.7598 15.8253 14.7649 16.2674 14H13" +
        "C11.8954 14 11 13.1046 11 12C11 10.8954 11.8954 10 13 10H16.2674C15.8253 9.23514 15.9314 8.24015 " +
        "16.5858 7.58579C16.7125 7.4591 16.8519 7.35296 17 7.26738V4.5C17 3.11929 15.8807 2 14.5 2H5Z",
)

private val TrashbinLid = VectorShape(
    "M3 6H21",
    strokeWidth = 2f,
)

private val TrashbinLidStroke = VectorShape(
    "M19 6V20C19 21.1046 18.1046 22 17 22H7C5.89543 22 5 21.1046 5 20V6",
    strokeWidth = 2f,
)

private val TrashbinHandle = VectorShape(
    "M8 6V4C8 2.89543 8.89543 2 10 2H14C15.1046 2 16 2.89543 16 4V6",
    strokeWidth = 2f,
)

private val TrashbinBarLeft = VectorShape(
    "M10 11V17",
    strokeWidth = 2f,
)

private val TrashbinBarRight = VectorShape(
    "M14 11V17",
    strokeWidth = 2f,
)


private const val CrossBody = "M487.148,1053.48 L492.813,1047.82 C494.376,1046.26 494.376,1043.72 492.813,1042.16" +
    " C491.248,1040.59 488.712,1040.59 487.148,1042.16 L481.484,1047.82 L475.82,1042.16" +
    " C474.257,1040.59 471.721,1040.59 470.156,1042.16 C468.593,1043.72 468.593,1046.26 470.156,1047.82" +
    " L475.82,1053.48 L470.156,1059.15 C468.593,1060.71 468.593,1063.25 470.156,1064.81" +
    " C471.721,1066.38 474.257,1066.38 475.82,1064.81 L481.484,1059.15 L487.148,1064.81" +
    " C488.712,1066.38 491.248,1066.38 492.813,1064.81 C494.376,1063.25 494.376,1060.71 492.813,1059.15" +
    " L487.148,1053.48"

private val PeopleHead = VectorShape(
    "M9.66667 10.3333C9.66667 12.013 10.3339 13.6239 11.5217 14.8117C12.7094 15.9994 14.3203 16.6667 16 16.6667" +
        "C17.6797 16.6667 19.2906 15.9994 20.4783 14.8117C21.6661 13.6239 22.3333 12.013 22.3333 10.3333" +
        "C22.3333 8.65363 21.6661 7.04272 20.4783 5.85499C19.2906 4.66726 17.6797 4 16 4" +
        "C14.3203 4 12.7094 4.66726 11.5217 5.85499C10.3339 7.04272 9.66667 8.65363 9.66667 10.3333Z",
)

private val PeopleBody = VectorShape(
    "M26 27.3333C26 24.6811 24.9464 22.1376 23.0711 20.2622C21.1957 18.3869 18.6522 17.3333 16 17.3333" +
        "C13.3478 17.3333 10.8043 18.3869 8.92893 20.2622C7.05357 22.1376 6 24.6811 6 27.3333" +
        "C6 27.5101 6.07024 27.6797 6.19526 27.8047C6.32029 27.9297 6.48986 28 6.66667 28H25.3333" +
        "C25.5101 28 25.6797 27.9297 25.8047 27.8047C25.9298 27.6797 26 27.5101 26 27.3333Z",
)

private const val JoystickBody =
    "M32.979 11.9001C32.9756 11.8511 32.9716 11.8022 32.967 11.7534C32.834 9.65204 31.9901 7.68449 30.6049 6.24618" +
        "C29.2197 4.80788 27.3959 4.00542 25.5 4.00008H10.5C8.5775 3.99043 6.72597 4.80637 5.33205 6.27752" +
        "C3.93813 7.74867 3.10961 9.76128 3.0195 11.8951C3.0045 12.0267 3 12.1634 3 12.3167V23.1667" +
        "C3 24.7083 3.5595 26.2133 4.533 27.2933C5.52121 28.3842 6.85701 28.9975 8.25 29C10.95 29 12.006 27.3333 13.5 24" +
        "C13.719 23.5134 14.988 20.6667 18 20.6667C20.994 20.6667 22.2795 23.5117 22.5 24C24.006 27.3333 25.05 29 27.75 29" +
        "C29.1375 29 30.492 28.3783 31.464 27.2967C32.4458 26.1987 32.9978 24.7144 33 23.1667V12.3334" +
        "C33 12.1751 32.994 12.0334 32.979 11.9001ZM10.5 15.7184C10.1005 15.7284 9.7032 15.6497 9.33146 15.4868" +
        "C8.95972 15.3239 8.62102 15.0801 8.33524 14.7697C8.04946 14.4594 7.82237 14.0887 7.66729 13.6795" +
        "C7.51221 13.2703 7.43228 12.8308 7.43218 12.3867C7.43208 11.9427 7.51181 11.5032 7.66671 11.0939" +
        "C7.8216 10.6846 8.04853 10.3138 8.33417 10.0033C8.61981 9.69276 8.9584 9.44878 9.33007 9.28567" +
        "C9.70174 9.12255 10.099 9.04358 10.4985 9.0534C11.2823 9.07266 12.0283 9.43211 12.5766 10.0548" +
        "C13.1249 10.6775 13.432 11.5139 13.4322 12.3851C13.4324 13.2562 13.1256 14.0928 12.5776 14.7158" +
        "C12.0296 15.3388 11.2838 15.6987 10.5 15.7184ZM25.5 7.38507C25.8978 7.38507 26.2794 7.56067 26.5607 7.87323" +
        "C26.842 8.18579 27 8.60971 27 9.05173C27 9.49376 26.842 9.91768 26.5607 10.2302C26.2794 10.5428 25.8978 10.7184 25.5 10.7184" +
        "C25.1022 10.7184 24.7206 10.5428 24.4393 10.2302C24.158 9.91768 24 9.49376 24 9.05173" +
        "C24 8.60971 24.158 8.18579 24.4393 7.87323C24.7206 7.56067 25.1022 7.38507 25.5 7.38507ZM22.5 14.0517" +
        "C22.1022 14.0517 21.7206 13.8761 21.4393 13.5636C21.158 13.251 21 12.8271 21 12.3851C21 11.943 21.158 11.5191" +
        "21.4393 11.2065C21.7206 10.894 22.1022 10.7184 22.5 10.7184C22.8978 10.7184 23.2794 10.894 23.5607 11.2065" +
        "C23.842 11.5191 24 11.943 24 12.3851C24 12.8271 23.842 13.251 23.5607 13.5636C23.2794 13.8761 22.8978 14.0517 22.5 14.0517Z" +
        "M25.5 17.385C25.1022 17.385 24.7206 17.2094 24.4393 16.8969C24.158 16.5843 24 16.1604 24 15.7184" +
        "C24 15.2764 24.158 14.8524 24.4393 14.5399C24.7206 14.2273 25.1022 14.0517 25.5 14.0517" +
        "C25.8978 14.0517 26.2794 14.2273 26.5607 14.5399C26.842 14.8524 27 15.2764 27 15.7184" +
        "C27 16.1604 26.842 16.5843 26.5607 16.8969C26.2794 17.2094 25.8978 17.385 25.5 17.385ZM28.5 14.0517" +
        "C28.1022 14.0517 27.7206 13.8761 27.4393 13.5636C27.158 13.251 27 12.8271 27 12.3851C27 11.943 27.158 11.5191" +
        "27.4393 11.2065C27.7206 10.894 28.1022 10.7184 28.5 10.7184C28.8978 10.7184 29.2794 10.894 29.5607 11.2065" +
        "C29.842 11.5191 30 11.943 30 12.3851C30 12.8271 29.842 13.251 29.5607 13.5636C29.2794 13.8761 28.8978 14.0517 28.5 14.0517Z"

private val ArrowRightBody = VectorShape(
    "M7.9395 2.45401C7.65829 2.7353 7.50032 3.11676 7.50032 3.51451C7.50032 3.91226 7.65829 4.29372 7.9395 4.57501" +
        "L15.3645 12L7.9395 19.425C7.66626 19.7079 7.51507 20.0868 7.51849 20.4801C7.52191 20.8734 7.67966 21.2496" +
        " 7.95777 21.5277C8.23588 21.8059 8.6121 21.9636 9.0054 21.967C9.39869 21.9704 9.7776 21.8192 10.0605 21.546" +
        "L18.546 13.0605C18.8272 12.7792 18.9852 12.3978 18.9852 12C18.9852 11.6023 18.8272 11.2208 18.546 10.9395" +
        "L10.0605 2.45401C9.77921 2.1728 9.39775 2.01483 9 2.01483C8.60225 2.01483 8.22079 2.1728 7.9395 2.45401Z",
)
