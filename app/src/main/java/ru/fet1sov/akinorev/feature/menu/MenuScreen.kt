package ru.fet1sov.akinorev.feature.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.icons.AuthIcons
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val initial = uiState.username.trim().take(1).uppercase().ifBlank { "?" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(palette.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                color = palette.accent,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = uiState.username.ifBlank { stringResource(R.string.app_name) },
            color = palette.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        if (uiState.email.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.email,
                color = palette.textMuted,
                fontSize = 13.sp,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.menu_account_signed_in),
            color = palette.textMuted,
            fontSize = 12.sp,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            onClick = viewModel::logout,
            enabled = !uiState.isLoggingOut,
            shape = RoundedCornerShape(14.dp),
            color = palette.bar,
            border = BorderStroke(1.dp, palette.barBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (uiState.isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = palette.accent,
                    )
                } else {
                    Icon(
                        imageVector = AuthIcons.Logout,
                        contentDescription = null,
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.menu_logout),
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
