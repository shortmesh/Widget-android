package io.shortmesh.sdk.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.shortmesh.sdk.R
import io.shortmesh.sdk.network.SupportedPlatforms
import io.shortmesh.sdk.viewmodel.AuthyViewModel

@Composable
fun ListPlatformsScreen(
    viewModel: AuthyViewModel,
    onClose: () -> Unit,
) {
    val platforms by viewModel.supportedPlatforms.collectAsState()
    ListPlatformsScreenComponents(
        platforms,
        onClick = { platform ->
            viewModel.selectPlatform(platform)
        },
        onClose = onClose
    )
}

@Preview(showBackground = true)
@Composable
private fun ListPlatformsScreenComponents(
    platforms: List<SupportedPlatforms>? = emptyList(),
    onClick: (SupportedPlatforms) -> Unit = {},
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
                stringResource(R.string.verify_your_account),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (platforms.isNullOrEmpty()) {
                NoAvailablePlatforms()
            }
            else {
                ListPlatforms(
                    platforms,
                    onClick,
                    onClose
                )
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
private fun ListPlatforms(
    platforms: List<SupportedPlatforms>? = emptyList(),
    onClick: (SupportedPlatforms) -> Unit = {},
    onClose: () -> Unit = {},
) {
    Column {
        Text(
            stringResource(R.string.choose_a_platform_to_receive_your_code),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        platforms?.forEach { platform ->
            PlatformCard(displayName = platform.display_name, iconUrl = platform.icon_url) {
                onClick(platform)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onClose,
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            stringResource(R.string.powered_by_shortmesh),
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
