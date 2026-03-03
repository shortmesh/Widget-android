package io.shortmesh.sdk

import android.content.Context
import android.content.Intent

/**
 * Public entry point for the ShortMesh verification SDK.
 *
 * Usage:
 * ```kotlin
 * ShortMeshWidget.launch(
 *     context    = context,
 *     identifier = "+237650393369",
 *     endpoints  = ShortMeshEndpoints(
 *         platforms = "https://example.com/api/v1/platforms",
 *         sendOtp   = "https://example.com/api/v1/otp/generate",
 *         verifyOtp = "https://example.com/api/v1/otp/verify",
 *         resendOtp = "https://example.com/api/v1/otp/resend"
 *     ),
 *     onSuccess  = { /* proceed */ },
 *     onError    = { error -> /* handle */ }
 * )
 * ```
 */
object ShortMeshWidget {

    /**
     * Launch the verification flow in a new Activity.
     *
     * @param context    Any valid [Context] (Activity, Fragment, Application).
     * @param identifier The phone number (or other identifier) to verify, e.g. "+237650393369".
     * @param endpoints  The four backend endpoint URLs.
     * @param onSuccess  Called on the main thread when verification succeeds.
     * @param onError    Called on the main thread with the error message on failure.
     */
    fun launch(
        context: Context,
        identifier: String,
        endpoints: ShortMeshEndpoints,
        onSuccess: (Unit) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        ShortMeshCallbackRegistry.onSuccess = onSuccess
        ShortMeshCallbackRegistry.onError   = onError

        val intent = Intent(context, ShortMeshActivity::class.java).apply {
            putExtra(ShortMeshActivity.EXTRA_IDENTIFIER,    identifier)
            putExtra(ShortMeshActivity.EXTRA_PLATFORMS_URL, endpoints.platforms)
            putExtra(ShortMeshActivity.EXTRA_SEND_OTP_URL,  endpoints.sendOtp)
            putExtra(ShortMeshActivity.EXTRA_VERIFY_URL,    endpoints.verifyOtp)
            putExtra(ShortMeshActivity.EXTRA_RESEND_URL,    endpoints.resendOtp)
        }
        context.startActivity(intent)
    }
}

internal object ShortMeshCallbackRegistry {
    var onSuccess: ((Unit) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun clear() {
        onSuccess = null
        onError   = null
    }
}
