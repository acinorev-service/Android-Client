package ru.fet1sov.akinorev

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.feature.session.SessionViewModel
import ru.fet1sov.akinorev.navigation.AcinorevApp
import ru.fet1sov.akinorev.push.PushConfig
import ru.fet1sov.akinorev.ui.components.SessionGateScreen
import ru.fet1sov.akinorev.ui.system.AppLocale
import ru.fet1sov.akinorev.ui.system.applyAcinorevEdgeToEdge
import ru.fet1sov.akinorev.ui.theme.AcinorevAppTheme

class NavigationActivity : ComponentActivity() {
    private val openNotifications = MutableStateFlow(false)

    override fun attachBaseContext(newBase: Context) {
        val settings = (newBase.applicationContext as AcinorevApplication).container.appSettingsRepository
        super.attachBaseContext(AppLocale.wrap(newBase, settings.settings.value.language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyAcinorevEdgeToEdge()
        openNotifications.value = intent?.wantsNotifications() ?: false

        val container = (application as AcinorevApplication).container

        setContent {
            AcinorevAppTheme(settingsRepository = container.appSettingsRepository) {
                val viewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory)
                val session by viewModel.state.collectAsStateWithLifecycle()
                val startOnNotifications by openNotifications.collectAsStateWithLifecycle()

                when (session) {
                    SessionState.Loading -> SessionGateScreen()

                    SessionState.Guest -> {
                        LaunchedEffect(Unit) { openLogin() }
                        SessionGateScreen()
                    }

                    is SessionState.Authorized -> AcinorevApp(
                        startOnNotifications = startOnNotifications,
                        onStartNavigationConsumed = { openNotifications.value = false },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.wantsNotifications()) {
            openNotifications.value = true
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

    private fun Intent.wantsNotifications(): Boolean =
        getBooleanExtra(PushConfig.ExtraOpenNotifications, false)
}
