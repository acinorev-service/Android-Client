package ru.fet1sov.akinorev.data.library

import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.LibraryPage

interface LibraryRepository {
    suspend fun fetchLibrary(): AuthResult<LibraryPage>
}
