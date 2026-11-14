package ru.fet1sov.akinorev.testing

import ru.fet1sov.akinorev.data.auth.SessionStorage
import ru.fet1sov.akinorev.data.auth.StoredSession

class InMemorySessionStorage(
    initial: StoredSession? = null,
) : SessionStorage {
    var session: StoredSession? = initial
        private set

    override suspend fun read(): StoredSession? = session

    override suspend fun write(session: StoredSession) {
        this.session = session
    }

    override suspend fun clear() {
        session = null
    }
}
