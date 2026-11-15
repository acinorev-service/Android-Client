package ru.fet1sov.akinorev.ui.icons

import org.junit.Assert.assertTrue
import org.junit.Test

class AppIconsTest {
    @Test
    fun vectors_areBuiltFromValidPaths() {
        val vectors = listOf(
            AppIcons.Bell,
            AppIcons.Chat,
            AppIcons.Unity,
            AppIcons.Gamepad,
            AppIcons.Emoji,
            AppIcons.Gear,
            AppIcons.Pen,
            AppIcons.Person,
            AppIcons.Diamond,
            AppIcons.DoorOut,
            AppIcons.ChevronRight,
            AppIcons.ArrowLeft,
            AppIcons.Plus,
            AppIcons.Trashbin,
            AppIcons.Phone,
            AppIcons.Desktop,
        )

        vectors.forEach { vector ->
            assertTrue(vector.name.isNotBlank())
            assertTrue(vector.defaultWidth.value > 0f)
            assertTrue(vector.defaultHeight.value > 0f)
            assertTrue(vector.viewportWidth > 0f)
            assertTrue(vector.viewportHeight > 0f)
            assertTrue(vector.root.size > 0)
        }
    }
}
