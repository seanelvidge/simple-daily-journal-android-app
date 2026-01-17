package com.example.journalapp

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DateUtils {
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val attachmentFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss")

    fun monthFolderName(month: YearMonth): String = month.format(monthFormatter)

    fun entryFileName(date: LocalDate): String = "${date.format(fileFormatter)}.md"

    fun displayDate(date: LocalDate): String = date.format(displayFormatter)

    fun attachmentTimestamp(now: LocalDateTime): String = now.format(attachmentFormatter)

    fun parseDateFromFileName(fileName: String): LocalDate? {
        if (!fileName.endsWith(".md")) return null
        val base = fileName.removeSuffix(".md")
        return runCatching { LocalDate.parse(base, fileFormatter) }.getOrNull()
    }
}
