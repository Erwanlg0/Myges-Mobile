package com.elg.studly.adapters.primary.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.elg.studly.R
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.ui.theme.MygesTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class DocumentCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun downloadingDocumentShowsProgressAndDisablesOpenAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val document = AcademicDocument(
            id = "document-1",
            title = "Attestation",
            category = "Annuel",
            year = "2026",
            mimeType = "application/pdf",
            fileName = "attestation.pdf",
            downloadUrl = "me/annualDocuments/document-1",
            updatedAt = Instant.parse("2026-06-12T10:00:00Z")
        )

        composeRule.setContent {
            MygesTheme {
                DocumentCard(
                    document = document,
                    downloading = true,
                    progress = 0.42f,
                    onOpen = {}
                )
            }
        }

        composeRule.onNodeWithText("Attestation").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.documents_downloading)).assertIsNotEnabled()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.42f, 0f..1f, 0))).assertIsDisplayed()
    }
}
