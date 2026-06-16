package com.elg.studly.adapters.secondary.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.domain.model.NotificationPreferences
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "myges_settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    override val settings: Flow<UserSettings> = context.settingsDataStore.data.map { preferences ->
        UserSettings(
            languageTag = preferences[LANGUAGE_TAG],
            notifications = NotificationPreferences(
                grades = preferences[NOTIFY_GRADES] ?: true,
                absences = preferences[NOTIFY_ABSENCES] ?: true,
                agenda = preferences[NOTIFY_AGENDA] ?: true,
                projects = preferences[NOTIFY_PROJECTS] ?: true,
                documents = preferences[NOTIFY_DOCUMENTS] ?: true
            ),
            calendarSyncEnabled = preferences[CALENDAR_SYNC] ?: false,
            biometricEnabled = preferences[BIOMETRIC_ENABLED] ?: false,
            themeMode = preferences[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
            lastSyncAt = preferences[LAST_SYNC]?.let(Instant::ofEpochMilli)
        )
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.settingsDataStore.edit { preferences -> preferences[THEME_MODE] = themeMode.name }
    }

    override suspend fun setLanguageTag(languageTag: String?) {
        context.settingsDataStore.edit { preferences ->
            if (languageTag == null) preferences.remove(LANGUAGE_TAG) else preferences[LANGUAGE_TAG] = languageTag
        }
    }

    override suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[CALENDAR_SYNC] = enabled }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun setGradeNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[NOTIFY_GRADES] = enabled }
    }

    override suspend fun setAbsenceNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[NOTIFY_ABSENCES] = enabled }
    }

    override suspend fun setAgendaNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[NOTIFY_AGENDA] = enabled }
    }

    override suspend fun setProjectNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[NOTIFY_PROJECTS] = enabled }
    }

    override suspend fun setDocumentNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[NOTIFY_DOCUMENTS] = enabled }
    }

    override suspend fun markSynced() {
        context.settingsDataStore.edit { preferences -> preferences[LAST_SYNC] = Instant.now().toEpochMilli() }
    }

    override suspend fun clearSyncMetadata() {
        context.settingsDataStore.edit { preferences -> preferences.remove(LAST_SYNC) }
    }

    private companion object {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val CALENDAR_SYNC = booleanPreferencesKey("calendar_sync")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val LAST_SYNC = longPreferencesKey("last_sync")
        val NOTIFY_ABSENCES = booleanPreferencesKey("notify_absences")
        val NOTIFY_AGENDA = booleanPreferencesKey("notify_agenda")
        val NOTIFY_DOCUMENTS = booleanPreferencesKey("notify_documents")
        val NOTIFY_GRADES = booleanPreferencesKey("notify_grades")
        val NOTIFY_PROJECTS = booleanPreferencesKey("notify_projects")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
