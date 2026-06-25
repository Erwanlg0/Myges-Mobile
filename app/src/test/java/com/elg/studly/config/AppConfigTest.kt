package com.elg.studly.config

import org.junit.Assert.assertEquals
import org.junit.Test

class AppConfigTest {
    @Test
    fun appConfigExposesBuildConfigValues() {
        val config = AppConfig()

        assertEquals("https://api.kordis.fr/", config.apiBaseUrl)
        assertEquals("comreseaugesskolae:/oauth2redirect", config.oauthRedirectUri)
        assertEquals(
            "https://authentication.kordis.fr/oauth/authorize?response_type=token&client_id=skolae-app&redirect_uri=comreseaugesskolae%3A%2Foauth2redirect",
            config.oauthAuthorizeUrl
        )
        assertEquals("MyGES Android", config.userAgent)
    }

    @Test
    fun ensureTrailingSlashKeepsExistingSlash() {
        assertEquals("https://api.kordis.fr/", "https://api.kordis.fr/".ensureTrailingSlash())
    }

    @Test
    fun ensureTrailingSlashAddsMissingSlash() {
        assertEquals("https://api.kordis.fr/", "https://api.kordis.fr".ensureTrailingSlash())
    }

    @Test
    fun withRedirectUriKeepsExistingRedirectUri() {
        val url = "https://authentication.kordis.fr/oauth/authorize?redirect_uri=existing"

        assertEquals(url, url.withRedirectUri("app:/callback"))
    }

    @Test
    fun withRedirectUriAddsMissingRedirectUri() {
        val url = "https://authentication.kordis.fr/oauth/authorize?response_type=token"

        assertEquals(
            "https://authentication.kordis.fr/oauth/authorize?response_type=token&redirect_uri=app%3A%2Fcallback",
            url.withRedirectUri("app:/callback")
        )
    }
}
