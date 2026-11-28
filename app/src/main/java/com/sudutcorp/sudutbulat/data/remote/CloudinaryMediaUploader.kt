package com.sudutcorp.sudutbulat.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Modular Cloudinary unsigned uploader using standard [HttpURLConnection] (zero extra dependencies).
 * Uploads compressed JPEG bytes into the configured folder (default: `pp`) and returns `secure_url`.
 */
open class CloudinaryMediaUploader(
    private val cloudName: String = DEFAULT_CLOUD_NAME,
    private val uploadPreset: String = DEFAULT_UPLOAD_PRESET,
    private val defaultFolder: String = DEFAULT_PROFILE_PHOTO_FOLDER
) {
    companion object {
        const val DEFAULT_CLOUD_NAME = "fp7jzide"
        const val DEFAULT_UPLOAD_PRESET = "sdb_dev"
        const val DEFAULT_PROFILE_PHOTO_FOLDER = "pp"
        const val DEFAULT_DISTRIBUTION_RECEIPT_FOLDER = "dr"

        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 25_000
    }

    private val json = Json { ignoreUnknownKeys = true }

    open suspend fun uploadProfilePhoto(
        userId: String,
        jpegBytes: ByteArray,
        folder: String = defaultFolder
    ): Result<String> = uploadJpeg(
        filePrefix = "pp",
        identifier = userId,
        defaultIdentifier = "user",
        jpegBytes = jpegBytes,
        folder = folder
    )

    open suspend fun uploadDistributionReceiptPhoto(
        distributionId: String,
        jpegBytes: ByteArray,
        folder: String = DEFAULT_DISTRIBUTION_RECEIPT_FOLDER
    ): Result<String> = uploadJpeg(
        filePrefix = "dr",
        identifier = distributionId,
        defaultIdentifier = "dist",
        jpegBytes = jpegBytes,
        folder = folder
    )

    private suspend fun uploadJpeg(
        filePrefix: String,
        identifier: String,
        defaultIdentifier: String,
        jpegBytes: ByteArray,
        folder: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            require(jpegBytes.isNotEmpty()) { "Data gambar kosong." }
            require(cloudName.isNotBlank()) { "Cloudinary cloud name belum dikonfigurasi." }
            require(uploadPreset.isNotBlank()) { "Cloudinary upload preset belum dikonfigurasi." }

            val endpoint = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
            val boundary = "----SudutBulatBoundary${UUID.randomUUID().toString().replace("-", "")}"
            val safeId = identifier.trim().lowercase().replace(Regex("[^a-z0-9_-]"), "_").ifBlank { defaultIdentifier }
            val fileName = "${filePrefix}_${safeId}_${System.currentTimeMillis()}.jpg"

            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doInput = true
                doOutput = true
                useCaches = false
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            try {
                DataOutputStream(connection.outputStream).use { out ->
                    writeFormField(out, boundary, "upload_preset", uploadPreset)
                    if (folder.isNotBlank()) {
                        writeFormField(out, boundary, "folder", folder)
                        writeFormField(out, boundary, "asset_folder", folder)
                    }
                    writeFileField(out, boundary, "file", fileName, "image/jpeg", jpegBytes)
                    out.writeBytes("--$boundary--\r\n")
                    out.flush()
                }

                val statusCode = connection.responseCode
                val responseStream = if (statusCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val body = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

                if (statusCode !in 200..299) {
                    val remoteErr = parseErrorMessage(body) ?: "HTTP $statusCode"
                    throw IOException("Gagal mengunggah ke Cloudinary: $remoteErr")
                }

                parseSecureUrl(body)
                    ?: throw IOException("Respons Cloudinary tidak berisi secure_url.")
            } finally {
                connection.disconnect()
            }
        }
    }

    internal fun parseSecureUrl(responseBody: String): String? {
        if (responseBody.isBlank()) return null
        return runCatching {
            val obj = json.parseToJsonElement(responseBody).jsonObject
            obj["secure_url"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                ?: obj["url"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    internal fun parseErrorMessage(responseBody: String): String? {
        if (responseBody.isBlank()) return null
        return runCatching {
            val obj = json.parseToJsonElement(responseBody).jsonObject
            obj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun writeFormField(
        out: DataOutputStream,
        boundary: String,
        name: String,
        value: String
    ) {
        out.writeBytes("--$boundary\r\n")
        out.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        out.write(value.toByteArray(Charsets.UTF_8))
        out.writeBytes("\r\n")
    }

    private fun writeFileField(
        out: DataOutputStream,
        boundary: String,
        fieldName: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        out.writeBytes("--$boundary\r\n")
        out.writeBytes("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"\r\n")
        out.writeBytes("Content-Type: $mimeType\r\n\r\n")
        out.write(bytes)
        out.writeBytes("\r\n")
    }
}
