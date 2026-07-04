package com.elg.studly.config

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryCookieJarTest {
    @Test
    fun loadForRequestReturnsMatchingCookies() {
        val jar = InMemoryCookieJar()
        val url = "https://api.kordis.fr/me".toHttpUrl()
        val cookie = Cookie.Builder()
            .name("session")
            .value("abc")
            .domain("api.kordis.fr")
            .path("/")
            .build()

        jar.saveFromResponse(url, listOf(cookie))

        assertEquals(listOf(cookie), jar.loadForRequest(url))
    }

    @Test
    fun saveFromResponseReplacesCookieWithSameName() {
        val jar = InMemoryCookieJar()
        val url = "https://api.kordis.fr/me".toHttpUrl()
        val first = Cookie.Builder()
            .name("session")
            .value("abc")
            .domain("api.kordis.fr")
            .path("/")
            .build()
        val second = Cookie.Builder()
            .name("session")
            .value("def")
            .domain("api.kordis.fr")
            .path("/")
            .build()

        jar.saveFromResponse(url, listOf(first))
        jar.saveFromResponse(url, listOf(second))

        assertEquals(listOf(second), jar.loadForRequest(url))
    }

    @Test
    fun loadForRequestDropsExpiredCookies() {
        val jar = InMemoryCookieJar()
        val url = "https://api.kordis.fr/me".toHttpUrl()
        val cookie = Cookie.Builder()
            .name("session")
            .value("abc")
            .domain("api.kordis.fr")
            .path("/")
            .expiresAt(1L)
            .build()

        jar.saveFromResponse(url, listOf(cookie))

        assertTrue(jar.loadForRequest(url).isEmpty())
    }

    @Test
    fun loadForRequestIgnoresOtherHosts() {
        val jar = InMemoryCookieJar()
        val url = "https://api.kordis.fr/me".toHttpUrl()
        val cookie = Cookie.Builder()
            .name("session")
            .value("abc")
            .domain("api.kordis.fr")
            .path("/")
            .build()

        jar.saveFromResponse(url, listOf(cookie))

        assertTrue(jar.loadForRequest("https://authentication.kordis.fr/oauth".toHttpUrl()).isEmpty())
    }
}
