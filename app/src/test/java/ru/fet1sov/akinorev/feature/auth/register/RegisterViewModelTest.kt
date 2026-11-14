package ru.fet1sov.akinorev.feature.auth.register

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
class RegisterViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultAuthRepository(api, sessionManager)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun viewModel() = RegisterViewModel(repository)

    @Test
    fun usernameChange_stripsForbiddenCharacters() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUsernameChange("fe!t1sov_Привет")

        assertEquals("fet1sov", viewModel.uiState.value.username)
    }

    @Test
    fun submit_withShortPassword_showsShortPasswordError() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUsernameChange("fet1sov")
        viewModel.onPasswordChange("123")
        viewModel.onRepeatPasswordChange("123")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthMessage.ShortPassword, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun submit_withMismatchedPasswords_showsMismatchError() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUsernameChange("fet1sov")
        viewModel.onPasswordChange("secret1")
        viewModel.onRepeatPasswordChange("secret2")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthMessage.PasswordMismatch, viewModel.uiState.value.error)
    }

    @Test
    fun submit_withInvalidEmail_showsEmailError() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUsernameChange("fet1sov")
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("secret1")
        viewModel.onRepeatPasswordChange("secret1")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(AuthMessage.InvalidEmail, viewModel.uiState.value.error)
    }

    @Test
    fun submit_withValidForm_registersAndAuthenticates() = runTest(mainDispatcher) {
        api.onRegister = {
            ApiFixtures.success(AuthPayloadDto(user = ApiFixtures.user, tokens = ApiFixtures.tokens))
        }
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onUsernameChange("fet1sov")
        viewModel.onEmailChange("fet1sov@example.com")
        viewModel.onPasswordChange("secret1")
        viewModel.onRepeatPasswordChange("secret1")
        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertNull(viewModel.uiState.value.error)
        assertEquals("access-1", sessionManager.accessToken())
    }
}
