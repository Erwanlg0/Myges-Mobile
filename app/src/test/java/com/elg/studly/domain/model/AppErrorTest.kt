package com.elg.studly.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppErrorTest {
    @Test
    fun appExceptionUsesErrorAsMessage() {
        assertEquals(AppError.Network.toString(), AppException(AppError.Network).message)
    }

    @Test
    fun toAppErrorUnwrapsAppException() {
        assertEquals(AppError.Unauthorized, AppException(AppError.Unauthorized).toAppError())
    }

    @Test
    fun toAppErrorWrapsUnexpectedThrowable() {
        assertEquals(AppError.Unexpected("boom"), IllegalStateException("boom").toAppError())
    }

    @Test
    fun remoteErrorExposesCodeAndMessage() {
        val error = AppError.Remote(500, "Server Error")

        assertEquals(500, error.code)
        assertEquals("Server Error", error.message)
    }

    @Test
    fun unexpectedErrorExposesMessage() {
        assertEquals("boom", AppError.Unexpected("boom").message)
    }
}
