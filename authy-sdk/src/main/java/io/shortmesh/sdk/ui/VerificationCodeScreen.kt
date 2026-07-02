package io.shortmesh.sdk.ui

import android.R.attr.label
import android.R.attr.textStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rejowan.ccpc.Country
import com.rejowan.ccpc.CountryCodePickerTextField
import io.shortmesh.sdk.R
import io.shortmesh.sdk.viewmodel.AuthyViewModel

@Composable
fun VerificationCodeScreen(
    viewModel: AuthyViewModel,
    submitCallback: (code: String) -> Unit = {},
    onCancelCallback: () -> Unit = {},
) {
    VerificationCodeScreenComponent(
        platformName = viewModel.selectedPlatform?.display_name ?: "",
        phoneNumber = viewModel.phoneNumber ?: "",
        submitCallback = { code ->
            viewModel.submitCode(code, submitCallback)
        },
        onCancelCallback
    )
}

@Preview(showBackground = true)
@Composable
private fun VerificationCodeScreenComponent(
    platformName: String = "",
    phoneNumber: String = "",
    submitCallback: (code: String) -> Unit = {},
    onCancelCallback: () -> Unit = {},
) {
    var code by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(16.dp)
//            .width(400.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                enabled = true,
                label = { Text(stringResource(R.string.enter_code))},
                placeholder = {Text(stringResource(R.string.enter_code))},
                supportingText = {
                    Column {
                        Text(stringResource(R.string.your_code_has_been_sent))
                        Text("$platformName ($phoneNumber)")
                    }
                },
                isError = false,
            )
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        submitCallback(code)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    enabled = code.isNotEmpty() && code.length > 3
                ) {
                    Text(stringResource(R.string.request_code))
                }
            }
        }
    }
}