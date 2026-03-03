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
        const val EXTRA_IDENTIFIER    = "shortmesh_identifier"
        const val EXTRA_PLATFORMS_URL = "shortmesh_platforms_url"
        const val EXTRA_SEND_OTP_URL  = "shortmesh_send_otp_url"
        const val EXTRA_VERIFY_URL    = "shortmesh_verify_url"
        const val EXTRA_RESEND_URL    = "shortmesh_resend_url"
    }

    private val viewModel: ShortMeshViewModel by viewModels {
        val identifier    = intent.getStringExtra(EXTRA_IDENTIFIER)    ?: ""
        val platformsUrl  = intent.getStringExtra(EXTRA_PLATFORMS_URL) ?: ""
        val sendOtpUrl    = intent.getStringExtra(EXTRA_SEND_OTP_URL)  ?: ""
        val verifyUrl     = intent.getStringExtra(EXTRA_VERIFY_URL)    ?: ""
        val resendUrl     = intent.getStringExtra(EXTRA_RESEND_URL)    ?: ""

        ShortMeshViewModelFactory(
            identifier = identifier,
            endpoints  = ShortMeshEndpoints(
                platforms = platformsUrl,
                sendOtp   = sendOtpUrl,
                verifyOtp = verifyUrl,
                resendOtp = resendUrl
            ),
            onSuccess = {
                ShortMeshCallbackRegistry.onSuccess?.invoke(Unit)
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