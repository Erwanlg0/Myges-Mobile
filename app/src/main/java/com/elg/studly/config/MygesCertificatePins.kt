package com.elg.studly.config

import okhttp3.CertificatePinner

object MygesCertificatePins {
    internal val pins: Map<String, List<String>> = mapOf(
        "api.kordis.fr" to listOf(
            "sha256/RFTU7RvGIxvlUSNQnUjKm3lMhQlPZ/WH1SKP6XnzQaY=",
            "sha256/AlSQhgtJirc8ahLyekmtX+Iw+v46yPYRLJt9Cq1GlB0=",
            "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M="
        ),
        "authentication.kordis.fr" to listOf(
            "sha256/girk71GPKWrpIRRhB/PptBIBKhVsHyHIGvXhcm/Me2Y=",
            "sha256/kZwN96eHtZftBWrOZUsd6cA4es80n3NzSk/XtYz2EqQ=",
            "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M="
        )
    )

    fun certificatePinner(): CertificatePinner {
        val builder = CertificatePinner.Builder()
        pins.forEach { (host, hostPins) ->
            hostPins.forEach { pin -> builder.add(host, pin) }
        }
        return builder.build()
    }
}
