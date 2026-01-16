package com.example.journalapp

enum class ThemeMode(val storageValue: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return values().firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}
