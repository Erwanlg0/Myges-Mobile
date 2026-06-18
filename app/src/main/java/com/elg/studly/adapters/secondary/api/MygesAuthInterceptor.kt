package com.elg.studly.adapters.secondary.api

import com.elg.studly.application.ports.SessionRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MygesAuthInterceptor(
    private val userAgent: String,
    apiBaseUrl: String,
    private val sessionRepository: SessionRepository
) : Interceptor {
    private val apiHost = apiBaseUrl.toHttpUrlOrNull()?.host

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
            .header("User-Agent", userAgent)
        if (request.header("Accept") == null) {
            requestBuilder.header("Accept", "application/json")
        }
        if (request.url.host != apiHost) {
            return chain.proceed(requestBuilder.build())
        }
        val session = sessionRepository.currentSession()
        if (session?.isExpired == true || session?.requiresRefresh == true) {
            sessionRepository.invalidateSession()
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Session refresh required")
                .body("".toResponseBody(null))
                .build()
        }
        session?.accessToken?.let { token ->
            requestBuilder.header("Authorization", token.withBearerScheme())
        }
        return chain.proceed(requestBuilder.build()).also { response ->
            if (response.code == 401) sessionRepository.invalidateSession()
        }
    }
}

internal fun String.withBearerScheme(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return trimmed
    val parts = trimmed.split(Regex("\\s+"), limit = 2)
    if (parts.size == 2 && parts[0].equals("bearer", ignoreCase = true)) {
        return "Bearer ${parts[1]}"
    }
    if (trimmed.any(Char::isWhitespace)) return trimmed
    return "Bearer $trimmed"
}
