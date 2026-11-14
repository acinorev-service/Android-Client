package ru.fet1sov.akinorev.feature.emojis

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
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.emojis.EmojiFileResult
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.FakeEmojiFileSource
import ru.fet1sov.akinorev.testing.FakeEmojisRepository
import ru.fet1sov.akinorev.testing.InMemorySessionStorage
import ru.fet1sov.akinorev.testing.fakeEmoji

@OptIn(ExperimentalCoroutinesApi::class)
class EmojiCreateViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val authRepository = DefaultAuthRepository(api, sessionManager)
    private val emojisRepository = FakeEmojisRepository()
    private val fileSource = FakeEmojiFileSource()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private suspend fun authorize(subscribed: Boolean = true) {
        val user = ApiFixtures.user.copy(isSubscribed = subscribed)
        sessionManager.save(user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = user)) }
    }

    private fun viewModel() = EmojiCreateViewModel(
        authRepository = authRepository,
        emojisRepository = emojisRepository,
        emojiFileSource = fileSource,
    )

    @Test
    fun onNameChange_normalizesInput() = runTest(mainDispatcher) {
        authorize()
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onNameChange("  PePe_01!  ")

        val state = viewModel.uiState.value
        assertEquals("pepe_01", state.name)
        assertEquals(":pepe_01:", state.nameMarker)
    }

    @Test
    fun onTitleAndDescription_areTruncatedToServerLimits() = runTest(mainDispatcher) {
        authorize()
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onTitleChange("t".repeat(100))
        viewModel.onDescriptionChange("d".repeat(400))

        val state = viewModel.uiState.value
        assertEquals(64, state.title.length)
        assertEquals(300, state.description.length)
    }

    @Test
    fun pickedFile_isStoredInState() = runTest(mainDispatcher) {
        authorize()
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("content://emoji/1", state.pickedUri)
        assertEquals("pepe.png", state.pickedFileName)
        assertEquals(listOf("content://emoji/1"), fileSource.requestedUris)
        assertNull(state.error)
    }

    @Test
    fun unsupportedFile_setsFormatError() = runTest(mainDispatcher) {
        authorize()
        fileSource.result = EmojiFileResult.Unsupported
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(EmojiCreateError.Format, state.error)
        assertEquals("", state.pickedUri)
        assertFalse(state.canSubmit)
    }

    @Test
    fun oversizedFile_setsSizeError() = runTest(mainDispatcher) {
        authorize()
        fileSource.result = EmojiFileResult.TooLarge
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()

        assertEquals(EmojiCreateError.Size, viewModel.uiState.value.error)
    }

    @Test
    fun canSubmit_requiresFileAndValidName() = runTest(mainDispatcher) {
        authorize()
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onNameChange("pepe")
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canSubmit)

        viewModel.clearFile()
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun canSubmit_isFalseWithoutSubscription() = runTest(mainDispatcher) {
        authorize(subscribed = false)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onNameChange("pepe")
        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun submit_success_marksCreatedAndSendsMetadata() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.uploadResult = AuthResult.Success(fakeEmoji(mine = true))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onNameChange("Pepe")
        viewModel.onTitleChange("  Title  ")
        viewModel.onDescriptionChange("  Description  ")
        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isCreated)
        assertFalse(state.isUploading)
        assertEquals(1, emojisRepository.uploadedMetadata.size)
        val metadata = emojisRepository.uploadedMetadata.single()
        assertEquals("pepe", metadata.name)
        assertEquals("Title", metadata.title)
        assertEquals("Description", metadata.description)
    }

    @Test
    fun submit_nameTaken_setsNameTakenError() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.uploadResult = AuthResult.Failure(AuthError.Server("emoji_name_taken", 409))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onNameChange("pepe")
        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(EmojiCreateError.NameTaken, state.error)
        assertFalse(state.isCreated)
        assertFalse(state.isUploading)
    }

    @Test
    fun submit_subscriptionRequired_setsSubscriptionError() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.uploadResult = AuthResult.Failure(AuthError.Server("subscription_required", 403))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onNameChange("pepe")
        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(EmojiCreateError.SubscriptionRequired, viewModel.uiState.value.error)
    }

    @Test
    fun hasDraft_reflectsAnyFilledField() = runTest(mainDispatcher) {
        authorize()
        val viewModel = viewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasDraft)

        viewModel.onNameChange("pepe")
        assertTrue(viewModel.uiState.value.hasDraft)

        viewModel.onNameChange("")
        assertFalse(viewModel.uiState.value.hasDraft)

        viewModel.onTitleChange("Title")
        assertTrue(viewModel.uiState.value.hasDraft)

        viewModel.onTitleChange("")
        viewModel.onDescriptionChange("Description")
        assertTrue(viewModel.uiState.value.hasDraft)

        viewModel.onDescriptionChange("")
        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasDraft)
    }

    @Test
    fun dismissError_clearsError() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.uploadResult = AuthResult.Failure(AuthError.Server("server_error", 500))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onNameChange("pepe")
        viewModel.onFilePicked("content://emoji/1")
        advanceUntilIdle()
        viewModel.submit()
        advanceUntilIdle()
        assertEquals(EmojiCreateError.Server, viewModel.uiState.value.error)

        viewModel.dismissError()

        assertNull(viewModel.uiState.value.error)
    }
}
