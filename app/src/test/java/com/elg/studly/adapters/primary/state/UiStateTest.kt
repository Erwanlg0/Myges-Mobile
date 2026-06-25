package com.elg.studly.adapters.primary.state

import com.elg.studly.R
import com.elg.studly.domain.model.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class UiStateTest {
    @Test
    fun featureUiStateExposesDefaults() {
        val state = FeatureUiState(data = "ready")

        assertEquals("ready", state.data)
        assertEquals(true, state.loading)
        assertEquals(false, state.refreshing)
        assertEquals(null, state.error)
        assertEquals(true, state.online)
    }

    @Test
    fun testAppErrorMessageRes() {
        assertEquals(R.string.error_empty_response, AppError.EmptyResponse.messageRes())
        assertEquals(R.string.error_network, AppError.Network.messageRes())
        assertEquals(R.string.error_offline, AppError.Offline.messageRes())
        assertEquals(R.string.error_permission_denied, AppError.PermissionDenied.messageRes())
        assertEquals(R.string.error_remote, AppError.Remote(500, "Server Error").messageRes())
        assertEquals(R.string.error_storage, AppError.Storage.messageRes())
        assertEquals(R.string.error_document_unavailable, AppError.DocumentUnavailable.messageRes())
        assertEquals(R.string.error_unauthorized, AppError.Unauthorized.messageRes())
        assertEquals(R.string.error_unexpected, AppError.Unexpected(Exception().message).messageRes())
    }
}
