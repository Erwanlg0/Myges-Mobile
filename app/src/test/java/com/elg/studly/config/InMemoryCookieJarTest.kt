package com.elg.studly.config

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import kotlin.concurrent.thread

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
    fun sameNameCookiesRemainDistinctByDomainAndPath() {
        val jar = InMemoryCookieJar()
        val url = "https://api.kordis.fr/me/profile".toHttpUrl()
        val root = Cookie.Builder()
            .name("session")
            .value("root")
            .domain("kordis.fr")
            .path("/")
            .build()
        val api = Cookie.Builder()
            .name("session")
            .value("api")
            .domain("api.kordis.fr")
            .path("/")
            .build()
        val profile = Cookie.Builder()
            .name("session")
            .value("profile")
            .domain("api.kordis.fr")
            .path("/me")
            .build()

        jar.saveFromResponse(url, listOf(root, api, profile))

        assertEquals(setOf(root, api, profile), jar.loadForRequest(url).toSet())
    }

    @Test
    fun concurrentReadsAndWritesDoNotCorruptCookieStore() {
        val jar = InMemoryCookieJar()
        val url = "https://api.kordis.fr/me".toHttpUrl()
        val failures = Collections.synchronizedList(mutableListOf<Throwable>())
        val threads = (0 until 8).map { index ->
            thread {
                runCatching {
                    repeat(500) { iteration ->
                        jar.saveFromResponse(
                            url,
                            listOf(
                                Cookie.Builder()
                                    .name("session-$index")
                                    .value(iteration.toString())
                                    .domain("api.kordis.fr")
                                    .path("/")
                                    .build()
                            )
                        )
                        jar.loadForRequest(url)
                    }
                }.onFailure { failures += it }
            }
        }

        threads.forEach { it.join() }

        assertTrue(failures.isEmpty())
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
