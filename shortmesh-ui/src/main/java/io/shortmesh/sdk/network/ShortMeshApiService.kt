package io.shortmesh.sdk.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "ShortMesh"
private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()
private val gson = Gson()

private val sharedHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

internal class ShortMeshApiClient(
    private val platformsUrl: String,
    private val sendOtpUrl: String,
    private val verifyOtpUrl: String,
    private val resendOtpUrl: String,
    private val http: OkHttpClient = sharedHttpClient
) {


    private suspend fun getRaw(url: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "GET $url")
        val request = Request.Builder().url(url).get().build()
        val response = http.newCall(request).execute()
        val rawBody = response.body?.string().orEmpty()
        Log.d(TAG, "GET $url → ${response.code}, body(100)=${rawBody.take(100)}")
        if (!response.isSuccessful) {
            throw ApiException(response.code, friendlyError(response.code, rawBody))
        }
        if (rawBody.isBlank()) {
            throw ApiException(response.code, "Empty response from server.")
        }
        if (rawBody.trimStart().startsWith("<")) {
            throw ApiException(response.code, "Unexpected response from server. Check your endpoint URL.")
        }
        rawBody
    }

    private suspend fun postRaw(url: String, body: Any): String = withContext(Dispatchers.IO) {
        val json = gson.toJson(body)
        Log.d(TAG, "POST $url body=$json")
        val requestBody = json.toRequestBody(JSON_TYPE)
        val request = Request.Builder().url(url).post(requestBody).build()
        val response = http.newCall(request).execute()
        val rawBody = response.body?.string().orEmpty()
        Log.d(TAG, "POST $url → ${response.code}, body(100)=${rawBody.take(100)}")
        if (!response.isSuccessful) {
            throw ApiException(response.code, friendlyError(response.code, rawBody))
        }
        if (rawBody.trimStart().startsWith("<")) {
            throw ApiException(response.code, "Unexpected response from server. Check your endpoint URL.")
        }
        rawBody
    }


    suspend fun getPlatforms(): List<PlatformDto> {
        val raw = getRaw(platformsUrl)
        val type = object : TypeToken<List<PlatformDto>>() {}.type
        return gson.fromJson(raw, type)
            ?: throw ApiException(200, "No verification methods available.")
    }

    suspend fun generateOtp(identifier: String, platform: String): GenerateOtpResponse {
        val raw = postRaw(sendOtpUrl, GenerateOtpRequest(identifier, platform))
        Log.d(TAG, "generateOtp raw response: $raw")
        return gson.fromJson(raw, GenerateOtpResponse::class.java)
            ?: throw ApiException(200, "Empty response from server.")
    }

    suspend fun verifyOtp(identifier: String, platform: String, code: String): VerifyOtpResponse {
        val raw = postRaw(verifyOtpUrl, VerifyOtpRequest(identifier, platform, code))
        Log.d(TAG, "verifyOtp raw response: $raw")
        val result = gson.fromJson(raw, VerifyOtpResponse::class.java)
            ?: throw ApiException(200, "Empty response from server.")
        Log.d(TAG, "verifyOtp isVerified=${result.isVerified} (verified=${result.verified}, success=${result.success}, status=${result.status}, message=${result.message})")
        return result
    }

    suspend fun resendOtp(identifier: String, platform: String): GenerateOtpResponse =
        generateOtp(identifier, platform)
}

class ApiException(val code: Int, override val message: String) : IOException(message)


internal fun friendlyError(httpCode: Int, rawBody: String?): String {
    val body = rawBody?.trim().orEmpty()
    val isHtml = body.startsWith("<!") || body.startsWith("<html", ignoreCase = true)
    val cleaned = if (isHtml || body.isBlank()) "" else body.take(120)
    return when {
        cleaned.isNotBlank() -> cleaned
        httpCode == 401 || httpCode == 403 -> "Unauthorized. Check your API credentials."
        httpCode == 404 -> "Endpoint not found. Check your configured URLs."
        httpCode in 500..599 -> "Server error ($httpCode). Please try again later."
        else -> "Request failed ($httpCode). Please try again."
    }
}
