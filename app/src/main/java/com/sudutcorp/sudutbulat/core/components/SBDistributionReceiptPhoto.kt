package com.sudutcorp.sudutbulat.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.ProfileImageProcessor

/**
 * Displays a 1:1 distribution receipt photo loaded off the main thread from Cloudinary
 * (with in-memory LruCache via [ProfileImageProcessor]) and supports tap-to-zoom preview.
 */
@Composable
fun SBDistributionReceiptPhotoCard(
    photoUrl: String,
    modifier: Modifier = Modifier,
    title: String = "Foto bukti penerimaan"
) {
    val cleanUrl = photoUrl.trim()
    if (cleanUrl.isBlank()) return

    var isPreviewOpen by rememberSaveable(cleanUrl) { mutableStateOf(false) }
    val cachedBitmap = ProfileImageProcessor.getCachedBitmap(cleanUrl)
    val loadState by produceState<Pair<Boolean, ImageBitmap?>>(
        initialValue = (cachedBitmap == null) to cachedBitmap,
        key1 = cleanUrl
    ) {
        val existing = ProfileImageProcessor.getCachedBitmap(cleanUrl)
        if (existing != null) {
            value = false to existing
        } else {
            value = true to null
            val decoded = ProfileImageProcessor.decodePhotoUrl(cleanUrl)
            value = false to decoded
        }
    }
    val (isLoading, bitmap) = loadState

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
            if (bitmap != null) {
                Text(
                    text = "Ketuk untuk perbesar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Primary_550
                )
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Grey_100.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, SBColor.Grey_200)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (bitmap != null) {
                            Modifier.clickable { isPreviewOpen = true }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    bitmap != null -> {
                        Image(
                            bitmap = bitmap,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                            color = SBColor.Primary_550
                        )
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_gallery),
                                contentDescription = null,
                                tint = SBColor.Grey_400,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Gagal memuat foto bukti",
                                fontSize = 12.sp,
                                color = SBColor.Grey_500
                            )
                        }
                    }
                }
            }
        }
    }

    if (isPreviewOpen && bitmap != null) {
        Dialog(
            onDismissRequest = { isPreviewOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .clickable { isPreviewOpen = false }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black
                    ) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Surface(
                        onClick = { isPreviewOpen = false },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Tutup",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
