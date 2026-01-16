package com.example.journalapp

import android.webkit.MimeTypeMap
import java.util.Locale
import java.util.UUID

object AttachmentUtils {
    fun safeFileName(name: String): String {
        val trimmed = name.trim().ifEmpty { "attachment" }
        return trimmed.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    fun splitExtension(name: String): Pair<String, String?> {
        val lastDot = name.lastIndexOf('.')
        return if (lastDot > 0 && lastDot < name.length - 1) {
            name.substring(0, lastDot) to name.substring(lastDot + 1)
        } else {
            name to null
        }
    }

    fun withSuffix(base: String, extension: String?): String {
        return if (extension.isNullOrBlank()) base else "$base.$extension"
    }

    fun ensureUniqueName(base: String, extension: String?): String {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        return withSuffix("${base}_$suffix", extension)
    }

    fun isImageMime(mime: String?): Boolean = mime?.lowercase(Locale.US)?.startsWith("image/") == true

    fun extensionFromMime(mime: String?): String? {
        if (mime.isNullOrBlank()) return null
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
    }
}
