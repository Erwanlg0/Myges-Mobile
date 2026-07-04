package com.elg.studly.adapters.secondary.api

import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MyGesApiService {
    @GET("me/profile")
    suspend fun profile(): JsonElement

    @GET("me/minimumVersion")
    suspend fun minimumVersion(): JsonElement?

    @GET("me/years")
    suspend fun years(): JsonElement?

    @GET("me/trimesterYears")
    suspend fun trimesterYears(): JsonElement?

    @GET("me/cvec")
    suspend fun cvec(): JsonElement?

    @GET("me/internalrules")
    suspend fun internalRules(): JsonElement?

    @GET("me/suggestion")
    suspend fun suggestions(): JsonElement?

    @POST("me/suggestion")
    suspend fun submitSuggestion(@Body suggestion: JsonElement): JsonElement?

    @GET("me/agenda")
    suspend fun agenda(
        @Query("start") start: Long?,
        @Query("end") end: Long?
    ): JsonElement?

    @GET("me/{year}/courses")
    suspend fun courses(@Path("year") year: String): JsonElement?

    @GET("me/{year}/classes")
    suspend fun classes(@Path("year") year: String): JsonElement?

    @GET("me/{year}/students")
    suspend fun students(@Path("year") year: String): JsonElement?

    @GET("me/{year}/teachers")
    suspend fun teachers(@Path("year") year: String): JsonElement?

    @GET("me/classes/{puid}/students/{year}")
    suspend fun classStudents(
        @Path("puid") puid: String,
        @Path("year") year: String
    ): JsonElement?

    @GET("me/classes/{puid}/students")
    suspend fun classStudents(@Path("puid") puid: String): JsonElement?

    @GET("me/{rcId}/files")
    suspend fun courseFiles(@Path("rcId") rcId: String): JsonElement?

    @GET("me/{rcId}/files/{ocId}")
    suspend fun courseFile(
        @Path("rcId") rcId: String,
        @Path("ocId") ocId: String
    ): JsonElement?

    @GET("me/{rcId}/syllabus")
    suspend fun syllabus(@Path("rcId") rcId: String): JsonElement?

    @GET("me/{year}/grades")
    suspend fun grades(@Path("year") year: String): JsonElement?

    @GET("me/{year}/absences")
    suspend fun absences(@Path("year") year: String): JsonElement?

    @GET("me/{year}/annualDocuments")
    suspend fun annualDocuments(@Path("year") year: String): JsonElement?

    @GET("me/annualDocuments/{id}")
    suspend fun annualDocument(@Path("id") id: String): JsonElement?

    @GET("me/{year}/projects")
    suspend fun projects(@Path("year") year: String): JsonElement?

    @GET("me/projects/{projectId}")
    suspend fun project(@Path("projectId") projectId: String): JsonElement?

    @GET("me/nextProjectSteps")
    suspend fun nextProjectSteps(): JsonElement?

    @GET("me/projectFiles/{pfId}")
    suspend fun projectFile(@Path("pfId") pfId: String): JsonElement?

    @GET("me/projectStepFiles/{psfId}")
    suspend fun projectStepFile(@Path("psfId") psfId: String): JsonElement?

    @GET("me/courses/{rcId}/projects")
    suspend fun courseProjects(@Path("rcId") rcId: String): JsonElement?

    @GET("me/courses/{rcId}/projects/{projectId}/groups/{projectGroupId}")
    suspend fun projectGroup(
        @Path("rcId") rcId: String,
        @Path("projectId") projectId: String,
        @Path("projectGroupId") projectGroupId: String
    ): JsonElement?

    @GET("me/{year}/practicals")
    suspend fun practicals(@Path("year") year: String): JsonElement?

    @GET("me/courses/{rcId}/practicals")
    suspend fun coursePracticals(@Path("rcId") rcId: String): JsonElement?

    
    @POST("me/courses/{rcId}/projects/{projectId}/groups/{groupId}")
    suspend fun joinGroup(
        @Path("rcId") rcId: String,
        @Path("projectId") projectId: String,
        @Path("groupId") groupId: String
    ): Response<ResponseBody>

    @DELETE("me/courses/{rcId}/projects/{projectId}/groups/{groupId}")
    suspend fun leaveGroup(
        @Path("rcId") rcId: String,
        @Path("projectId") projectId: String,
        @Path("groupId") groupId: String
    ): Response<ResponseBody>

    @GET("me/projectGroups/{projectGroupId}/messages")
    suspend fun projectGroupMessages(@Path("projectGroupId") projectGroupId: String): JsonElement?

    @POST("me/projectGroups/{projectGroupId}/messages")
    suspend fun sendProjectGroupMessage(
        @Path("projectGroupId") projectGroupId: String,
        @Body message: JsonElement
    ): Response<ResponseBody>

    @GET("me/events")
    suspend fun events(): JsonElement?

    @POST("me/events/{eventId}/subscribe")
    suspend fun subscribeEvent(@Path("eventId") eventId: String): Response<ResponseBody>

    @DELETE("me/events/{eventId}/unsubscribe")
    suspend fun unsubscribeEvent(@Path("eventId") eventId: String): Response<ResponseBody>

    @GET("me/news")
    suspend fun news(): JsonElement?

    @GET("me/news/banners")
    suspend fun newsBanners(): JsonElement?

    @GET("me/partners")
    suspend fun partners(): JsonElement?

    @GET("me/notificationsDelays")
    suspend fun notificationDelays(): JsonElement?

    @GET("me/notificationsDelays/{notificationTypeId}")
    suspend fun notificationDelay(@Path("notificationTypeId") notificationTypeId: String): JsonElement?

    @GET("me/speedMeetingAppointments")
    suspend fun speedMeetingAppointments(): JsonElement?

    @GET
    @Streaming
    @Headers("Accept: */*")
    suspend fun download(@Url url: String): Response<ResponseBody>
}
