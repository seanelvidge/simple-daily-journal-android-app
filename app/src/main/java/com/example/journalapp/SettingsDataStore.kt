package com.example.journalapp

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "journal_settings")

class SettingsDataStore(private val context: Context) {
    private val rootUriKey = stringPreferencesKey("root_uri")

    val rootUriFlow: Flow<Uri?> = context.dataStore.data.map { prefs ->
        prefs[rootUriKey]?.let(Uri::parse)
    }

    suspend fun setRootUri(uri: Uri?) {
        context.dataStore.edit { prefs ->
            if (uri == null) {
                prefs.remove(rootUriKey)
            } else {
                prefs[rootUriKey] = uri.toString()
            }
        }
    }
}
