package ru.fet1sov.akinorev.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import ru.fet1sov.akinorev.data.remote.AcinorevJson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.sessionDataStore by preferencesDataStore(name = "auth_session")

class EncryptedSessionStorage(
    context: Context,
    private val json: Json = AcinorevJson.instance,
    private val cipher: SessionCipher = SessionCipher(),
) : SessionStorage {
    private val dataStore = context.applicationContext.sessionDataStore

    override suspend fun read(): StoredSession? {
        val encoded = dataStore.data.first()[SessionKey] ?: return null
        val decoded = cipher.decrypt(encoded) ?: return null
        return runCatching { json.decodeFromString<StoredSession>(decoded) }.getOrNull()
    }

    override suspend fun write(session: StoredSession) {
        val encoded = cipher.encrypt(json.encodeToString(StoredSession.serializer(), session))
        dataStore.edit { preferences -> preferences[SessionKey] = encoded }
    }

    override suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(SessionKey) }
    }

    private companion object {
        val SessionKey = stringPreferencesKey("encrypted_session")
    }
}

class SessionCipher(
    private val keyAlias: String = "akinorev.session.v1",
) {
    fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val payload = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String? = runCatching {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        if (payload.size <= IvLength) return null
        val iv = payload.copyOfRange(0, IvLength)
        val body = payload.copyOfRange(IvLength, payload.size)
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TagBits, iv))
        String(cipher.doFinal(body), Charsets.UTF_8)
    }.getOrNull()

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(Provider).apply { load(null) }
        val existing = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Provider)
        generator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val Provider = "AndroidKeyStore"
        const val Transformation = "AES/GCM/NoPadding"
        const val IvLength = 12
        const val TagBits = 128
    }
}
