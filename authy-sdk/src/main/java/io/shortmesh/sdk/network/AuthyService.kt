package io.shortmesh.sdk.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.jvm.java

private const val BASE_URL = "https://authy.shortmesh.com"


private val authyRetrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()


interface AuthyService {
    @GET("/api/v1/platforms")
    suspend fun getPlatforms(): List<SupportedPlatforms>?
}

data class SupportedPlatforms(
    val display_name: String,
    val name: String,
    val device_id: String
) {
    companion object {
        val authyApiService : AuthyService by lazy {
            authyRetrofit.create(AuthyService::class.java)
        }
    }
}
