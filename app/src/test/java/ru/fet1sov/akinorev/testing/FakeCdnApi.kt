package ru.fet1sov.akinorev.testing

import okhttp3.MultipartBody
import retrofit2.Response
import ru.fet1sov.akinorev.data.remote.CdnApi
import ru.fet1sov.akinorev.data.remote.dto.ApiEnvelope
import ru.fet1sov.akinorev.data.remote.dto.CdnUploadDto
import ru.fet1sov.akinorev.testing.ApiFixtures.error

class FakeCdnApi : CdnApi {
    var onUploadFile: suspend (MultipartBody.Part) -> Response<ApiEnvelope<CdnUploadDto>> = { error(404) }

    var uploads: Int = 0
        private set

    override suspend fun uploadFile(file: MultipartBody.Part): Response<ApiEnvelope<CdnUploadDto>> {
        uploads += 1
        return onUploadFile(file)
    }
}
