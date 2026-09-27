package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ClassRoutine
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    private val DAYS = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday")

    fun generateWeeklyPdf(
        context: Context,
        classes: List<ClassRoutine>,
        department: String = "Department of Computer Science & Engineering",
        section: String = "58_A"
    ): File? {
        val pdfDoc = PdfDocument()
        // A4 page dimensions in points (595 x 842)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 10f
            isAntiAlias = true
        }

        val dayHeaderPaint = Paint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100 background
            style = Paint.Style.FILL
        }

        val tableHeaderTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val rowTextPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8f
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225) // Slate 300
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
        }

        val bgEvenPaint = Paint().apply {
            color = Color.rgb(248, 250, 252) // Slate 50
            style = Paint.Style.FILL
        }

        var currentY = 36f

        // Document Title
        canvas.drawText("Routine Scrapper By Dhrubo", 36f, currentY, titlePaint)
        currentY += 15f
        canvas.drawText("$department • Section: $section • Weekly Class Schedule", 36f, currentY, subPaint)
        currentY += 14f

        // Divider
        canvas.drawLine(36f, currentY, (pageWidth - 36).toFloat(), currentY, borderPaint)
        currentY += 14f

        val leftMargin = 36f
        val rightMargin = (pageWidth - 36).toFloat()
        val totalWidth = rightMargin - leftMargin

        // Column widths
        val colTimeWidth = 90f
        val colCourseWidth = 160f
        val colCodeWidth = 65f
        val colRoomWidth = 60f
        val colTeacherWidth = totalWidth - (colTimeWidth + colCourseWidth + colCodeWidth + colRoomWidth)

        for (day in DAYS) {
            val dayClasses = classes.filter { it.day.equals(day, ignoreCase = true) }

            // Day Header pill
            canvas.drawRect(leftMargin, currentY, rightMargin, currentY + 16f, Paint().apply {
                color = Color.rgb(224, 231, 255) // Indigo 100
                style = Paint.Style.FILL
            })
            canvas.drawText(day.uppercase(), leftMargin + 8f, currentY + 12f, dayHeaderPaint)
            currentY += 18f

            // Table Header
            canvas.drawRect(leftMargin, currentY, rightMargin, currentY + 14f, tableHeaderPaint)
            canvas.drawRect(leftMargin, currentY, rightMargin, currentY + 14f, borderPaint)

            var colX = leftMargin + 4f
            canvas.drawText("Time", colX, currentY + 10f, tableHeaderTextPaint)
            colX += colTimeWidth
            canvas.drawText("Course Title", colX, currentY + 10f, tableHeaderTextPaint)
            colX += colCourseWidth
            canvas.drawText("Code", colX, currentY + 10f, tableHeaderTextPaint)
            colX += colCodeWidth
            canvas.drawText("Room", colX, currentY + 10f, tableHeaderTextPaint)
            colX += colRoomWidth
            canvas.drawText("Faculty", colX, currentY + 10f, tableHeaderTextPaint)
            currentY += 14f

            if (dayClasses.isEmpty()) {
                val rowHeight = 14f
                canvas.drawRect(leftMargin, currentY, rightMargin, currentY + rowHeight, borderPaint)
                canvas.drawText("No classes scheduled (Free Day / Weekend)", leftMargin + 8f, currentY + 10f, rowTextPaint)
                currentY += rowHeight + 8f
            } else {
                dayClasses.forEachIndexed { index, item ->
                    val rowHeight = 15f
                    if (index % 2 == 1) {
                        canvas.drawRect(leftMargin, currentY, rightMargin, currentY + rowHeight, bgEvenPaint)
                    }
                    canvas.drawRect(leftMargin, currentY, rightMargin, currentY + rowHeight, borderPaint)

                    var cx = leftMargin + 4f
                    canvas.drawText("${item.startTime} - ${item.endTime}", cx, currentY + 11f, rowTextPaint)
                    cx += colTimeWidth

                    // Truncate course name if too long
                    val courseText = if (item.courseName.length > 30) item.courseName.take(28) + "..." else item.courseName
                    canvas.drawText(courseText, cx, currentY + 11f, rowTextPaint)
                    cx += colCourseWidth

                    canvas.drawText(item.courseCode, cx, currentY + 11f, rowTextPaint)
                    cx += colCodeWidth

                    canvas.drawText(item.room, cx, currentY + 11f, rowTextPaint)
                    cx += colRoomWidth

                    val teacherText = if (item.teacher.length > 20) item.teacher.take(18) + ".." else item.teacher
                    canvas.drawText(teacherText, cx, currentY + 11f, rowTextPaint)

                    currentY += rowHeight
                }
                currentY += 8f
            }
        }

        // Footer note
        canvas.drawText("Generated by Routine Scrapper By Dhrubo • Official Academic Schedule Export", 36f, (pageHeight - 24).toFloat(), subPaint)

        pdfDoc.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Routine_Scrapper_Weekly_Routine.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()
            file
        } catch (e: Exception) {
            pdfDoc.close()
            null
        }
    }

    fun downloadAndOpenPdf(context: Context, file: File): Boolean {
        return try {
            // Save to device Downloads directory
            FileStorageManager.downloadPdfToDownloads(
                context = context,
                sourcePdf = file,
                targetFileName = "Routine_Scrapper_Weekly_Routine_${System.currentTimeMillis() % 10000}.pdf"
            )
            // Open for immediate viewing
            FileStorageManager.openFile(context, file, "application/pdf")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun shareOrOpenPdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Weekly Class Routine - Routine Scrapper By Dhrubo")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share or Open Weekly Routine PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
