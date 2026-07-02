package io.shortmesh.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shortmesh.sdk.network.SupportedPlatforms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SupportedPlatformsUiState {
    object Loading : SupportedPlatformsUiState()
    object List : SupportedPlatformsUiState()
    object PhoneNumberProvision : SupportedPlatformsUiState()
    object Verify : SupportedPlatformsUiState()
    object Complete : SupportedPlatformsUiState()
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

    fun submitCode(code: String, callback: (String) -> Unit) {
        _listPlatformsUiState.value = SupportedPlatformsUiState.Loading
        viewModelScope.launch {
            try {
                callback(code)
                _listPlatformsUiState.value = SupportedPlatformsUiState.Complete
            } catch (e: Exception) {
                e.printStackTrace()
                _listPlatformsUiState.value = SupportedPlatformsUiState.Error(e.message ?: "")
            }
        }
    }
}

