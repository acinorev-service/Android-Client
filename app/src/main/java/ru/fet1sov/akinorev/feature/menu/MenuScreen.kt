package ru.fet1sov.akinorev.feature.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.ProfileHeader
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.components.SubscriptionCard
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import ru.fet1sov.akinorev.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onOpenEntry: (MenuEntry) -> Unit,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val sheetState = rememberModalBottomSheetState()
    var isSubscriptionSheetVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        ProfileHeader(
            profile = uiState.profile,
            isLoading = uiState.isAuthPending,
            onClick = { uiState.profile?.username?.let(onOpenProfile) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isAuthPending) {
            repeat(MenuSkeletonRows) { index ->
                MenuRowSkeleton(modifier = Modifier.padding(top = if (index == 0) 0.dp else 8.dp))
            }
        } else {
            SubscriptionCard(
                isSubscribed = uiState.profile?.isSubscribed == true,
                expiry = uiState.subscriptionExpiry,
                onClick = if (uiState.profile?.isSubscribed == true) {
                    null
                } else {
                    { isSubscriptionSheetVisible = true }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            uiState.entries.forEach { entry ->
                MenuRow(
                    entry = entry,
                    palette = palette,
                    onClick = { onOpenEntry(entry) },
                )
            }

            if (uiState.profile != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LogoutRow(
                    isLoggingOut = uiState.isLoggingOut,
                    palette = palette,
                    onClick = viewModel::logout,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (isSubscriptionSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isSubscriptionSheetVisible = false },
            sheetState = sheetState,
            containerColor = palette.bar,
        ) {
            Column(modifier = Modifier.padding(horizontal = 30.dp)) {
                Text(
                    text = stringResource(R.string.subscription_title),
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.subscription_android_note),
                    color = palette.textMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MenuRow(
    entry: MenuEntry,
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                kind = entry.icon,
                tint = palette.textPrimary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(entry.labelRes),
                color = palette.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LogoutRow(
    isLoggingOut: Boolean,
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = !isLoggingOut,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = ErrorRed,
                )
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                AppIcon(
                    kind = AppIconKind.Logout,
                    tint = ErrorRed,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = stringResource(R.string.menu_logout),
                color = ErrorRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MenuRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        SkeletonBlock(height = 28.dp, width = 28.dp, shape = RoundedCornerShape(percent = 50))
        Spacer(modifier = Modifier.width(8.dp))
        SkeletonBlock(height = 16.dp, width = 140.dp)
    }
}

private const val MenuSkeletonRows = 4
