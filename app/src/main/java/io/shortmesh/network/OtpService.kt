package io.shortmesh.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface OtpService {
    @POST("/api/v1/otp/generate")
    suspend fun generate(@Body request: OtpGenerateRequest): OtpGenerateResponse

    @POST("/api/v1/otp/verify")
    suspend fun verify(@Body request: OtpVerifyRequest): OtpVerifyResponse
}

data class OtpGenerateRequest(
    val phone_number: String,
    val platform: String,
)

data class OtpGenerateResponse(
    val expires_at: String?,
    val message: String?,
    val error: String?,
)

data class OtpVerifyRequest(
    val code: String,
    val phone_number: String,
    val platform: String,
)

data class OtpVerifyResponse(
    val message: String?,
    val error: String?,
)

object OtpApi {
    private val service: OtpService by lazy {
        Retrofit.Builder()
            .baseUrl("https://authy.shortmesh.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OtpService::class.java)
    }

    suspend fun generate(phoneNumber: String, platform: String): OtpGenerateResponse =
        service.generate(OtpGenerateRequest(phoneNumber, platform))

    suspend fun verify(code: String, phoneNumber: String, platform: String): OtpVerifyResponse =
        service.verify(OtpVerifyRequest(code, phoneNumber, platform))
}
