package com.elg.studly.config

import com.elg.studly.application.ports.CalendarSyncPort
import com.elg.studly.application.ports.NotificationScheduler
import com.elg.studly.application.ports.SessionRepository
import com.elg.studly.application.ports.SettingsRepository
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.application.usecase.CalendarAccountsUseCase
import com.elg.studly.application.usecase.ClearCacheUseCase
import com.elg.studly.application.usecase.CompleteOAuthLoginUseCase
import com.elg.studly.application.usecase.DownloadDocumentUseCase
import com.elg.studly.application.usecase.JoinGroupUseCase
import com.elg.studly.application.usecase.LeaveGroupUseCase
import com.elg.studly.application.usecase.LogoutUseCase
import com.elg.studly.application.usecase.ObserveAbsencesUseCase
import com.elg.studly.application.usecase.ObserveAgendaUseCase
import com.elg.studly.application.usecase.ObserveCoursesUseCase
import com.elg.studly.application.usecase.ObserveDashboardUseCase
import com.elg.studly.application.usecase.ObserveDirectoryUseCase
import com.elg.studly.application.usecase.ObserveDocumentsUseCase
import com.elg.studly.application.usecase.ObserveEventsUseCase
import com.elg.studly.application.usecase.ObserveGradesUseCase
import com.elg.studly.application.usecase.ObserveLockedBiometricSessionUseCase
import com.elg.studly.application.usecase.ObserveNewsUseCase
import com.elg.studly.application.usecase.ObservePracticalsUseCase
import com.elg.studly.application.usecase.ObserveProjectsUseCase
import com.elg.studly.application.usecase.ObserveSessionUseCase
import com.elg.studly.application.usecase.ObserveSettingsUseCase
import com.elg.studly.application.usecase.ProjectMessagesUseCase
import com.elg.studly.application.usecase.RefreshStudentDataUseCase
import com.elg.studly.application.usecase.RescheduleSyncUseCase
import com.elg.studly.application.usecase.SendProjectMessageUseCase
import com.elg.studly.application.usecase.SyncAgendaToCalendarUseCase
import com.elg.studly.application.usecase.UnlockWithBiometricsUseCase
import com.elg.studly.application.usecase.UpdateSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideCompleteOAuthLoginUseCase(
        sessionRepository: SessionRepository,
        settingsRepository: SettingsRepository,
        notificationScheduler: NotificationScheduler
    ): CompleteOAuthLoginUseCase = CompleteOAuthLoginUseCase(sessionRepository, settingsRepository, notificationScheduler)

    @Provides
    fun provideObserveSessionUseCase(sessionRepository: SessionRepository): ObserveSessionUseCase =
        ObserveSessionUseCase(sessionRepository)

    @Provides
    fun provideObserveLockedBiometricSessionUseCase(sessionRepository: SessionRepository): ObserveLockedBiometricSessionUseCase =
        ObserveLockedBiometricSessionUseCase(sessionRepository)

    @Provides
    fun provideUnlockWithBiometricsUseCase(sessionRepository: SessionRepository): UnlockWithBiometricsUseCase =
        UnlockWithBiometricsUseCase(sessionRepository)

    @Provides
    fun provideLogoutUseCase(
        sessionRepository: SessionRepository,
        notificationScheduler: NotificationScheduler
    ): LogoutUseCase = LogoutUseCase(sessionRepository, notificationScheduler)

    @Provides
    fun provideObserveDashboardUseCase(repository: StudentDataRepository): ObserveDashboardUseCase =
        ObserveDashboardUseCase(repository)

    @Provides
    fun provideObserveAgendaUseCase(repository: StudentDataRepository): ObserveAgendaUseCase =
        ObserveAgendaUseCase(repository)

    @Provides
    fun provideObserveGradesUseCase(repository: StudentDataRepository): ObserveGradesUseCase =
        ObserveGradesUseCase(repository)

    @Provides
    fun provideObserveAbsencesUseCase(repository: StudentDataRepository): ObserveAbsencesUseCase =
        ObserveAbsencesUseCase(repository)

    @Provides
    fun provideObserveCoursesUseCase(repository: StudentDataRepository): ObserveCoursesUseCase =
        ObserveCoursesUseCase(repository)

    @Provides
    fun provideObserveProjectsUseCase(repository: StudentDataRepository): ObserveProjectsUseCase =
        ObserveProjectsUseCase(repository)

    @Provides
    fun provideObservePracticalsUseCase(repository: StudentDataRepository): ObservePracticalsUseCase =
        ObservePracticalsUseCase(repository)

    @Provides
    fun provideObserveDocumentsUseCase(repository: StudentDataRepository): ObserveDocumentsUseCase =
        ObserveDocumentsUseCase(repository)

    @Provides
    fun provideObserveDirectoryUseCase(repository: StudentDataRepository): ObserveDirectoryUseCase =
        ObserveDirectoryUseCase(repository)

    @Provides
    fun provideObserveNewsUseCase(repository: StudentDataRepository): ObserveNewsUseCase =
        ObserveNewsUseCase(repository)

    @Provides
    fun provideObserveEventsUseCase(repository: StudentDataRepository): ObserveEventsUseCase =
        ObserveEventsUseCase(repository)

    @Provides
    fun provideRefreshStudentDataUseCase(
        repository: StudentDataRepository,
        settingsRepository: SettingsRepository,
        calendarSyncPort: CalendarSyncPort,
        notificationScheduler: NotificationScheduler
    ): RefreshStudentDataUseCase =
        RefreshStudentDataUseCase(repository, settingsRepository, calendarSyncPort, notificationScheduler)

    @Provides
    fun provideClearCacheUseCase(
        repository: StudentDataRepository,
        settingsRepository: SettingsRepository
    ): ClearCacheUseCase = ClearCacheUseCase(repository, settingsRepository)

    @Provides
    fun provideDownloadDocumentUseCase(repository: StudentDataRepository): DownloadDocumentUseCase =
        DownloadDocumentUseCase(repository)

    @Provides
    fun provideJoinGroupUseCase(repository: StudentDataRepository): JoinGroupUseCase =
        JoinGroupUseCase(repository)

    @Provides
    fun provideLeaveGroupUseCase(repository: StudentDataRepository): LeaveGroupUseCase =
        LeaveGroupUseCase(repository)

    @Provides
    fun provideProjectMessagesUseCase(repository: StudentDataRepository): ProjectMessagesUseCase =
        ProjectMessagesUseCase(repository)

    @Provides
    fun provideSendProjectMessageUseCase(repository: StudentDataRepository): SendProjectMessageUseCase =
        SendProjectMessageUseCase(repository)

    @Provides
    fun provideObserveSettingsUseCase(repository: SettingsRepository): ObserveSettingsUseCase =
        ObserveSettingsUseCase(repository)

    @Provides
    fun provideUpdateSettingsUseCase(
        repository: SettingsRepository,
        studentDataRepository: StudentDataRepository,
        notificationScheduler: NotificationScheduler
    ): UpdateSettingsUseCase = UpdateSettingsUseCase(repository, studentDataRepository, notificationScheduler)

    @Provides
    fun provideRescheduleSyncUseCase(
        settingsRepository: SettingsRepository,
        notificationScheduler: NotificationScheduler
    ): RescheduleSyncUseCase = RescheduleSyncUseCase(settingsRepository, notificationScheduler)

    @Provides
    fun provideSyncAgendaToCalendarUseCase(calendarSyncPort: CalendarSyncPort): SyncAgendaToCalendarUseCase =
        SyncAgendaToCalendarUseCase(calendarSyncPort)

    @Provides
    fun provideCalendarAccountsUseCase(calendarSyncPort: CalendarSyncPort): CalendarAccountsUseCase =
        CalendarAccountsUseCase(calendarSyncPort)
}
