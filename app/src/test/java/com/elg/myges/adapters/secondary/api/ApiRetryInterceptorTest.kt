package com.elg.myges.adapters.secondary.api

import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiRetryInterceptorTest {
    @Test
    fun retriesGetWhenRateLimitedThenReturnsSuccess() {
        val delays = mutableListOf<Long>()
        val interceptor = ApiRetryInterceptor(sleeper = delays::add)
        val chain = retryChain(
            request = getRequest(),
            responses = listOf(response(429), response(200))
        )

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        assertEquals(listOf(1_000L), delays)
    }

    @Test
    fun retriesServiceUnavailableWithExponentialBackoff() {
        val delays = mutableListOf<Long>()
        val interceptor = ApiRetryInterceptor(sleeper = delays::add)
        val chain = retryChain(
            request = getRequest(),
            responses = listOf(response(503), response(503), response(200))
        )

        val result = interceptor.intercept(chain)

        assertEquals(200, result.code)
        assertEquals(listOf(1_000L, 2_000L), delays)
    }

    @Test
    fun honorsRetryAfterHeaderSeconds() {
        val delays = mutableListOf<Long>()
        val interceptor = ApiRetryInterceptor(sleeper = delays::add)
        val chain = retryChain(
            request = getRequest(),
            responses = listOf(response(429, "Retry-After" to "3"), response(200))
        )

        interceptor.intercept(chain)

        assertEquals(listOf(3_000L), delays)
    }

    @Test
    fun doesNotRetryPostRequests() {
        val delays = mutableListOf<Long>()
        val interceptor = ApiRetryInterceptor(sleeper = delays::add)
        val chain = retryChain(
            request = Request.Builder()
                .url("https://api.kordis.fr/me/suggestion")
                .post("{}".toRequestBody(null))
                .build(),
            responses = listOf(response(503), response(200))
        )

        val result = interceptor.intercept(chain)

        assertEquals(503, result.code)
        assertEquals(emptyList<Long>(), delays)
    }
}

private fun getRequest(): Request {
    return Request.Builder()
        .url("https://api.kordis.fr/me/profile")
        .build()
}

private fun response(
    code: Int,
    vararg headers: Pair<String, String>
): Response {
    val builder = Response.Builder()
        .request(getRequest())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("HTTP $code")
        .body("".toResponseBody(null))
    headers.forEach { (name, value) -> builder.header(name, value) }
    return builder.build()
}

private fun retryChain(
    request: Request,
    responses: List<Response>
): Interceptor.Chain {
    val chain = mockk<Interceptor.Chain>()
    var index = 0
    every { chain.request() } returns request
    every { chain.proceed(request) } answers {
        responses[index++].newBuilder()
            .request(request)
            .build()
    }
    return chain
}
