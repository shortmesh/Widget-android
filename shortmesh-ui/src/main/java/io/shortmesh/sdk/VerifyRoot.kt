package io.shortmesh.sdk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.shortmesh.sdk.viewmodel.ShortMeshStep
import io.shortmesh.sdk.viewmodel.ShortMeshViewModel

@Composable
internal fun ShortMeshRoot(
    viewModel: ShortMeshViewModel,
    onDismiss: () -> Unit = {}
) {
    val step by viewModel.step.collectAsState()

    var lastOtpStep by remember { mutableStateOf<ShortMeshStep.EnterOtp?>(null) }
    if (step is ShortMeshStep.EnterOtp) lastOtpStep = step as ShortMeshStep.EnterOtp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth  = false,
            dismissOnBackPress       = false,
            dismissOnClickOutside    = false
        )
    ) {
        when (val s = step) {
            is ShortMeshStep.LoadingPlatforms -> LoadingScreen(message = "Loading platforms…")

            is ShortMeshStep.SelectPlatform -> VerifyScreen(
                platforms        = s.platforms,
                selected         = s.selected,
                error            = s.error,
                onPlatformClick  = viewModel::selectPlatform,
                onContinue       = viewModel::confirmPlatform,
                onClose          = onDismiss
            )

            is ShortMeshStep.Verifying -> {
                val otp = lastOtpStep
                if (otp != null) {
                    OtpScreen(
                        platform        = otp.platform.label,
                        resendCountdown = otp.resendCountdown,
                        error           = otp.error,
                        isLoading       = true,
                        onSubmit        = viewModel::submitOtp,
                        onResend        = viewModel::resendOtp,
                        onBack          = viewModel::goBackToSelection,
                        onClose         = onDismiss
                    )
                } else {
                    LoadingScreen()
                }
            }

            is ShortMeshStep.EnterOtp -> OtpScreen(
                platform         = s.platform.label,
                resendCountdown  = s.resendCountdown,
                error            = s.error,
                isLoading        = false,
                onSubmit         = viewModel::submitOtp,
                onResend         = viewModel::resendOtp,
                onBack           = viewModel::goBackToSelection,
                onClose          = onDismiss
            )

            is ShortMeshStep.Success -> SuccessScreen(onClose = {
                viewModel.notifySuccess()
                onDismiss()
            })

            is ShortMeshStep.FatalError -> ErrorScreen(
                message = s.message,
                onRetry = viewModel::loadPlatforms,
                onClose = onDismiss
            )
        }
    }
}
