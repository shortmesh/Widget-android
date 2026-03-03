package io.shortmesh.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shortmesh.sdk.ShortMeshEndpoints
import io.shortmesh.sdk.network.ApiException
import io.shortmesh.sdk.network.PlatformDto
import io.shortmesh.sdk.network.ShortMeshApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    /** OTP entry screen. */
    data class EnterOtp(
        val platform: PlatformDto,
        val resendCountdown: Int = 30,
        val error: String? = null
    ) : ShortMeshStep

    /** Verifying OTP against backend. */
    object Verifying : ShortMeshStep

    /** Verification complete. */
    object Success : ShortMeshStep

    /** Unrecoverable error (e.g. platforms failed to load). */
    data class FatalError(val message: String) : ShortMeshStep
}

// ── ViewModel ────────────────────────────────────────────────────────────────

internal class ShortMeshViewModel(
    private val identifier: String,
    endpoints: ShortMeshEndpoints,
    private val onSuccess: () -> Unit,
    private val onError: (String) -> Unit
) : ViewModel() {

    private val api = ShortMeshApiClient(
        platformsUrl = endpoints.platforms,
        sendOtpUrl   = endpoints.sendOtp,
        verifyOtpUrl = endpoints.verifyOtp,
        resendOtpUrl = endpoints.resendOtp
    )

    private val _step = MutableStateFlow<ShortMeshStep>(ShortMeshStep.LoadingPlatforms)
    val step: StateFlow<ShortMeshStep> = _step.asStateFlow()

    private var countdownJob: Job? = null

    init {
        loadPlatforms()
    }

    // ── platform loading ─────────────────────────────────────────────────────

    fun loadPlatforms() {
        _step.value = ShortMeshStep.LoadingPlatforms
        viewModelScope.launch {
            runCatching { api.getPlatforms() }
                .onSuccess { platforms ->
                    if (platforms.isNullOrEmpty()) {
                        val msg = "No verification methods available."
                        _step.value = ShortMeshStep.FatalError(msg)
                        onError(msg)
                    } else {
                        _step.value = ShortMeshStep.SelectPlatform(platforms)
                    }
                }
                .onFailure { e ->
                    val msg = when (e) {
                        is ApiException -> e.message
                        is java.net.UnknownHostException -> "No internet connection. Check your network and try again."
                        is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                        is com.google.gson.JsonSyntaxException -> "Unexpected response from server. Check your endpoint URL."
                        else -> "Could not load verification methods. Please try again."
                    } ?: "Could not load verification methods. Please try again."
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
        val platform = current.platforms.find { it.id == selectedId } ?: return

        viewModelScope.launch {
            _step.value = ShortMeshStep.Verifying // show spinner while sending OTP
            runCatching { api.generateOtp(identifier, selectedId) }
                .onSuccess { response ->
                    _step.value = ShortMeshStep.EnterOtp(
                        platform = platform,
                        resendCountdown = response.expirySeconds
                    )
                    startCountdown(response.expirySeconds)
                }
                .onFailure { e ->
                    val msg = friendlyNetworkError(e, "Failed to send OTP")
                    _step.value = current.copy(error = msg)
                    onError(msg)
                }
        }
    }


    fun submitOtp(code: String) {
        val current = _step.value as? ShortMeshStep.EnterOtp ?: return
        viewModelScope.launch {
            _step.value = ShortMeshStep.Verifying
            runCatching { api.verifyOtp(identifier, current.platform.id, code) }
                .onSuccess { response ->
                    if (response.isVerified) {
                        _step.value = ShortMeshStep.Success
                    } else {
                        _step.value = current.copy(error = "Invalid code. Please try again.")
                    }
                }
                .onFailure { e ->
                    _step.value = current.copy(error = friendlyNetworkError(e, "Verification failed"))
                }
        }
    }

    fun resendOtp() {
        val current = _step.value as? ShortMeshStep.EnterOtp ?: return
        viewModelScope.launch {
            runCatching { api.generateOtp(identifier, current.platform.id) }
                .onSuccess { response ->
                    _step.update {
                        if (it is ShortMeshStep.EnterOtp)
                            it.copy(resendCountdown = response.expirySeconds, error = null)
                        else it
                    }
                    startCountdown(response.expirySeconds)
                }
                .onFailure { e ->
                    _step.update {
                        if (it is ShortMeshStep.EnterOtp)
                            it.copy(error = friendlyNetworkError(e, "Resend failed"))
                        else it
                    }
                }
        }
    }

    fun notifySuccess() {
        onSuccess()
    }

    fun goBackToSelection() {
        countdownJob?.cancel()
        loadPlatforms()
    }

    // ── countdown ────────────────────────────────────────────────────────────

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            repeat(seconds) {
                delay(1_000L)
                _step.update { state ->
                    if (state is ShortMeshStep.EnterOtp && state.resendCountdown > 0)
                        state.copy(resendCountdown = state.resendCountdown - 1)
                    else state
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

private fun friendlyNetworkError(e: Throwable, fallback: String): String = when (e) {
    is ApiException                      -> e.message ?: fallback
    is java.net.UnknownHostException     -> "No internet connection. Check your network and try again."
    is java.net.SocketTimeoutException   -> "Connection timed out. Please try again."
    else                                 -> fallback
}

