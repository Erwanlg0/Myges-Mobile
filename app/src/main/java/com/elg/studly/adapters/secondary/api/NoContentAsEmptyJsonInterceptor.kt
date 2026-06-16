package com.elg.studly.adapters.secondary.api

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class NoContentAsEmptyJsonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code != HTTP_NO_CONTENT || chain.request().method != "GET") return response
        return response.newBuilder()
            .code(HTTP_OK)
            .message("OK")
            .body(EMPTY_JSON_BODY.toResponseBody(JSON_MEDIA_TYPE))
            .build()
    }

    private companion object {
        const val HTTP_NO_CONTENT = 204
        const val HTTP_OK = 200
        const val EMPTY_JSON_BODY = """{"result":[]}"""
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
