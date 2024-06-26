package com.mhss.app.presentation

sealed class SettingsEvent {
    data class ImportData(val fileUri: String, val encrypted: Boolean, val password: String): SettingsEvent()
    data class ExportData(val directoryUri: String, val encrypted: Boolean, val password: String): SettingsEvent()
}