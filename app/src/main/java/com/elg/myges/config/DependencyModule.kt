package com.elg.myges.config

import android.content.Context
import androidx.room.Room
import com.elg.myges.BuildConfig
import com.elg.myges.adapters.secondary.api.MyGesApiService
import com.elg.myges.adapters.secondary.calendar.AndroidCalendarSyncAdapter
import com.elg.myges.adapters.secondary.network.AndroidNetworkMonitor
import com.elg.myges.adapters.secondary.notification.AndroidNotificationScheduler
import com.elg.myges.adapters.secondary.repository.MygesSessionRepository
import com.elg.myges.adapters.secondary.repository.OfflineFirstStudentDataRepository
import com.elg.myges.adapters.secondary.settings.AppSettingsRepository
import com.elg.myges.adapters.secondary.storage.MygesDatabase
import com.elg.myges.adapters.secondary.storage.StudentDao
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
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
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
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", appConfig.userAgent)
                val session = sessionRepository.currentSession()
                if (session?.isExpired == true || session?.requiresRefresh == true) {
                    sessionRepository.invalidateSession()
                    return@addInterceptor Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(401)
                        .message("Session refresh required")
                        .body("".toResponseBody(null))
                        .build()
                }
                session?.accessToken?.let { token ->
                    requestBuilder.header("Authorization", token)
                }
                chain.proceed(requestBuilder.build()).also { response ->
                    if (response.code == 401) sessionRepository.invalidateSession()
                }
            }
            .addInterceptor(logging)
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
    fun provideDatabase(@ApplicationContext context: Context): MygesDatabase {
        return Room.databaseBuilder(context, MygesDatabase::class.java, "myges.db")
            .build()
    }

    @Provides
    fun provideStudentDao(database: MygesDatabase): StudentDao {
        return database.studentDao()
    }
}
