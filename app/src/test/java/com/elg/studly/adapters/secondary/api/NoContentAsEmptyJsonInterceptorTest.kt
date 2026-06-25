package com.elg.studly.adapters.secondary.api

import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test

class NoContentAsEmptyJsonInterceptorTest {
    @Test
    fun getNoContentResponseBecomesEmptyJsonResult() {
        val interceptor = NoContentAsEmptyJsonInterceptor()
        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder()
            .url("https://api.kordis.fr/me/2026/courses")
            .build()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(204)
            .message("No Content")
            .build()

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals("application/json; charset=utf-8", response.body.contentType().toString())
        assertEquals("""{"result":[]}""", response.body.string())
    }

    @Test
    fun postNoContentResponseIsUnchanged() {
        val interceptor = NoContentAsEmptyJsonInterceptor()
        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder()
            .url("https://api.kordis.fr/me/suggestion")
            .post("{}".toRequestBody(null))
            .build()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(204)
            .message("No Content")
            .build()

        val response = interceptor.intercept(chain)

        assertEquals(204, response.code)
    }
}
