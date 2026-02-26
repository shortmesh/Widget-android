package io.shortmesh.sdk


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ShortMeshActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        val apiEndpoint = intent.getStringExtra("apiEndpoint") ?: ""

        setContent {
            ShortMeshRoot(
                phoneNumber = phoneNumber,
                apiEndpoint = apiEndpoint
            )
        }
    }
}