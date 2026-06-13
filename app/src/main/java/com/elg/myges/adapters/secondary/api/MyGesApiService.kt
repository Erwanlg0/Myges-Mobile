package com.elg.myges.adapters.secondary.api

import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.http.GET
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

    @GET("me/agenda")
    suspend fun agenda(
        @Query("start") start: Long?,
        @Query("end") end: Long?
    ): JsonElement?

    @GET("me/{year}/courses")
    suspend fun courses(@Path("year") year: String): JsonElement?

    @GET("me/{rcId}/files")
    suspend fun courseFiles(@Path("rcId") rcId: String): JsonElement?

    @GET("me/{rcId}/syllabus")
    suspend fun syllabus(@Path("rcId") rcId: String): JsonElement?

    @GET("me/{year}/grades")
    suspend fun grades(@Path("year") year: String): JsonElement?

    @GET("me/{year}/absences")
    suspend fun absences(@Path("year") year: String): JsonElement?

    @GET("me/{year}/annualDocuments")
    suspend fun annualDocuments(@Path("year") year: String): JsonElement?

    @GET("me/{year}/projects")
    suspend fun projects(@Path("year") year: String): JsonElement?

    @GET("me/nextProjectSteps")
    suspend fun nextProjectSteps(): JsonElement?

    @GET("me/{year}/practicals")
    suspend fun practicals(@Path("year") year: String): JsonElement?

    @GET("me/news")
    suspend fun news(): JsonElement?

    @GET("me/news/banners")
    suspend fun newsBanners(): JsonElement?

    @GET("me/partners")
    suspend fun partners(): JsonElement?

    @GET("me/speedMeetingAppointments")
    suspend fun speedMeetingAppointments(): JsonElement?

    @GET
    @Streaming
    suspend fun download(@Url url: String): ResponseBody
}
