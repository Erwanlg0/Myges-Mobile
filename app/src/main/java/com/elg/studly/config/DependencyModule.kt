package com.elg.studly.config

import android.content.Context
import androidx.room.Room
import com.elg.studly.BuildConfig
import com.elg.studly.adapters.secondary.api.ApiRetryInterceptor
import com.elg.studly.adapters.secondary.api.MygesAuthInterceptor
import com.elg.studly.adapters.secondary.api.MyGesApiService
import com.elg.studly.adapters.secondary.api.NoContentAsEmptyJsonInterceptor
import com.elg.studly.adapters.secondary.calendar.AndroidCalendarSyncAdapter
import com.elg.studly.adapters.secondary.network.AndroidNetworkMonitor
import com.elg.studly.adapters.secondary.notification.AndroidNotificationScheduler
import com.elg.studly.adapters.secondary.repository.MygesSessionRepository
import com.elg.studly.adapters.secondary.repository.OfflineFirstStudentDataRepository
import com.elg.studly.adapters.secondary.security.DatabasePassphraseStore
import com.elg.studly.adapters.secondary.settings.AppSettingsRepository
import com.elg.studly.adapters.secondary.storage.MygesDatabase
import com.elg.studly.adapters.secondary.storage.StudentDao
import com.elg.studly.adapters.secondary.storage.deletePlaintextDatabaseIfPresent
import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.application.ports.NetworkMonitor
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.ConcurrentHashMap
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
            .cookieJar(InMemoryCookieJar())
            .addInterceptor(MygesAuthInterceptor(appConfig.userAgent, appConfig.apiBaseUrl, sessionRepository))
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

    private const val DATABASE_NAME = "Studly.db"
}


internal class InMemoryCookieJar : CookieJar {
    private val store = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existing = store.getOrPut(host) { mutableListOf() }
        cookies.forEach { cookie ->
            existing.removeAll { it.name == cookie.name }
            existing.add(cookie)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val cookies = store[url.host] ?: return emptyList()
        cookies.removeAll { it.expiresAt < now }
        return cookies.filter { it.matches(url) }
    }
}
