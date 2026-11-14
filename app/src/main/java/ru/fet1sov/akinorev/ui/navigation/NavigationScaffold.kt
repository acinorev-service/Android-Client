package ru.fet1sov.akinorev.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.navigation.NavigationUiState
import ru.fet1sov.akinorev.model.TopLevelDestination

@Composable
fun NavigationScaffold(
    uiState: NavigationUiState,
    selectedDestination: TopLevelDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = navigationPalette()
    val selectedIndex = selectedDestination?.let(uiState.destinations::indexOf) ?: -1
    val isBarVisible = selectedIndex >= 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.screen),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = palette.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.nav_caption),
                    color = palette.textMuted,
                    fontSize = 12.sp,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                content()
            }

            if (isBarVisible) {
                Spacer(modifier = Modifier.height(96.dp))
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

        if (isBarVisible) {
            AcinorevNavigationBar(
                items = uiState.destinations,
                selectedIndex = selectedIndex,
                onSelect = { index -> onDestinationSelected(uiState.destinations[index]) },
                badges = uiState.badges,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}
