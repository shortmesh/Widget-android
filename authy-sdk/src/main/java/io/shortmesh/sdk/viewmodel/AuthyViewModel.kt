package io.shortmesh.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shortmesh.sdk.network.SupportedPlatforms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

sealed class SupportedPlatformsUiState {
    object Loading : SupportedPlatformsUiState()
    object List : SupportedPlatformsUiState()
    object PhoneNumberProvision : SupportedPlatformsUiState()
    object Verify : SupportedPlatformsUiState()
    data class Complete(val message: String = "") : SupportedPlatformsUiState()
    data class Failed(val message: String) : SupportedPlatformsUiState()
    data class Error(val message: String) : SupportedPlatformsUiState()
}

class AuthyViewModel : ViewModel() {
    private val _supportedPlatforms = MutableStateFlow<List<SupportedPlatforms>?>(null)
    val supportedPlatforms: StateFlow<List<SupportedPlatforms>?> =
        _supportedPlatforms.asStateFlow()

    private val _listPlatformsUiState = MutableStateFlow<SupportedPlatformsUiState?>(
        SupportedPlatformsUiState.Loading)
    val listPlatformsUiState: StateFlow<SupportedPlatformsUiState?> =
        _listPlatformsUiState.asStateFlow()

    private val _otpExpiresAt = MutableStateFlow<Long?>(null)
    val otpExpiresAt: StateFlow<Long?> = _otpExpiresAt.asStateFlow()

    private var baseUrl: String? = null

    fun getPlatforms(url: String) {
        baseUrl = url
        loadPlatforms()
    }

    fun loadPlatforms() {
        baseUrl?.let {
            _listPlatformsUiState.value = SupportedPlatformsUiState.Loading
            viewModelScope.launch {
                try {
                    val platforms = SupportedPlatforms.getAuthyApiService(baseUrl!!).getPlatforms()
                    _supportedPlatforms.value = platforms
                    _listPlatformsUiState.value = SupportedPlatformsUiState.List
                } catch(e: Exception) {
                    e.printStackTrace()
                    _listPlatformsUiState.value = SupportedPlatformsUiState.Error(e.message ?: "")
                }
            }
        }
    }

    var selectedPlatform: SupportedPlatforms? = null
    var phoneNumber: String? = null

    fun selectPlatform(platform: SupportedPlatforms) {
        selectedPlatform = platform
        _listPlatformsUiState.value = SupportedPlatformsUiState.PhoneNumberProvision
    }

    fun submitPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        _listPlatformsUiState.value = SupportedPlatformsUiState.Verify
    }

    fun setOtpExpiresAt(expiresAt: String?) {
        _otpExpiresAt.value = parseExpiresAt(expiresAt)
    }

    fun submitCode(code: String, callback: suspend (String) -> String) {
        _listPlatformsUiState.value = SupportedPlatformsUiState.Loading
        viewModelScope.launch {
            try {
                val message = callback(code)
                if (message.contains("OTP verified successfully", ignoreCase = true)) {
                    _listPlatformsUiState.value = SupportedPlatformsUiState.Complete(message)
                } else {
                    _listPlatformsUiState.value = SupportedPlatformsUiState.Failed(message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _listPlatformsUiState.value = SupportedPlatformsUiState.Failed(e.message ?: "")
            }
        }
    }

    fun retryVerification() {
        _listPlatformsUiState.value = SupportedPlatformsUiState.Verify
    }

    private fun parseExpiresAt(expiresAt: String?): Long? {
        if (expiresAt == null) return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(expiresAt)
                if (date != null) return date.time
            } catch (e: Exception) { /* try next format */ }
        }
        // Fallback: try as Unix timestamp in seconds
        return expiresAt.toLongOrNull()?.times(1000)
    }
}

