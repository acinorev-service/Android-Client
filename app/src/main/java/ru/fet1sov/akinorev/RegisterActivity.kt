package ru.fet1sov.akinorev

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.feature.auth.register.RegisterScreen
import ru.fet1sov.akinorev.feature.auth.register.RegisterViewModel
import ru.fet1sov.akinorev.ui.system.applyAcinorevEdgeToEdge
import ru.fet1sov.akinorev.ui.theme.AkinorevTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyAcinorevEdgeToEdge()

        setContent {
            AkinorevTheme {
                val viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.isAuthenticated) {
                    if (uiState.isAuthenticated) openNavigation()
                }

                RegisterScreen(
                    uiState = uiState,
                    onUsernameChange = viewModel::onUsernameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onRepeatPasswordChange = viewModel::onRepeatPasswordChange,
                    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
                    onToggleRepeatPasswordVisibility = viewModel::onToggleRepeatPasswordVisibility,
                    onSubmit = viewModel::onSubmit,
                    onNavigateToLogin = ::closeRegister,
                )
            }
        }
    }

    private fun closeRegister() {
        finish()
    }

    private fun openNavigation() {
        startActivity(
            Intent(this, NavigationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }
}
