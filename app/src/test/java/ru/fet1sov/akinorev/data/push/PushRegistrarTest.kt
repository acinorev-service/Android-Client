package ru.fet1sov.akinorev.data.push

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.fet1sov.akinorev.data.remote.AuthResult

class PushRegistrarTest {
    private val store = FakePushTokenStore()
    private val source = FakePushTokenSource()
    private val repository = FakePushDeviceRepository()
    private val registrar = PushRegistrar(store, source, repository)

    @Test
    fun sync_registersTokenFromFirebase() = runTest {
        source.token = "fcm-1"

        val result = registrar.sync()

        assertTrue(result)
        assertEquals("fcm-1", repository.registeredToken)
        assertEquals("fcm-1", store.token())
    }

    @Test
    fun sync_prefersExplicitToken() = runTest {
        source.token = "fcm-old"

        registrar.sync(token = "fcm-new")

        assertEquals("fcm-new", repository.registeredToken)
        assertEquals("fcm-new", store.token())
    }

    @Test
    fun sync_withoutAnyToken_doesNothing() = runTest {
        val result = registrar.sync()

        assertFalse(result)
        assertNull(repository.registeredToken)
    }

    @Test
    fun sync_reportsFailureWhenServerRejects() = runTest {
        source.token = "fcm-1"
        repository.registerResult = AuthResult.Failure(ru.fet1sov.akinorev.data.remote.AuthError.Network)

        assertFalse(registrar.sync())
        assertEquals("fcm-1", store.token())
    }

    @Test
    fun unregister_removesStoredToken() = runTest {
        store.save("fcm-1")

        registrar.unregister()

        assertEquals("fcm-1", repository.unregisteredToken)
        assertNull(store.token())
    }

    private class FakePushTokenStore : PushTokenStore {
        private var value: String? = null

        override fun token(): String? = value

        override fun save(token: String) {
            value = token
        }

        override fun clear() {
            value = null
        }
    }

    private class FakePushTokenSource : PushTokenSource {
        var token: String? = null

        override suspend fun currentToken(): String? = token
    }

    private class FakePushDeviceRepository : PushDeviceRepository {
        var registeredToken: String? = null
        var unregisteredToken: String? = null
        var registerResult: AuthResult<Unit> = AuthResult.Success(Unit)

        override suspend fun register(
            token: String,
            appVersion: String,
            locale: String,
        ): AuthResult<Unit> {
            registeredToken = token
            return registerResult
        }

        override suspend fun unregister(token: String): AuthResult<Unit> {
            unregisteredToken = token
            return AuthResult.Success(Unit)
        }
    }
}
