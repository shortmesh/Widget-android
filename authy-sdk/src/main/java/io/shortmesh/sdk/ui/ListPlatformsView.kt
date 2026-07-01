package io.shortmesh.sdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.shortmesh.sdk.viewmodel.AuthyViewModel
import io.shortmesh.sdk.viewmodel.SupportedPlatformsUiState

@Composable
fun AuthyWidgetLauncherView(
    authyUrl: String,
    viewModel: AuthyViewModel,
    onDismiss: () -> Unit = {}
) {
    val listPlatformsUiState by viewModel.listPlatformsUiState.collectAsState()

    Column {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth  = false,
                dismissOnBackPress       = true,
                dismissOnClickOutside    = false
            )
        ) {
            when (val s = listPlatformsUiState) {
                is SupportedPlatformsUiState.Loading -> LoadingScreen(
                    title = "Loading platforms…",
                    message = "Please wait"
                )

//                is SupportedPlatformsUiState.Verify -> VerifyScreen(
//                    platforms = s.supportedPlatforms,
//                    onContinue = { TODO() },
//                    onClose = onDismiss
//                )

                is SupportedPlatformsUiState.Error -> ErrorScreen(
                    message = s.message,
                    onRetry = viewModel::loadPlatforms,
                    onClose = onDismiss
                )
                else -> VerifyScreen( url = authyUrl, viewModel = viewModel, )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthyWidgetLauncherView_Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        AuthyWidgetLauncherView(
            authyUrl = "",
            viewModel = remember{ AuthyViewModel() },
        ) {}
    }
}