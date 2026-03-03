package io.shortmesh.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.shortmesh.sdk.ShortMeshEndpoints

internal class ShortMeshViewModelFactory(
    private val identifier: String,
    private val endpoints: ShortMeshEndpoints,
    private val onSuccess: () -> Unit,
    private val onError: (String) -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShortMeshViewModel::class.java)) {
            return ShortMeshViewModel(identifier, endpoints, onSuccess, onError) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
