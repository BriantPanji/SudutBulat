package com.sudutcorp.sudutbulat.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.ProfileImageProcessor

/**
 * Reusable circular avatar component that offloads Base64/photo decoding to [kotlinx.coroutines.Dispatchers.IO]
 * with an in-memory [android.util.LruCache] and falls back to 1-or-2 word initials when no photo is set.
 */
@Composable
fun SBAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    photoUrl: String? = null,
    size: Dp = 44.dp,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    containerColor: Color = SBColor.Secondary_100,
    contentColor: Color = SBColor.Primary_500,
    isLoading: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    val cleanUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() }
    val cachedBitmap = ProfileImageProcessor.getCachedBitmap(cleanUrl)
    val decodedBitmap by produceState<ImageBitmap?>(initialValue = cachedBitmap, key1 = cleanUrl) {
        value = if (cleanUrl == null) {
            null
        } else {
            ProfileImageProcessor.getCachedBitmap(cleanUrl)
                ?: ProfileImageProcessor.decodePhotoUrl(cleanUrl)
        }
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(clickableModifier),
        shape = CircleShape,
        color = containerColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = decodedBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription ?: initials,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = initials,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = contentColor
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size((size * 0.36f).coerceIn(16.dp, 28.dp)),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
