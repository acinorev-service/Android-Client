package ru.fet1sov.akinorev.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import retrofit2.Response
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.remote.dto.ApiEnvelope
import ru.fet1sov.akinorev.data.remote.dto.ApiErrorDto
import ru.fet1sov.akinorev.data.remote.dto.RefreshRequest
import java.io.IOException

class ApiClient(
    private val api: AcinorevApi,
    private val sessionManager: SessionManager,
    private val json: Json = AcinorevJson.instance,
) {
    private val refreshMutex = Mutex()

    suspend fun <T> execute(
        retryOnUnauthorized: Boolean = true,
        call: suspend () -> Response<ApiEnvelope<T>>,
    ): AuthResult<T> {
        val response = try {
            call()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: IOException) {
            return AuthResult.Failure(AuthError.Network)
        } catch (unexpected: Exception) {
            return AuthResult.Failure(AuthError.Unexpected)
        }

        if (response.isSuccessful) {
            val data = response.body()?.data
            return if (data == null) {
                AuthResult.Failure(AuthError.Unexpected)
            } else {
                AuthResult.Success(data)
            }
        }

        if (response.code() == 401 && retryOnUnauthorized && sessionManager.refreshToken() != null) {
            if (refreshTokens() is AuthResult.Success) {
                return execute(retryOnUnauthorized = false, call = call)
            }
        }

        return AuthResult.Failure(errorFrom(response))
    }

    suspend fun executeNoContent(
        retryOnUnauthorized: Boolean = true,
        call: suspend () -> Response<*>,
    ): AuthResult<Unit> {
        val response = try {
            call()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: IOException) {
            return AuthResult.Failure(AuthError.Network)
        } catch (unexpected: Exception) {
            return AuthResult.Failure(AuthError.Unexpected)
        }

        if (response.isSuccessful) return AuthResult.Success(Unit)

        if (response.code() == 401 && retryOnUnauthorized && sessionManager.refreshToken() != null) {
            if (refreshTokens() is AuthResult.Success) {
                return executeNoContent(retryOnUnauthorized = false, call = call)
            }
        }

        return AuthResult.Failure(errorFrom(response))
    }

    suspend fun refreshTokens(): AuthResult<Unit> = refreshMutex.withLock {
        val refreshToken = sessionManager.refreshToken()
            ?: return@withLock AuthResult.Failure(AuthError.Server(Unauthorized, 401))

        when (val result = execute(retryOnUnauthorized = false) {
            api.refresh(RefreshRequest(refreshToken))
        }) {
            is AuthResult.Success -> {
                val tokens = result.value
                if (tokens.accessToken.isBlank() || tokens.refreshToken.isBlank()) {
                    sessionManager.clear()
                    AuthResult.Failure(AuthError.Unexpected)
                } else {
                    sessionManager.saveTokens(tokens.asDomain())
                    AuthResult.Success(Unit)
                }
            }

            is AuthResult.Failure -> {
                if (result.error.isUnauthorized()) sessionManager.clear()
                result
            }
        }
    }

    private fun errorFrom(response: Response<*>): AuthError {
        val code = response.errorBody()?.string()?.let { body ->
            runCatching {
                json.decodeFromString(ApiEnvelope.serializer(ApiErrorDto.serializer()), body).data?.code
            }.getOrNull()
        }.orEmpty()

        val resolved = code.ifBlank { fallbackCode(response.code()) }
        return AuthError.Server(resolved, response.code())
    }

    private fun fallbackCode(status: Int): String = when (status) {
        400 -> "bad_request"
        401 -> Unauthorized
        403 -> "forbidden"
        404 -> "not_found"
        409 -> "user_exists"
        423 -> "user_banned"
        429 -> "rate_limited"
        in 500..599 -> "server_error"
        else -> "unknown"
    }

    private companion object {
        const val Unauthorized = "unauthorized"
    }
}
