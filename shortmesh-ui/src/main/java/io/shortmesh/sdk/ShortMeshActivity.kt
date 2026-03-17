package io.shortmesh.sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import io.shortmesh.sdk.viewmodel.ShortMeshViewModel
import io.shortmesh.sdk.viewmodel.ShortMeshViewModelFactory

class ShortMeshActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PLATFORMS_URL = "shortmesh_platforms_url"
    }

    private val viewModel: ShortMeshViewModel by viewModels {
        val platformsUrl = intent.getStringExtra(EXTRA_PLATFORMS_URL) ?: ""

        ShortMeshViewModelFactory(
            platformsUrl = platformsUrl,
            onPlatformSelected = { platformId ->
                ShortMeshCallbackRegistry.onPlatformSelected?.invoke(platformId)
                ShortMeshCallbackRegistry.clear()
                finish()
            },
            onError = { error ->
                ShortMeshCallbackRegistry.onError?.invoke(error)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ShortMeshRoot(
                    viewModel = viewModel,
                    onDismiss = { finish() }
                )
            }
        }
    }
}