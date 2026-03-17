package io.shortmesh.sdk

import android.content.Context
import android.content.Intent

/**
 * Public entry point for the ShortMesh platform-picker SDK.
 *
 * Usage:
 * ```kotlin
 * ShortMeshWidget.launch(
 *     context            = context,
 *     endpoints          = ShortMeshEndpoints(
 *         platforms = "https://example.com/api/v1/platforms"
 *     ),
 *     onPlatformSelected = { platform -> /* e.g. "wa", "tg", "signal" */ },
 *     onError            = { error -> /* handle */ }
 * )
 * ```
 */
object ShortMeshWidget {

    /**
     * Launch the platform-picker dialog.
     *
     * @param context            Any valid [Context] (Activity, Fragment, Application).
     * @param endpoints          The backend endpoint URLs (only [ShortMeshEndpoints.platforms] is used).
     * @param onPlatformSelected Called on the main thread with the platform id the user chose, e.g. "wa".
     * @param onError            Called on the main thread with the error message on failure.
     */
    fun launch(
        context: Context,
        endpoints: ShortMeshEndpoints,
        onPlatformSelected: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        ShortMeshCallbackRegistry.onPlatformSelected = onPlatformSelected
        ShortMeshCallbackRegistry.onError            = onError

        val intent = Intent(context, ShortMeshActivity::class.java).apply {
            putExtra(ShortMeshActivity.EXTRA_PLATFORMS_URL, endpoints.platforms)
        }
        context.startActivity(intent)
    }
}

internal object ShortMeshCallbackRegistry {
    var onPlatformSelected: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun clear() {
        onPlatformSelected = null
        onError            = null
    }
}
