package com.elg.studly.adapters.secondary.api

import okhttp3.Interceptor
import okhttp3.Response
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

class ApiRetryInterceptor(
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
    private val baseDelayMs: Long = DEFAULT_BASE_DELAY_MS,
    private val maxDelayMs: Long = DEFAULT_MAX_DELAY_MS,
    private val sleeper: (Long) -> Unit = Thread::sleep
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        if (request.method != "GET") return response
        var attempt = 0
        while (attempt < maxRetries && response.code.isRetryableStatus()) {
            val delayMs = response.retryDelayMs(attempt)
            response.close()
            sleeper(delayMs)
            response = chain.proceed(request)
            attempt++
        }
        return response
    }

    private fun Response.retryDelayMs(attempt: Int): Long {
        val retryAfter = header("Retry-After")?.toRetryAfterDelayMs()
        if (retryAfter != null) return min(retryAfter, maxDelayMs)
        val exponential = baseDelayMs * (1L shl attempt)
        return min(exponential, maxDelayMs)
    }

    private fun Int.isRetryableStatus(): Boolean {
        return this == HTTP_TOO_MANY_REQUESTS || this == HTTP_SERVICE_UNAVAILABLE
    }

    private fun String.toRetryAfterDelayMs(): Long? {
        trim().toLongOrNull()?.let { return it.coerceAtLeast(0) * 1000 }
        return runCatching {
            val retryAt = ZonedDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
            Duration.between(java.time.Instant.now(), retryAt).toMillis().coerceAtLeast(0)
        }.getOrNull()
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val HTTP_SERVICE_UNAVAILABLE = 503
        const val DEFAULT_MAX_RETRIES = 2
        const val DEFAULT_BASE_DELAY_MS = 1_000L
        const val DEFAULT_MAX_DELAY_MS = 30_000L
    }
}
