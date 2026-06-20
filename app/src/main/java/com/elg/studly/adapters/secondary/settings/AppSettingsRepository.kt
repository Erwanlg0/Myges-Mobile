package com.elg.studly.adapters.secondary.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.domain.model.NotificationPreferences
import com.elg.studly.domain.model.RefreshIntervals
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.domain.model.NO_REMINDER_MINUTES
import com.elg.studly.domain.model.UserSettings
import com.elg.studly.domain.model.clampReminderLeadMinutes
import com.elg.studly.domain.model.clampRefreshMinutes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Instant
import com.elg.studly.adapters.time.*
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
                agenda = preferences[NOTIFY_AGENDA] ?: false,
                projects = preferences[NOTIFY_PROJECTS] ?: false,
                documents = preferences[NOTIFY_DOCUMENTS] ?: false
            ),
            calendarSyncEnabled = preferences[CALENDAR_SYNC] ?: false,
            biometricEnabled = preferences[BIOMETRIC_ENABLED] ?: false,
            themeMode = preferences[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
            dynamicColorEnabled = preferences[DYNAMIC_COLOR] ?: false,
            refreshIntervals = preferences.toRefreshIntervals(),
            classReminderLeadMinutes = clampReminderLeadMinutes(preferences[CLASS_REMINDER_LEAD] ?: NO_REMINDER_MINUTES),
            deadlineReminderLeadMinutes = clampReminderLeadMinutes(preferences[DEADLINE_REMINDER_LEAD] ?: NO_REMINDER_MINUTES),
            lastSyncAt = preferences[LAST_SYNC]?.let(Instant::fromEpochMilliseconds)
        )
    }

    private fun Preferences.toRefreshIntervals(): RefreshIntervals {
        val defaults = RefreshIntervals()
        return SyncFeature.entries.fold(defaults) { acc, feature ->
            val stored = this[INTERVAL_KEYS.getValue(feature)] ?: return@fold acc
            acc.with(feature, stored)
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.settingsDataStore.edit { preferences -> preferences[THEME_MODE] = themeMode.name }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setRefreshInterval(feature: SyncFeature, minutes: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[INTERVAL_KEYS.getValue(feature)] = clampRefreshMinutes(minutes)
        }
    }

    override suspend fun setClassReminderLeadMinutes(minutes: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[CLASS_REMINDER_LEAD] = clampReminderLeadMinutes(minutes)
        }
    }

    override suspend fun setDeadlineReminderLeadMinutes(minutes: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DEADLINE_REMINDER_LEAD] = clampReminderLeadMinutes(minutes)
        }
    }

    override suspend fun lastFetchedAt(feature: SyncFeature): Instant? {
        return context.settingsDataStore.data.first()[LAST_FETCH_KEYS.getValue(feature)]
            ?.let(Instant::fromEpochMilliseconds)
    }

    override suspend fun markFeatureFetched(feature: SyncFeature) {
        context.settingsDataStore.edit { preferences ->
            preferences[LAST_FETCH_KEYS.getValue(feature)] = kotlin.time.Clock.System.now().toEpochMilli()
        }
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
        context.settingsDataStore.edit { preferences -> preferences[LAST_SYNC] = kotlin.time.Clock.System.now().toEpochMilli() }
    }

    override suspend fun clearSyncMetadata() {
        context.settingsDataStore.edit { preferences ->
            preferences.remove(LAST_SYNC)
            LAST_FETCH_KEYS.values.forEach(preferences::remove)
        }
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
        val CLASS_REMINDER_LEAD = intPreferencesKey("class_reminder_lead_minutes")
        val DEADLINE_REMINDER_LEAD = intPreferencesKey("deadline_reminder_lead_minutes")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")

        val INTERVAL_KEYS = SyncFeature.entries.associateWith {
            intPreferencesKey("refresh_interval_${it.name.lowercase()}")
        }
        val LAST_FETCH_KEYS = SyncFeature.entries.associateWith {
            longPreferencesKey("last_fetch_${it.name.lowercase()}")
        }
    }
}
