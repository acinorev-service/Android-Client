package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterPushDeviceRequest(
    val token: String,
    val platform: String = "android",
    @SerialName("app_version") val appVersion: String? = null,
    val locale: String? = null,
)

@Serializable
data class UnregisterPushDeviceRequest(
    val token: String,
)

@Serializable
data class PushDeviceResponse(
    val ok: Boolean = true,
)
