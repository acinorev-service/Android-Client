package ru.fet1sov.akinorev.feature.auth.login

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.feature.auth.AuthMessage
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultAuthRepository(api, sessionManager)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun viewModel() = LoginViewModel(repository)

    @Test
    fun initialState_finishesRestoringSession() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isInitializing.not())
        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun submit_withBlankFields_showsEmptyFieldsError() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthMessage.EmptyFields, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun submit_withInvalidCredentials_showsServerError() = runTest(mainDispatcher) {
        api.onLogin = { ApiFixtures.error(401, "invalid_credentials") }
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUsernameChange("fet1sov")
        viewModel.onPasswordChange("secret1")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthMessage.InvalidCredentials, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun submit_withValidCredentials_authenticates() = runTest(mainDispatcher) {
        api.onLogin = {
            ApiFixtures.success(AuthPayloadDto(user = ApiFixtures.user, tokens = ApiFixtures.tokens))
        }
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUsernameChange("  fet1sov  ")
        viewModel.onPasswordChange("secret1")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun togglePasswordVisibility_flipsFlag() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onTogglePasswordVisibility()

        assertTrue(viewModel.uiState.value.isPasswordVisible)
    }
}
