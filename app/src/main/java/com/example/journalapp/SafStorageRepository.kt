package com.example.journalapp

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class SafStorageRepository(
    private val context: Context,
    private val settings: SettingsDataStore,
) : StorageRepository {

    override suspend fun getRootUri(): Uri? = withContext(Dispatchers.IO) {
        settings.rootUriFlow.firstOrNull()
    }

    override suspend fun setRootUri(uri: Uri?) {
        settings.setRootUri(uri)
    }

    override suspend fun loadEntry(date: LocalDate): String = withContext(Dispatchers.IO) {
        val root = getRootDocument() ?: return@withContext ""
        val monthFolder = ensureMonthFolder(root, YearMonth.from(date)) ?: return@withContext ""
        val entryFile = monthFolder.findFile(DateUtils.entryFileName(date))
            ?: return@withContext ""
        context.contentResolver.openInputStream(entryFile.uri)?.use { input ->
            input.bufferedReader().readText()
        } ?: ""
    }

    override suspend fun saveEntry(date: LocalDate, text: String) = withContext(Dispatchers.IO) {
        val root = getRootDocument() ?: return@withContext
        val monthFolder = ensureMonthFolder(root, YearMonth.from(date)) ?: return@withContext
        val fileName = DateUtils.entryFileName(date)
        val entryFile = monthFolder.findFile(fileName)
            ?: monthFolder.createFile("text/markdown", fileName)
            ?: return@withContext
        context.contentResolver.openOutputStream(entryFile.uri, "wt")?.use { output ->
            output.bufferedWriter().use { writer ->
                writer.write(text)
            }
        }
    }

    override suspend fun listEntryDates(month: YearMonth): Set<LocalDate> = withContext(Dispatchers.IO) {
        val root = getRootDocument() ?: return@withContext emptySet()
        val monthFolder = ensureMonthFolder(root, month) ?: return@withContext emptySet()
        monthFolder.listFiles()
            .mapNotNull { file -> file.name?.let(DateUtils::parseDateFromFileName) }
            .toSet()
    }

    override suspend fun listAttachmentUris(month: YearMonth): Map<String, Uri> = withContext(Dispatchers.IO) {
        val root = getRootDocument() ?: return@withContext emptyMap()
        val monthFolder = ensureMonthFolder(root, month) ?: return@withContext emptyMap()
        val attachments = ensureAttachmentsFolder(monthFolder) ?: return@withContext emptyMap()
        attachments.listFiles()
            .mapNotNull { file ->
                val name = file.name ?: return@mapNotNull null
                name to file.uri
            }
            .toMap()
    }

    override suspend fun appendAttachments(date: LocalDate, uris: List<Uri>): List<CopiedAttachment> =
        withContext(Dispatchers.IO) {
            val root = getRootDocument() ?: return@withContext emptyList()
            val monthFolder = ensureMonthFolder(root, YearMonth.from(date)) ?: return@withContext emptyList()
            val attachments = ensureAttachmentsFolder(monthFolder) ?: return@withContext emptyList()
            val copied = mutableListOf<CopiedAttachment>()
            for (uri in uris) {
                val displayName = queryDisplayName(uri) ?: "attachment"
                val safeName = AttachmentUtils.safeFileName(displayName)
                val (base, ext) = AttachmentUtils.splitExtension(safeName)
                val timestamp = DateUtils.attachmentTimestamp(LocalDateTime.now())
                val initialBase = "${timestamp}_$base"
                var targetName = AttachmentUtils.withSuffix(initialBase, ext)
                if (attachments.findFile(targetName) != null) {
                    targetName = AttachmentUtils.ensureUniqueName(initialBase, ext)
                }
                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val target = attachments.createFile(mime, targetName) ?: continue
                context.contentResolver.openInputStream(uri)?.use { input ->
                    context.contentResolver.openOutputStream(target.uri)?.use { output ->
                        input.copyTo(output)
                    }
                } ?: continue
                copied += CopiedAttachment(targetName, AttachmentUtils.isImageMime(mime))
            }
            copied
        }

    private suspend fun getRootDocument(): DocumentFile? {
        val rootUri = settings.rootUriFlow.firstOrNull() ?: return null
        return DocumentFile.fromTreeUri(context, rootUri)
    }

    private fun ensureMonthFolder(root: DocumentFile, month: YearMonth): DocumentFile? {
        val name = DateUtils.monthFolderName(month)
        return root.findFile(name) ?: root.createDirectory(name)
    }

    private fun ensureAttachmentsFolder(monthFolder: DocumentFile): DocumentFile? {
        return monthFolder.findFile("attachments") ?: monthFolder.createDirectory("attachments")
    }

    private fun queryDisplayName(uri: Uri): String? {
        val resolver = context.contentResolver
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }
}
