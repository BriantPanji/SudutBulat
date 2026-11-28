package com.sudutcorp.sudutbulat.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.min

/**
 * Modular processor for capturing, normalizing (EXIF orientation + 1:1 center-crop + 512px downscale),
 * loading remote Cloudinary URLs (and legacy Base64 data URLs) off the main thread, and caching
 * decoded [ImageBitmap]s in memory.
 */
object ProfileImageProcessor {

    private const val TARGET_AVATAR_PX = 512
    private const val MAX_DECODE_DIMENSION_PX = 1024
    private const val JPEG_QUALITY = 85
    private const val CAMERA_CACHE_DIR = "camera_captures"
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    private val bitmapCache = object : LruCache<String, ImageBitmap>(32) {}

    /**
     * Creates a temporary [Uri] inside `cacheDir/camera_captures/` exposed via [FileProvider]
     * for [androidx.activity.result.contract.ActivityResultContracts.TakePicture].
     */
    fun createTempCameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, CAMERA_CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
        val cutoff = System.currentTimeMillis() - 15 * 60 * 1000L
        dir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                runCatching { file.delete() }
            }
        }
        val tempFile = File.createTempFile("avatar_${System.currentTimeMillis()}_", ".jpg", dir)
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }

    /**
     * Reads [uri] on [Dispatchers.IO], corrects EXIF orientation, center-crops to 1:1 square,
     * scales to [TARGET_AVATAR_PX]x[TARGET_AVATAR_PX], and returns compressed JPEG bytes ready for upload.
     */
    suspend fun processUriToJpegBytes(context: Context, uri: Uri): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver

                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                }
                require(boundsOptions.outWidth > 0 && boundsOptions.outHeight > 0) {
                    "Format gambar tidak didukung atau file rusak."
                }

                val sampleSize = calculateInSampleSize(
                    width = boundsOptions.outWidth,
                    height = boundsOptions.outHeight,
                    maxDimension = MAX_DECODE_DIMENSION_PX
                )

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val rawBitmap = resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                } ?: throw IllegalStateException("Gagal membaca gambar dari perangkat.")

                val exifOrientation = runCatching {
                    resolver.openInputStream(uri)?.use { stream ->
                        ExifInterface(stream).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    } ?: ExifInterface.ORIENTATION_NORMAL
                }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

                val orientedBitmap = applyExifOrientation(rawBitmap, exifOrientation)
                val squareBitmap = centerCropAndScaleSquare(orientedBitmap, TARGET_AVATAR_PX)

                val output = ByteArrayOutputStream()
                squareBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
                output.toByteArray()
            }
        }

    fun cacheJpegForUrl(photoUrl: String, jpegBytes: ByteArray) {
        if (photoUrl.isBlank() || jpegBytes.isEmpty()) return
        runCatching {
            val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
            if (bitmap != null) {
                bitmapCache.put(photoUrl, bitmap.asImageBitmap())
            }
        }
    }

    fun getCachedBitmap(photoUrl: String?): ImageBitmap? {
        if (photoUrl.isNullOrBlank()) return null
        return bitmapCache.get(photoUrl)
    }

    /**
     * Loads an HTTPS/HTTP Cloudinary URL (or legacy `data:image/...;base64,...` string) into an
     * [ImageBitmap] on [Dispatchers.IO] and caches it in [bitmapCache].
     */
    suspend fun decodePhotoUrl(photoUrl: String?): ImageBitmap? {
        val clean = photoUrl?.trim()?.takeIf { it.isNotBlank() } ?: return null
        bitmapCache.get(clean)?.let { return it }

        return withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = when {
                    clean.startsWith("http://", ignoreCase = true) ||
                        clean.startsWith("https://", ignoreCase = true) -> {
                        downloadBitmapFromUrl(clean)
                    }
                    clean.startsWith("data:image", ignoreCase = true) -> {
                        val payload = clean.substringAfter("base64,", "")
                        if (payload.isBlank()) null else {
                            val bytes = Base64.decode(payload, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                    }
                    else -> null
                } ?: return@runCatching null

                bitmap.asImageBitmap().also { img ->
                    bitmapCache.put(clean, img)
                }
            }.getOrNull()
        }
    }

    private fun downloadBitmapFromUrl(urlString: String): Bitmap? {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
        }
        return try {
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } finally {
            connection.disconnect()
        }
    }

    internal fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var inSampleSize = 1
        val largest = max(width, height)
        while (largest / (inSampleSize * 2) >= maxDimension) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(-1f, 1f)
            }
            else -> return source
        }
        return runCatching {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }.getOrDefault(source)
    }

    internal fun centerCropAndScaleSquare(source: Bitmap, targetSizePx: Int): Bitmap {
        val side = min(source.width, source.height).coerceAtLeast(1)
        val xOffset = ((source.width - side) / 2).coerceAtLeast(0)
        val yOffset = ((source.height - side) / 2).coerceAtLeast(0)
        val cropped = if (xOffset == 0 && yOffset == 0 && source.width == source.height) {
            source
        } else {
            Bitmap.createBitmap(source, xOffset, yOffset, side, side)
        }
        return if (cropped.width == targetSizePx && cropped.height == targetSizePx) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, targetSizePx, targetSizePx, true)
        }
    }
}
