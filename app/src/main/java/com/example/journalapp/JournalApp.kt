package com.example.journalapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun JournalApp(
    viewModel: JournalViewModel,
    onPickFolder: () -> Unit,
    onPickFiles: () -> Unit,
    onPickPhotos: () -> Unit,
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier,
    ) {
        composable("main") {
            JournalScreen(
                state = state,
                onPickFolder = onPickFolder,
                onPickFiles = onPickFiles,
                onPickPhotos = onPickPhotos,
                onOpenSettings = { navController.navigate("settings") },
                onPrevDay = { viewModel.loadDate(state.currentDate.minusDays(1)) },
                onNextDay = { viewModel.loadDate(state.currentDate.plusDays(1)) },
                onDateSelected = { viewModel.loadDate(it) },
                onMonthChanged = { viewModel.loadMonth(it) },
                onTextChanged = viewModel::onTextChanged,
                onSaveNow = viewModel::saveNow,
            )
        }
        composable("settings") {
            SettingsScreen(
                rootUri = state.rootUri,
                onBack = { navController.popBackStack() },
                onPickFolder = onPickFolder,
            )
        }
    }
}
