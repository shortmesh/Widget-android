package io.shortmesh.sdk

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OtpScreen(
    platform: String,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var secondsLeft by remember { mutableStateOf(30) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter verification code",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Sent via $platform",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            BasicTextField(
                value = code,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(6) { index ->
                            val char = code.getOrNull(index)
                            val isFocused = index == code.length

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = if (isFocused)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char?.toString() ?: "",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (secondsLeft > 0) {
                Text(
                    "Didn't receive a code? Resend available in ${secondsLeft}s",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
                val resendText = buildAnnotatedString {
                    withStyle(SpanStyle(color = mutedColor)) {
                        append("Didn't receive a code? ")
                    }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "RESEND",
                            styles = TextLinkStyles(
                                style = SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)
                            ),
                            linkInteractionListener = {
                                secondsLeft = 30
                                // TODO: API call here,
                                // viewModel.resendOtp(platform)
                            }
                        )
                    ) {
                        append("Resend")
                    }
                }
                Text(
                    text = resendText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onBack() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("Go back")
                }

                Button(
                    onClick = { onSubmit(code) },
                    enabled = code.length == 6,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("Continue")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Powered by ShortMesh", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
