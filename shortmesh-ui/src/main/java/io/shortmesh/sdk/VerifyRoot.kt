package io.shortmesh.sdk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth  = false,
            dismissOnBackPress       = true,
            dismissOnClickOutside    = false
        )
    ) {
        when (val s = step) {
            is ShortMeshStep.LoadingPlatforms -> LoadingScreen(
                title   = "Loading platforms…",
                message = "Please wait"
            )

            is ShortMeshStep.SelectPlatform -> VerifyScreen(
                platforms        = s.platforms,
                selected         = s.selected,
                error            = s.error,
                onPlatformClick  = viewModel::selectPlatform,
                onContinue       = viewModel::confirmPlatform,
                onClose          = onDismiss
            )

            is ShortMeshStep.FatalError -> ErrorScreen(
                message = s.message,
                onRetry = viewModel::loadPlatforms,
                onClose = onDismiss
            )
        }
    }
}
