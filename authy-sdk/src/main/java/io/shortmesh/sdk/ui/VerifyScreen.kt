package io.shortmesh.sdk.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.shortmesh.sdk.R
import io.shortmesh.sdk.network.SupportedPlatforms
import io.shortmesh.sdk.viewmodel.AuthyViewModel
import kotlinx.coroutines.NonCancellable.isActive

@Composable
fun VerifyScreen( viewModel: AuthyViewModel) {
    val platforms by viewModel.supportedPlatforms.collectAsState()
    VerifyScreenComponents(platforms) {
        TODO()
    }
}

@Preview(showBackground = true)
@Composable
private fun VerifyScreenComponents(
    platforms: List<SupportedPlatforms>? = emptyList(),
    onClose: () -> Unit = {},
) {
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
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Verify your account",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (platforms.isNullOrEmpty()) {
                NoAvailablePlatforms()
            }
            else {
                ListPlatforms(platforms)
            }

        }
    }

}


@Preview(showBackground = true)
@Composable
private fun NoAvailablePlatforms() {
    Column() {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No available verification methods. Contact support for assistance.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(
//                    onClick = onClose,
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text("Close")
//                }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Powered by ShortMesh",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListPlatforms(
    platforms: List<SupportedPlatforms>? = emptyList(),
) {
    Column {
        Text(
            "Choose how you'd like to be reached.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        platforms?.forEach { platform ->
            PlatformCard(displayName = platform.display_name, iconUrl = platform.icon_url) {
                // Handle selection
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
//            if (error != null) {
//                Text(
//                    text = error,
//                    fontSize = 12.sp,
//                    color = MaterialTheme.colorScheme.error,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//            }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { TODO() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { TODO() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Select")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Powered by ShortMesh",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Preview
@Composable
private fun PlatformCard(
    displayName: String = "",
    iconUrl: String = "",
    onClick: () -> Unit = {}
) {
    Box {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = if (selected == platform.id)
//                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
//                else
//                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
//            ),
//            border = if (selected == platform.id)
//                CardDefaults.outlinedCardBorder()
//            else null
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                GlideImage(
                    model = iconUrl,
                    contentDescription = "Platform icon",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(50.dp),
                    loading = placeholder(R.drawable.outline_downloading_24),
                    failure = placeholder(R.drawable.outline_broken_image_24)
                ) {
                    it.diskCacheStrategy(DiskCacheStrategy.ALL) // Caches both original and resized images
                        .circleCrop()                             // Makes the image a circle
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(displayName)
            }
        }
    }
}
