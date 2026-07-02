package io.shortmesh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.shortmesh.sdk.ui.AuthyWidgetLauncherView
import io.shortmesh.sdk.viewmodel.AuthyViewModel
import io.shortmesh.ui.theme.ShortMeshSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShortMeshSDKTheme {
                var showAuthyWidget by remember { mutableStateOf(false) }
                val authyViewModel: AuthyViewModel by viewModels()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if(!showAuthyWidget) {
                            Button(
                                onClick = { showAuthyWidget = true }
                            ) {
                                Text("Select Platform")
                            }
                        } else {
                            AuthyWidgetLauncherView(
                                showDialog = showAuthyWidget,
                                authyUrl = "https://authy.shortmesh.com",
                                viewModel = authyViewModel,
                                requestCodeCallback = {},
                                sendCodeCallback = {},
                            ) {
                                showAuthyWidget = false
                            }
                        }
                    }
                }
            }
        }
    }
}
