package ru.fet1sov.akinorev.feature.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.auth.textRes
import ru.fet1sov.akinorev.ui.auth.AuthErrorBanner
import ru.fet1sov.akinorev.ui.auth.AuthPrimaryButton
import ru.fet1sov.akinorev.ui.auth.AuthScaffold
import ru.fet1sov.akinorev.ui.auth.AuthTextField
import ru.fet1sov.akinorev.ui.auth.authReveal
import ru.fet1sov.akinorev.ui.icons.AuthIcons
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()

    AuthScaffold(
        title = stringResource(R.string.auth_login_title),
        subtitle = stringResource(R.string.auth_login_subtitle),
        modifier = modifier,
    ) {
        if (uiState.isInitializing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 2.dp,
                    color = palette.accent,
                )
            }
            return@AuthScaffold
        }

        Column(modifier = Modifier.authReveal(index = 0)) {
            AuthTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = stringResource(R.string.auth_field_username),
                placeholder = stringResource(R.string.auth_placeholder_username),
                icon = AuthIcons.Person,
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.authReveal(index = 1)) {
            AuthTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.auth_field_password),
                placeholder = stringResource(R.string.auth_placeholder_password),
                icon = AuthIcons.Lock,
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailing = {
                    PasswordVisibilityToggle(
                        visible = uiState.isPasswordVisible,
                        onToggle = onTogglePasswordVisibility,
                    )
                },
            )
        }

        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn() + slideInVertically { -it / 3 },
            exit = fadeOut() + slideOutVertically { -it / 3 },
        ) {
            val message = uiState.error?.let { stringResource(it.textRes) }
            if (message != null) {
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    AuthErrorBanner(message = message)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.authReveal(index = 2)) {
            AuthPrimaryButton(
                text = stringResource(R.string.auth_login_action),
                onClick = onSubmit,
                isLoading = uiState.isSubmitting,
                enabled = uiState.canSubmit,
                icon = AuthIcons.ArrowForward,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.auth_login_switch),
            color = palette.textMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .authReveal(index = 3)
                .clickable(enabled = !uiState.isSubmitting, onClick = onNavigateToRegister)
                .padding(vertical = 6.dp),
        )
    }
}

@Composable
internal fun PasswordVisibilityToggle(
    visible: Boolean,
    onToggle: () -> Unit,
) {
    val palette = navigationPalette()

    Icon(
        imageVector = if (visible) AuthIcons.EyeOff else AuthIcons.Eye,
        contentDescription = stringResource(
            if (visible) R.string.auth_hide_password else R.string.auth_show_password,
        ),
        tint = palette.textMuted,
        modifier = Modifier
            .size(20.dp)
            .clickable(onClick = onToggle),
    )
}
