package com.example.journalapp

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(FlowPreview::class)
class JournalViewModel(
    private val repository: StorageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val textFlow = MutableStateFlow("")
    private var suppressSave = false
    private var attachmentUriMap: Map<String, Uri> = emptyMap()
    private var lastSavedText: String = ""

    init {
        viewModelScope.launch {
            textFlow
                .debounce(800)
                .distinctUntilChanged()
                .collectLatest { text ->
                    if (!suppressSave) {
                        saveCurrent(text)
                    }
                }
        }

        viewModelScope.launch {
            val root = repository.getRootUri()
            _uiState.update { it.copy(rootUri = root) }
            if (root != null) {
                loadDate(LocalDate.now())
            }
        }
    }

    fun onFolderSelected(uri: Uri?) {
        viewModelScope.launch {
            repository.setRootUri(uri)
            _uiState.update { it.copy(rootUri = uri) }
            if (uri != null) {
                loadDate(LocalDate.now())
            }
        }
    }

    fun loadDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            suppressSave = true
            try {
                val text = repository.loadEntry(date)
                val month = YearMonth.from(date)
                attachmentUriMap = repository.listAttachmentUris(month)
                val preview = transformMarkdown(text, attachmentUriMap)
                val entries = repository.listEntryDates(month)
                lastSavedText = text
                _uiState.update {
                    it.copy(
                        currentDate = date,
                        currentMonth = month,
                        editorText = text,
                        previewText = preview,
                        entriesForMonth = entries,
                        errorMessage = null,
                    )
                }
                textFlow.value = text
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Folder access lost. Please reselect.") }
            } finally {
                suppressSave = false
            }
        }
    }

    fun loadMonth(month: YearMonth) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entries = repository.listEntryDates(month)
                _uiState.update { it.copy(currentMonth = month, entriesForMonth = entries) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Folder access lost. Please reselect.") }
            }
        }
    }

    fun onTextChanged(text: String) {
        _uiState.update {
            it.copy(
                editorText = text,
                previewText = transformMarkdown(text, attachmentUriMap),
            )
        }
        textFlow.value = text
    }

    fun saveNow() {
        viewModelScope.launch {
            saveCurrent(_uiState.value.editorText)
        }
    }

    fun attachFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val date = _uiState.value.currentDate
                val copied = repository.appendAttachments(date, uris)
                if (copied.isEmpty()) return@launch
                val appended = appendAttachmentsToMarkdown(_uiState.value.editorText, copied)
                attachmentUriMap = repository.listAttachmentUris(YearMonth.from(date))
                val preview = transformMarkdown(appended, attachmentUriMap)
                _uiState.update {
                    it.copy(editorText = appended, previewText = preview)
                }
                textFlow.value = appended
                saveCurrent(appended)
                val entries = repository.listEntryDates(YearMonth.from(date))
                _uiState.update { it.copy(entriesForMonth = entries) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Folder access lost. Please reselect.") }
            }
        }
    }

    private suspend fun saveCurrent(text: String) {
        val date = _uiState.value.currentDate
        if (text == lastSavedText) return
        try {
            repository.saveEntry(date, text)
            lastSavedText = text
            val entries = repository.listEntryDates(YearMonth.from(date))
            _uiState.update { it.copy(entriesForMonth = entries) }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Folder access lost. Please reselect.") }
        }
    }

    private fun appendAttachmentsToMarkdown(text: String, attachments: List<CopiedAttachment>): String {
        val builder = StringBuilder(text)
        if (builder.isNotEmpty() && !builder.endsWith("\n")) {
            builder.append("\n")
        }
        if (builder.isNotEmpty()) {
            builder.append("\n")
        }
        for (attachment in attachments) {
            val line = if (attachment.isImage) {
                "![${attachment.name}](attachments/${attachment.name})"
            } else {
                "- [${attachment.name}](attachments/${attachment.name})"
            }
            builder.append(line).append("\n")
        }
        return builder.toString()
    }

    private fun transformMarkdown(text: String, attachmentMap: Map<String, Uri>): String {
        if (attachmentMap.isEmpty()) return text
        val regex = Regex("\\((attachments/[^)]+)\\)")
        return regex.replace(text) { match ->
            val path = match.groupValues[1]
            val name = path.removePrefix("attachments/")
            val uri = attachmentMap[name]?.toString() ?: path
            "(${uri})"
        }
    }

    class Factory(
        private val repository: StorageRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return JournalViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


data class JournalUiState(
    val rootUri: Uri? = null,
    val currentDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.from(LocalDate.now()),
    val editorText: String = "",
    val previewText: String = "",
    val entriesForMonth: Set<LocalDate> = emptySet(),
    val errorMessage: String? = null,
)
