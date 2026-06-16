package com.elg.studly.config

import android.net.Uri
import com.elg.studly.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfig @Inject constructor() {
    val apiBaseUrl: String = BuildConfig.MYGES_API_BASE_URL.ensureTrailingSlash()
    val oauthRedirectUri: String = BuildConfig.KORDIS_OAUTH_REDIRECT_URI
    val oauthAuthorizeUrl: String = BuildConfig.KORDIS_OAUTH_AUTHORIZE_URL.withRedirectUri(oauthRedirectUri)
    val userAgent: String = BuildConfig.MYGES_USER_AGENT
}

private fun String.ensureTrailingSlash(): String {
    return if (endsWith('/')) this else "$this/"
}

private fun String.withRedirectUri(redirectUri: String): String {
    val uri = Uri.parse(this)
    if (uri.getQueryParameter("redirect_uri") != null) return this
    return uri.buildUpon()
        .appendQueryParameter("redirect_uri", redirectUri)
        .build()
        .toString()
}
