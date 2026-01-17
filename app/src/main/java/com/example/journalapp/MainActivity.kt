package com.example.journalapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.journalapp.ui.theme.JournalTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val settings by lazy { SettingsDataStore(this) }
    private val viewModel: JournalViewModel by viewModels {
        JournalViewModel.Factory(
            SafStorageRepository(this, settings)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_JournalApp)
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val themeMode by settings.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val pickFolderLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree(),
                onResult = { uri ->
                    if (uri != null) {
                        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        context.contentResolver.takePersistableUriPermission(uri, flags)
                    }
                    viewModel.onFolderSelected(uri)
                }
            )
            val pickFilesLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenMultipleDocuments(),
                onResult = { uris -> viewModel.attachFiles(uris) }
            )
            val pickPhotosLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickMultipleVisualMedia(),
                onResult = { uris -> viewModel.attachFiles(uris) }
            )

            JournalTheme(mode = themeMode) {
                JournalApp(
                    viewModel = viewModel,
                    onPickFolder = { pickFolderLauncher.launch(null) },
                    onPickFiles = { pickFilesLauncher.launch(arrayOf("*/*")) },
                    onPickPhotos = {
                        pickPhotosLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        coroutineScope.launch {
                            settings.setThemeMode(mode)
                        }
                    },
                )
            }
        }
    }
}
