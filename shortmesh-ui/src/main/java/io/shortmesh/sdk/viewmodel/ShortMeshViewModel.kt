package io.shortmesh.sdk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shortmesh.sdk.network.ApiException
import io.shortmesh.sdk.network.PlatformDto
import io.shortmesh.sdk.network.ShortMeshApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI State ─────────────────────────────────────────────────────────────────

sealed interface ShortMeshStep {
    /** Fetching available platforms from backend. */
    object LoadingPlatforms : ShortMeshStep

    /** Platform selection screen. */
    data class SelectPlatform(
        val platforms: List<PlatformDto>,
        val selected: String? = null,
        val error: String? = null
    ) : ShortMeshStep

    /** Unrecoverable error (e.g. platforms failed to load). */
    data class FatalError(val message: String) : ShortMeshStep
}

// ── ViewModel ────────────────────────────────────────────────────────────────

internal class ShortMeshViewModel(
    platformsUrl: String,
    private val onPlatformSelected: (String) -> Unit,
    private val onError: (String) -> Unit
) : ViewModel() {

    private val api = ShortMeshApiClient(platformsUrl = platformsUrl)

    private val _step = MutableStateFlow<ShortMeshStep>(ShortMeshStep.LoadingPlatforms)
    val step: StateFlow<ShortMeshStep> = _step.asStateFlow()

    init {
        loadPlatforms()
    }

    // ── platform loading ─────────────────────────────────────────────────────

    fun loadPlatforms() {
        _step.value = ShortMeshStep.LoadingPlatforms
        viewModelScope.launch {
            runCatching { api.getPlatforms() }
                .onSuccess { platforms ->
                    _step.value = ShortMeshStep.SelectPlatform(platforms)
                    if (platforms.isEmpty()) {
                        onError("No verification methods available.")
                    }
                }
                .onFailure { e ->
                    Log.e("ShortMesh", "loadPlatforms failed: ${e.javaClass.name}: ${e.message}", e)
                    val msg = when (e) {
                        is ApiException -> e.message
                        is java.net.UnknownHostException -> "Cannot reach the server. Check your network connection and endpoint URL."
                        is java.net.SocketTimeoutException -> "The server took too long to respond. Check your endpoint URL and try again."
                        is java.net.ConnectException -> "Could not connect to the server. Make sure your endpoint URL is correct."
                        is com.google.gson.JsonSyntaxException -> "Unexpected response format. Check your endpoint URL."
                        else -> "Could not load platforms (${e.javaClass.simpleName}). Check your endpoint URL."
                    }
                    _step.value = ShortMeshStep.FatalError(msg)
                    onError(msg)
                }
        }
    }

    // ── selection ────────────────────────────────────────────────────────────

    fun selectPlatform(id: String) {
        val current = _step.value as? ShortMeshStep.SelectPlatform ?: return
        _step.update { current.copy(selected = id) }
    }

    fun confirmPlatform() {
        val current = _step.value as? ShortMeshStep.SelectPlatform ?: return
        val selectedId = current.selected ?: return
        onPlatformSelected(selectedId)
    }
}

