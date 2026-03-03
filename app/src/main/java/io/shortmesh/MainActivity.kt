package io.shortmesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.shortmesh.sdk.ShortMeshEndpoints
import io.shortmesh.sdk.ShortMeshWidget
import io.shortmesh.ui.theme.ShortMeshSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShortMeshSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                ShortMeshWidget.launch(
                                    context    = this@MainActivity,
                                    identifier = "+237650393369",
                                    endpoints  = ShortMeshEndpoints(
                                        platforms = "https://7lr8ppqk-4000.uks1.devtunnels.ms/api/v1/platforms",
                                        sendOtp   = "https://7lr8ppqk-4000.uks1.devtunnels.ms/api/v1/otp/generate",
                                        verifyOtp = "https://7lr8ppqk-4000.uks1.devtunnels.ms/api/v1/otp/verify",
                                        resendOtp = "https://7lr8ppqk-4000.uks1.devtunnels.ms/api/v1/otp/resend"
                                    ),
                                    onSuccess = { /* proceed to next screen */ },
                                    onError   = { error -> /* handle error */ }
                                )
                            }
                        ) {
                            Text("Start Verification")
                        }
                    }
                }
            }
        }
    }
}
