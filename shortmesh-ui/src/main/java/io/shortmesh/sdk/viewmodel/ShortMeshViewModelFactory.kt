package io.shortmesh.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class ShortMeshViewModelFactory(
    private val platformsUrl: String,
    private val onPlatformSelected: (String) -> Unit,
    private val onError: (String) -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShortMeshViewModel::class.java)) {
            return ShortMeshViewModel(platformsUrl, onPlatformSelected, onError) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
