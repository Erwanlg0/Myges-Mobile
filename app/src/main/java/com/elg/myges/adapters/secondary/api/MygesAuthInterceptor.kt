package com.elg.myges.adapters.secondary.api

import com.elg.myges.application.ports.SessionRepository
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MygesAuthInterceptor(
    private val userAgent: String,
    private val sessionRepository: SessionRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
            .header("User-Agent", userAgent)
        if (request.header("Accept") == null) {
            requestBuilder.header("Accept", "application/json")
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
    if (trimmed.isBlank() || trimmed.any(Char::isWhitespace)) return trimmed
    return "bearer $trimmed"
}
