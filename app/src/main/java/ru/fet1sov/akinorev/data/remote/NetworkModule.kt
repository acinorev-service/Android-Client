package ru.fet1sov.akinorev.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.data.auth.SessionManager
import java.util.concurrent.TimeUnit

class NetworkModule(
    baseUrl: String,
    cdnBaseUrl: String,
    sessionManager: SessionManager,
) {
    private val contentType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthHeaderInterceptor(sessionManager))
        .addInterceptor(loggingInterceptor())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    val api: AcinorevApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(AcinorevJson.instance.asConverterFactory(contentType))
        .build()
        .create(AcinorevApi::class.java)

    val cdnApi: CdnApi = Retrofit.Builder()
        .baseUrl(cdnBaseUrl.trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(AcinorevJson.instance.asConverterFactory(contentType))
        .build()
        .create(CdnApi::class.java)

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}

internal object AcinorevJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }
}

private class AuthHeaderInterceptor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.accessToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
