package com.example.journalapp

import android.net.Uri
import java.time.LocalDate
import java.time.YearMonth

interface StorageRepository {
    suspend fun getRootUri(): Uri?
    suspend fun setRootUri(uri: Uri?)

    suspend fun loadEntry(date: LocalDate): String
    suspend fun saveEntry(date: LocalDate, text: String)

    suspend fun listEntryDates(month: YearMonth): Set<LocalDate>
    suspend fun listAttachmentUris(month: YearMonth): Map<String, Uri>

    suspend fun appendAttachments(date: LocalDate, uris: List<Uri>): List<CopiedAttachment>
}

data class CopiedAttachment(
    val name: String,
    val isImage: Boolean,
)
