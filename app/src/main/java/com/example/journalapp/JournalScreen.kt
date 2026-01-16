package com.example.journalapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    state: JournalUiState,
    onPickFolder: () -> Unit,
    onPickFiles: () -> Unit,
    onPickPhotos: () -> Unit,
    onOpenSettings: () -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onTextChanged: (String) -> Unit,
    onSaveNow: () -> Unit,
) {
    var showCalendar by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }
    var isPreview by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                onSaveNow()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (state.rootUri == null) {
        SelectFolderScreen(onPickFolder = onPickFolder)
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = DateUtils.displayDate(state.currentDate),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = { showCalendar = true }) {
                            Text(text = "Pick date")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onPrevDay) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
                    }
                },
                actions = {
                    IconButton(onClick = { isPreview = !isPreview }) {
                        if (isPreview) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit mode")
                        } else {
                            Icon(Icons.Default.Visibility, contentDescription = "Preview mode")
                        }
                    }
                    IconButton(onClick = onNextDay) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
                    }
                    IconButton(onClick = { showAttachMenu = true }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach")
                    }
                    IconButton(onClick = { showCalendar = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.errorMessage != null) {
                ErrorBanner(message = state.errorMessage, onReselect = onPickFolder)
            }
            if (isPreview) {
                MarkdownPreview(
                    markdown = state.previewText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                )
            } else {
                EditorCard(
                    text = state.editorText,
                    onTextChanged = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                )
            }
        }
    }

    if (showCalendar) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showCalendar = false },
            sheetState = sheetState,
        ) {
            CalendarSheet(
                month = state.currentMonth,
                selectedDate = state.currentDate,
                entries = state.entriesForMonth,
                onMonthChanged = onMonthChanged,
                onDateSelected = {
                    showCalendar = false
                    onDateSelected(it)
                },
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    DropdownMenu(
        expanded = showAttachMenu,
        onDismissRequest = { showAttachMenu = false },
    ) {
        DropdownMenuItem(
            text = { Text("Photos") },
            onClick = {
                showAttachMenu = false
                onPickPhotos()
            },
        )
        DropdownMenuItem(
            text = { Text("Files") },
            onClick = {
                showAttachMenu = false
                onPickFiles()
            },
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onReselect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
            .padding(12.dp),
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onReselect) {
            Text("Reselect folder")
        }
    }
}

@Composable
private fun EditorCard(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        androidx.compose.material3.OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxSize(),
            textStyle = MaterialTheme.typography.bodyLarge,
            label = { Text("Markdown") },
            placeholder = { Text("Write your day...") },
        )
    }
}

@Composable
private fun MarkdownPreview(
    markdown: String,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        Text(text = "Preview", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        MarkdownView(
            markdown = markdown,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
