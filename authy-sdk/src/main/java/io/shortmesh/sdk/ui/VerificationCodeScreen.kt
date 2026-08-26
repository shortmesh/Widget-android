package io.shortmesh.sdk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.shortmesh.sdk.R
import io.shortmesh.sdk.viewmodel.AuthyViewModel
import io.shortmesh.sdk.viewmodel.SupportedPlatformsUiState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VerificationCodeScreen(
    viewModel: AuthyViewModel,
    submitCallback: (code: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onVerificationSuccess: () -> Unit,
    onResendCallback: () -> Unit,
    onCancelCallback: () -> Unit,
) {
    val otpExpiresInSeconds by viewModel.otpExpiresInSeconds.collectAsState()
    var showVerifying by remember{ mutableStateOf(false)}
    var error: String? by remember{ mutableStateOf(null) }

    VerificationCodeScreenComponent(
        platformName = viewModel.selectedPlatform?.display_name ?: "",
        phoneNumber = viewModel.phoneNumber ?: "",
        expiresInSeconds = otpExpiresInSeconds,
        submitCallback = { code ->
            showVerifying = true
            submitCallback(code) { status, message ->
                if(status) {
                    onVerificationSuccess()
                } else {
                    error = message
                }
                showVerifying = false
            }
        },
        onCancelCallback = onCancelCallback,
        onResendCallback = {
            error = null
            onResendCallback()
        },
        error = error,
        verifying = showVerifying
    )
}

@Preview(showBackground = true)
@Composable
private fun VerificationCodeScreenComponent(
    platformName: String = "",
    phoneNumber: String = "",
    error: String? = null,
    expiresInSeconds: Long? = null,
    verifying: Boolean = false,
    submitCallback: (code: String) -> Unit = {},
    onCancelCallback: () -> Unit = {},
    onResendCallback: () -> Unit = {},
) {
    var code by remember { mutableStateOf("") }
    var remainingSeconds by remember { mutableLongStateOf(0L) }
    val isExpired = expiresInSeconds != null && remainingSeconds <= 0

    LaunchedEffect(expiresInSeconds) {
        if (expiresInSeconds == null) return@LaunchedEffect
        remainingSeconds = expiresInSeconds.coerceAtLeast(0L)
        while (remainingSeconds > 0) {
            delay(1000L.milliseconds)
            remainingSeconds -= 1
            if (remainingSeconds <= 0) break
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(!error.isNullOrEmpty() || LocalInspectionMode.current) {
                Text(
                    error ?: "This is a sample error message.",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.size(16.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                enabled = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.enter_code)) },
                placeholder = { Text(stringResource(R.string.enter_code)) },
                supportingText = {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.your_code_has_been_sent))
                            append(" ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(platformName.ifBlank { stringResource(R.string.your_selected_platform) })
                            }
                            append(" (")
                            append(phoneNumber.ifBlank { stringResource(R.string.your_number) })
                            append(")")
                        }
                    )
                },
                isError = !error.isNullOrEmpty(),
            )

            if (expiresInSeconds != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpired) {
                            stringResource(R.string.otp_expired)
                        } else {
                            stringResource(R.string.otp_expires_in, formatCountdown(remainingSeconds))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpired || remainingSeconds < 60) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    TextButton(
                        onClick = onResendCallback,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(stringResource(R.string.resend_code))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onCancelCallback,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        submitCallback(code)
                        code = ""
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    enabled = code.isNotEmpty() && code.length > 3 && !verifying
                ) {
                    if(verifying) {
                        CircularProgressIndicator()
                    }
                    else {
                        Text(stringResource(R.string.submit))
                    }
                }
            }
        }
    }
}

private fun formatCountdown(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}