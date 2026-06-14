package com.elg.myges.config

import android.content.Context
import androidx.room.Room
import com.elg.myges.BuildConfig
import com.elg.myges.adapters.secondary.api.ApiRetryInterceptor
import com.elg.myges.adapters.secondary.api.MygesAuthInterceptor
import com.elg.myges.adapters.secondary.api.MyGesApiService
import com.elg.myges.adapters.secondary.api.NoContentAsEmptyJsonInterceptor
import com.elg.myges.adapters.secondary.calendar.AndroidCalendarSyncAdapter
import com.elg.myges.adapters.secondary.network.AndroidNetworkMonitor
import com.elg.myges.adapters.secondary.notification.AndroidNotificationScheduler
import com.elg.myges.adapters.secondary.repository.MygesSessionRepository
import com.elg.myges.adapters.secondary.repository.OfflineFirstStudentDataRepository
import com.elg.myges.adapters.secondary.security.DatabasePassphraseStore
import com.elg.myges.adapters.secondary.settings.AppSettingsRepository
import com.elg.myges.adapters.secondary.storage.MygesDatabase
import com.elg.myges.adapters.secondary.storage.StudentDao
import com.elg.myges.adapters.secondary.storage.deletePlaintextDatabaseIfPresent
import com.elg.myges.application.ports.CalendarSyncPort
import com.elg.myges.application.ports.NetworkMonitor
import com.elg.myges.application.ports.NotificationScheduler
import com.elg.myges.application.ports.SessionRepository
import com.elg.myges.application.ports.SettingsRepository
import com.elg.myges.application.ports.StudentDataRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PortBindingModule {
    @Binds
    abstract fun bindCalendarSyncPort(adapter: AndroidCalendarSyncAdapter): CalendarSyncPort

    @Binds
    abstract fun bindNetworkMonitor(monitor: AndroidNetworkMonitor): NetworkMonitor

    @Binds
    abstract fun bindNotificationScheduler(scheduler: AndroidNotificationScheduler): NotificationScheduler

    @Binds
    abstract fun bindSessionRepository(repository: MygesSessionRepository): SessionRepository

    @Binds
    abstract fun bindSettingsRepository(repository: AppSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindStudentDataRepository(repository: OfflineFirstStudentDataRepository): StudentDataRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DependencyModule {
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        appConfig: AppConfig,
        sessionRepository: SessionRepository
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(MygesAuthInterceptor(appConfig.userAgent, sessionRepository))
            .addInterceptor(ApiRetryInterceptor())
            .addInterceptor(NoContentAsEmptyJsonInterceptor())
            .addInterceptor(logging)
            .certificatePinner(MygesCertificatePins.certificatePinner())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        appConfig: AppConfig,
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(appConfig.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideMyGesApiService(retrofit: Retrofit): MyGesApiService {
        return retrofit.create(MyGesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        databasePassphraseStore: DatabasePassphraseStore
    ): MygesDatabase {
        System.loadLibrary("sqlcipher")
        deletePlaintextDatabaseIfPresent(context, DATABASE_NAME)
        return Room.databaseBuilder(context, MygesDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(SupportOpenHelperFactory(databasePassphraseStore.readOrCreate()))
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStudentDao(database: MygesDatabase): StudentDao {
        return database.studentDao()
    }

    private const val DATABASE_NAME = "myges.db"
}
