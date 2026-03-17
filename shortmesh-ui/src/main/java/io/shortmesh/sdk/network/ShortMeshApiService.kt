package io.shortmesh.sdk.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit

private const val TAG = "ShortMesh"
private val gson = Gson()

private fun freshClient(): OkHttpClient = OkHttpClient.Builder()
    .protocols(listOf(Protocol.HTTP_1_1))
    .connectionPool(ConnectionPool(0, 1, TimeUnit.MILLISECONDS))
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .retryOnConnectionFailure(false)
    .build()

internal class ShortMeshApiClient(
    private val platformsUrl: String,
) {

    private fun validateUrl(url: String) {
        try {
            val parsed = URL(url)
            if (parsed.protocol !in listOf("http", "https")) {
                throw ApiException(0, "Invalid URL scheme. URL must start with http:// or https://")
            }
        } catch (_: MalformedURLException) {
            throw ApiException(0, "Invalid URL: \"$url\". Check your configured endpoint.")
        }
    }

    private suspend fun getRaw(url: String): String = withContext(Dispatchers.IO) {
        validateUrl(url)
        Log.d(TAG, "GET $url")
        try {
            val builder = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "ShortMesh-Android-SDK/1.0")
                .header("Connection", "close")
                .get()
            val response = freshClient().newCall(builder.build()).execute()
            val rawBody = response.body?.string().orEmpty()
            Log.d(TAG, "GET $url → ${response.code}, body(100)=${rawBody.take(100)}")
            if (!response.isSuccessful) {
                throw ApiException(response.code, friendlyError(response.code, rawBody))
            }
            if (rawBody.isBlank()) {
                throw ApiException(response.code, "Empty response from server. Check your endpoint URL.")
            }
            if (rawBody.trimStart().startsWith("<")) {
                throw ApiException(response.code, "Unexpected HTML response. Check your endpoint URL.")
            }
            rawBody
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            val msg = when {
                e.message?.contains("end of stream") == true ->
                    "Server closed the connection without responding. Check your endpoint URL and API key."
                else -> e.message ?: "Network error. Check your connection."
            }
            Log.e(TAG, "GET $url failed: ${e.javaClass.name}: ${e.message}")
            throw ApiException(0, msg)
        } catch (e: Exception) {
            Log.e(TAG, "GET $url failed: ${e.javaClass.name}: ${e.message}")
            throw ApiException(0, "Unexpected error: ${e.message}")
        }
    }

    suspend fun getPlatforms(): List<PlatformDto> {
        val raw = getRaw(platformsUrl)
        val type = object : TypeToken<List<PlatformDto>>() {}.type
        return gson.fromJson(raw, type) ?: emptyList()
    }
}

class ApiException(@Suppress("unused") val code: Int, override val message: String) : IOException(message)

internal fun friendlyError(httpCode: Int, rawBody: String?): String {
    val body = rawBody?.trim().orEmpty()
    val isHtml = body.startsWith("<!") || body.startsWith("<html", ignoreCase = true)
    val cleaned = if (isHtml || body.isBlank()) "" else body.take(120)
    return when {
        cleaned.isNotBlank() -> cleaned
        httpCode == 401 || httpCode == 403 -> "Unauthorized. Check your API credentials."
        httpCode == 404 -> "Endpoint not found. Check your configured URL."
        httpCode in 500..599 -> "Server error ($httpCode). Please try again later."
        else -> "Request failed ($httpCode). Please try again."
    }
}
