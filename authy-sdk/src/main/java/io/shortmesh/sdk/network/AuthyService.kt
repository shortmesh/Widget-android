package io.shortmesh.sdk.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.jvm.java

interface AuthyService {
    @GET("/api/v1/platforms")
    suspend fun getPlatforms(): List<SupportedPlatforms>?
}

data class SupportedPlatforms(
    val display_name: String,
    val name: String,
    val icon_url: String,
) {
    companion object {

        fun getAuthyApiService(baseUrl: String): AuthyService {
            val authyRetrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
            return authyRetrofit.create(AuthyService::class.java)
        }
    }
}
