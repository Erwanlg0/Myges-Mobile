package com.elg.studly.adapters.secondary.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elg.studly.domain.model.SyncFeature
import com.elg.studly.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSettingsRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: AppSettingsRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        repository = AppSettingsRepository(context)
    }

    @Test
    fun defaultSettingsAreCorrect() = runTest {
        val settings = repository.settings.first()
        
        assertEquals(null, settings.languageTag)
        assertEquals(ThemeMode.System, settings.themeMode)
        assertFalse(settings.calendarSyncEnabled)
        assertFalse(settings.biometricEnabled)
        assertFalse(settings.dynamicColorEnabled)
        assertTrue(settings.notifications.grades)
        assertTrue(settings.notifications.absences)
        assertFalse(settings.notifications.agenda)
        assertFalse(settings.notifications.projects)
        assertFalse(settings.notifications.documents)
    }

    @Test
    fun updatesAndReadsSettings() = runTest {
        repository.setLanguageTag("en")
        repository.setThemeMode(ThemeMode.Dark)
        repository.setCalendarSyncEnabled(true)
        repository.setBiometricEnabled(true)
        repository.setDynamicColorEnabled(true)
        
        val settings = repository.settings.first()
        
        assertEquals("en", settings.languageTag)
        assertEquals(ThemeMode.Dark, settings.themeMode)
        assertTrue(settings.calendarSyncEnabled)
        assertTrue(settings.biometricEnabled)
        assertTrue(settings.dynamicColorEnabled)
    }

    @Test
    fun handlesSyncMetadata() = runTest {
        repository.markSynced()
        repository.markFeatureFetched(SyncFeature.Agenda)
        
        val settings = repository.settings.first()
        assertTrue(settings.lastSyncAt != null)
        assertTrue(repository.lastFetchedAt(SyncFeature.Agenda) != null)
        
        repository.clearSyncMetadata()
        
        val clearedSettings = repository.settings.first()
        assertEquals(null, clearedSettings.lastSyncAt)
        assertEquals(null, repository.lastFetchedAt(SyncFeature.Agenda))
    }
}
