package ru.fet1sov.akinorev.data.auth

interface SessionStorage {
    suspend fun read(): StoredSession?
    suspend fun write(session: StoredSession)
    suspend fun clear()
}
