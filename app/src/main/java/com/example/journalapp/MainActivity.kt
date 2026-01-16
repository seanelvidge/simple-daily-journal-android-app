package com.example.journalapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import com.example.journalapp.ui.theme.JournalTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JournalViewModel by viewModels {
        JournalViewModel.Factory(
            SafStorageRepository(this, SettingsDataStore(this))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
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

            JournalTheme {
                JournalApp(
                    viewModel = viewModel,
                    onPickFolder = { pickFolderLauncher.launch(null) },
                    onPickFiles = { pickFilesLauncher.launch(arrayOf("*/*")) },
                    onPickPhotos = {
                        pickPhotosLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                )
            }
        }
    }
}
