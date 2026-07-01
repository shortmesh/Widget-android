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
    object Verify : SupportedPlatformsUiState()
    object PhoneNumberProvision : SupportedPlatformsUiState()
    data class Error(val message: String) : SupportedPlatformsUiState()
}

class AuthyViewModel : ViewModel() {
    private val _supportedPlatforms = MutableStateFlow<List<SupportedPlatforms>?>(null)
    val supportedPlatforms: StateFlow<List<SupportedPlatforms>?> =
        _supportedPlatforms.asStateFlow()

    private val _listPlatformsUiState = MutableStateFlow<SupportedPlatformsUiState?>(null)
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

    private var selectedPlatform: SupportedPlatforms? = null
    fun selectPlatform(platform: SupportedPlatforms) {
        selectedPlatform = platform
        _listPlatformsUiState.value = SupportedPlatformsUiState.PhoneNumberProvision
    }
//
//    fun confirmPlatform() {
//        val current = listPlatformsUiState.value as? ListPlatformsUiState.SelectPlatform ?: return
//        val selectedId = current.selected ?: return
//        onPlatformSelected(selectedId)
//    }
}

