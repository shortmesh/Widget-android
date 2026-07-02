package io.shortmesh.sdk.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.shortmesh.sdk.R
import io.shortmesh.sdk.viewmodel.AuthyViewModel
import io.shortmesh.sdk.viewmodel.SupportedPlatformsUiState

@Composable
fun AuthyWidgetLauncherView(
    showDialog: Boolean,
    authyUrl: String,
    viewModel: AuthyViewModel,
    requestCodeCallback: (phoneNumber: String) -> Unit = {},
    sendCodeCallback: (code: String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val listPlatformsUiState by viewModel.listPlatformsUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getPlatforms(authyUrl)
    }

    if(showDialog) {
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
                        title = stringResource(R.string.loading_platforms),
                        message = stringResource(R.string.please_wait)
                    )

                    is SupportedPlatformsUiState.Error -> ErrorScreen(
                        message = s.message,
                        onRetry = viewModel::loadPlatforms,
                        onClose = onDismiss
                    )

                    is SupportedPlatformsUiState.List -> ListPlatformsScreen(
                        viewModel = viewModel,
                        onClose = onDismiss
                    )

                    is SupportedPlatformsUiState.PhoneNumberProvision -> PhoneNumberScreen(
                        viewModel = viewModel,
                        requestCodeCallback = requestCodeCallback,
                        onCancelCallback = onDismiss
                    )
                    is SupportedPlatformsUiState.Verify -> VerificationCodeScreen(
                        viewModel = viewModel,
                        submitCallback = sendCodeCallback,
                        onCancelCallback = onDismiss
                    )
                    else -> {
                        onDismiss()
                    }
                }
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
            true,
            authyUrl = "",
            viewModel = remember{ AuthyViewModel() },
        ) {}
    }
}