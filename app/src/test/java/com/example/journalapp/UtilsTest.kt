package com.example.journalapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class UtilsTest {
    @Test
    fun monthFolderName_formatsAsYearMonth() {
        val month = YearMonth.of(2026, 1)
        assertEquals("2026-01", DateUtils.monthFolderName(month))
    }

    @Test
    fun entryFileName_formatsAsDateMarkdown() {
        val date = LocalDate.of(2026, 1, 16)
        assertEquals("2026-01-16.md", DateUtils.entryFileName(date))
    }

    @Test
    fun parseDateFromFileName_parsesMarkdownDates() {
        val parsed = DateUtils.parseDateFromFileName("2026-01-16.md")
        assertNotNull(parsed)
        assertEquals(LocalDate.of(2026, 1, 16), parsed)
    }

    @Test
    fun attachmentTimestamp_formats() {
        val stamp = DateUtils.attachmentTimestamp(LocalDateTime.of(2026, 1, 16, 9, 5, 2))
        assertEquals("2026-01-16_090502", stamp)
    }

    @Test
    fun safeFileName_replacesInvalidChars() {
        assertEquals("a_b_c", AttachmentUtils.safeFileName("a/b:c"))
    }

    @Test
    fun splitExtension_handlesExtension() {
        val (base, ext) = AttachmentUtils.splitExtension("photo.jpg")
        assertEquals("photo", base)
        assertEquals("jpg", ext)
    }

    @Test
    fun withSuffix_appendsExtension() {
        assertEquals("note.txt", AttachmentUtils.withSuffix("note", "txt"))
    }
}
