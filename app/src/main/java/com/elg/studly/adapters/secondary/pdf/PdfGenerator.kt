package com.elg.studly.adapters.secondary.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.OutputStream

object PdfGenerator {

    
    fun savePdfToDownloads(context: Context, text: String, title: String, fileName: String): Uri? {
        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "document" }.let {
            if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf"
        }
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return null
                resolver.openOutputStream(uri)?.use { generatePdfFromText(text, title, it) }
                    ?: return null
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                uri
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .apply { mkdirs() }
                val file = File(dir, safeName)
                file.outputStream().use { generatePdfFromText(text, title, it) }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        }.getOrNull()
    }


    fun generatePdfFromText(text: String, title: String, outputStream: OutputStream) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 50f
        val contentWidth = pageWidth - 2 * margin

        val paintText = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val paintTitle = TextPaint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paintText, contentWidth.toInt())
            .build()

        var pageNumber = 1
        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var yPosition = margin
        canvas.drawText(title, margin, yPosition + 18f, paintTitle)
        yPosition += 40f

        val lineHeight = 16f
        for (line in 0 until layout.lineCount) {
            if (yPosition + lineHeight > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageNumber++
                page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                yPosition = margin
            }
            val start = layout.getLineStart(line)
            val end = layout.getLineVisibleEnd(line)
            if (end > start) {
                canvas.drawText(text, start, end, margin, yPosition + 12f, paintText)
            }
            yPosition += lineHeight
        }

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
