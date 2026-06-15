package com.elg.myges.adapters.secondary.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

object PdfGenerator {
    fun generatePdfFromText(text: String, title: String, outputStream: OutputStream) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        val margin = 50f
        val contentWidth = pageWidth - 2 * margin

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintTitle = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val lines = mutableListOf<String>()
        // Simple manual line wrapping
        text.split("\n").forEach { paragraph ->
            if (paragraph.isBlank()) {
                lines.add("")
                return@forEach
            }
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "${currentLine} ${word}"
                val width = paintText.measureText(testLine)
                if (width <= contentWidth) {
                    currentLine.append(if (currentLine.isEmpty()) word else " ${word}")
                } else {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
            }
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Draw title
        var yPosition = margin
        canvas.drawText(title, margin, yPosition + 18f, paintTitle)
        yPosition += 40f

        val lineHeight = 16f
        for (line in lines) {
            if (yPosition + lineHeight > pageHeight - margin) {
                // Finish current page and start a new one
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = margin
            }

            if (line.isNotEmpty()) {
                canvas.drawText(line, margin, yPosition + 12f, paintText)
            }
            yPosition += lineHeight
        }

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
