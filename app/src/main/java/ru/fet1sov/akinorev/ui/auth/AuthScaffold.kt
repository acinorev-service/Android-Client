package ru.fet1sov.akinorev.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun AuthScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = navigationPalette()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.screen)
            .systemBarsPadding()
            .imePadding(),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val viewportHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = viewportHeight)
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = palette.bar,
                    border = BorderStroke(1.dp, palette.barBorder),
                    shadowElevation = 18.dp,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                        Text(
                            text = title,
                            color = palette.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 30.sp,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            color = palette.textMuted,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        content()
                    }
                }
            }
        }
    }
}
