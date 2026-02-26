package io.shortmesh.sdk

import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShortMeshRoot(
    phoneNumber: String,
    apiEndpoint: String,
    onDismiss: () -> Unit = {}
) {
    var step by remember { mutableStateOf("select") }
    var selectedPlatform by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        when (step) {
            "select" -> VerifyScreen(
                onPlatformSelected = {
                    selectedPlatform = it
                    step = "otp"
                },
                onClose = onDismiss
            )

            "otp" -> OtpScreen(
                platform = selectedPlatform ?: "",
                onSubmit = { code ->
                    scope.launch {
                        step = "loading"
                        delay(2000) // replace with API call
                        step = "success"
                    }
                },
                onClose = onDismiss,
                onBack = { step = "select" }
            )

            "loading" -> LoadingScreen()

            "success" -> SuccessScreen(onClose = onDismiss)
        }
    }
}
