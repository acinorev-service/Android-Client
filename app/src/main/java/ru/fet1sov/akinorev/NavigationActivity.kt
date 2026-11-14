package ru.fet1sov.akinorev

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.feature.session.SessionViewModel
import ru.fet1sov.akinorev.navigation.AcinorevApp
import ru.fet1sov.akinorev.ui.components.SessionGateScreen
import ru.fet1sov.akinorev.ui.system.applyAcinorevEdgeToEdge
import ru.fet1sov.akinorev.ui.theme.AkinorevTheme

class NavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyAcinorevEdgeToEdge()

        setContent {
            AkinorevTheme {
                val viewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory)
                val session by viewModel.state.collectAsStateWithLifecycle()

                when (session) {
                    SessionState.Loading -> SessionGateScreen()

                    SessionState.Guest -> {
                        LaunchedEffect(Unit) { openLogin() }
                        SessionGateScreen()
                    }

                    is SessionState.Authorized -> AcinorevApp()
                }
            }
        }
    }

    private fun openLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }
}
