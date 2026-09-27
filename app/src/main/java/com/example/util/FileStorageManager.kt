package com.example.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.data.model.QuizFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

object FileStorageManager {

    /**
     * Resolves the display name of a Content Uri.
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                // Ignore fallback to path
            }
        }
        if (name.isNullOrBlank()) {
            name = uri.lastPathSegment?.substringAfterLast('/') ?: "attached_file_${System.currentTimeMillis()}"
        }
        return name ?: "attached_file"
    }

    /**
     * Resolves the file size from a Content Uri.
     */
    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var size: Long = -1L
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && cursor.moveToFirst()) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        return size
    }

    /**
     * Formats bytes into a readable string (e.g. "1.2 MB", "450 KB").
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Unknown size"
        val units = arrayOf("B", "KB", "MB", "GB")
        var digitGroups = 0
        var tempSize = bytes.toDouble()
        while (tempSize >= 1024.0 && digitGroups < units.size - 1) {
            tempSize /= 1024.0
            digitGroups++
        }
        val df = DecimalFormat("#,##0.#")
        return "${df.format(tempSize)} ${units[digitGroups]}"
    }

    /**
     * Copies a selected device file (from Intent Uri) into the app's internal files directory for the quiz.
     */
    fun saveUriToQuizFiles(context: Context, uri: Uri, quizId: Long): QuizFile? {
        return try {
            val fileName = getFileNameFromUri(context, uri)
            val detectedMime = context.contentResolver.getType(uri)
                ?: getMimeTypeFromExtension(fileName)
                ?: "application/octet-stream"

            val rawSize = getFileSizeFromUri(context, uri)

            val quizDir = File(context.filesDir, "quizzes/$quizId").apply { mkdirs() }
            val sanitizedName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(quizDir, "${System.currentTimeMillis()}_$sanitizedName")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            val actualSize = if (rawSize > 0) rawSize else targetFile.length()
            val formattedSize = formatFileSize(actualSize)

            QuizFile(
                quizId = quizId,
                originalName = fileName,
                displayName = fileName,
                mimeType = detectedMime,
                fileSize = formattedSize,
                storagePath = targetFile.absolutePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Ensures that a file exists on disk for this QuizFile. If missing (e.g., pre-seeded sample data),
     * generates a valid readable file on disk.
     */
    fun ensureQuizFileOnDisk(context: Context, quizFile: QuizFile): File {
        if (quizFile.storagePath.isNotBlank()) {
            val file = File(quizFile.storagePath)
            if (file.exists() && file.length() > 0) {
                return file
            }
        }

        // Generate a placeholder file with meaningful academic content
        val quizDir = File(context.filesDir, "quizzes/${quizFile.quizId}").apply { mkdirs() }
        val targetFile = File(quizDir, quizFile.originalName.ifBlank { quizFile.displayName })

        if (quizFile.displayName.endsWith(".pdf", ignoreCase = true) || quizFile.mimeType.contains("pdf", ignoreCase = true)) {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = doc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 18f
                isFakeBoldText = true
            }
            val textPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 12f
            }

            canvas.drawText("Routine Scrapper By Dhrubo - Quiz Study Material", 40f, 60f, titlePaint)
            canvas.drawText("File: ${quizFile.displayName}", 40f, 90f, textPaint)
            canvas.drawText("Topic Reference Document", 40f, 110f, textPaint)
            canvas.drawText("This study material was attached for Quiz preparation.", 40f, 150f, textPaint)
            canvas.drawText("• Review all relevant lecture slides and syllabus notes.", 40f, 175f, textPaint)
            canvas.drawText("• Practice past examination questions and problem sets.", 40f, 195f, textPaint)
            canvas.drawText("• Verify date, time, and room details in the Quiz Zone.", 40f, 215f, textPaint)

            doc.finishPage(page)
            FileOutputStream(targetFile).use { fos ->
                doc.writeTo(fos)
            }
            doc.close()
        } else {
            // Text or other document fallback
            FileOutputStream(targetFile).use { fos ->
                val content = """
                    === Routine Scrapper By Dhrubo: Study Material ===
                    File: ${quizFile.displayName}
                    Type: ${quizFile.mimeType}
                    
                    Instructions for students:
                    1. Study all topics outlined for this quiz.
                    2. Check the scheduled room and time in Quiz Zone.
                    3. Contact your faculty advisor or teacher for queries.
                """.trimIndent()
                fos.write(content.toByteArray(Charsets.UTF_8))
            }
        }

        return targetFile
    }

    /**
     * Downloads a QuizFile into the public Downloads directory of the Android device.
     */
    fun downloadQuizFileToDevice(context: Context, quizFile: QuizFile): Uri? {
        val fileOnDisk = ensureQuizFileOnDisk(context, quizFile)
        return downloadFileToPublicDownloads(
            context = context,
            fileName = quizFile.displayName,
            mimeType = quizFile.mimeType.ifBlank { getMimeTypeFromExtension(quizFile.displayName) ?: "application/octet-stream" },
            sourceFile = fileOnDisk
        )
    }

    /**
     * Downloads a PDF file to the public Downloads folder.
     */
    fun downloadPdfToDownloads(context: Context, sourcePdf: File, targetFileName: String = "Routine_Scrapper_Weekly_Routine.pdf"): Uri? {
        return downloadFileToPublicDownloads(
            context = context,
            fileName = targetFileName,
            mimeType = "application/pdf",
            sourceFile = sourcePdf
        )
    }

    /**
     * Saves a source file directly to the device's public Downloads folder.
     * Works across Android 10+ (via MediaStore) and earlier versions.
     */
    fun downloadFileToPublicDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        sourceFile: File?,
        fallbackContent: ByteArray? = null
    ): Uri? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val collectionUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = context.contentResolver.insert(collectionUri, values) ?: return null

                context.contentResolver.openOutputStream(itemUri)?.use { outStream ->
                    if (sourceFile != null && sourceFile.exists()) {
                        sourceFile.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    } else if (fallbackContent != null) {
                        outStream.write(fallbackContent)
                    }
                }

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(itemUri, values, null, null)

                return itemUri
            } else {
                // Pre-Android 10 fallback
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)

                if (sourceFile != null && sourceFile.exists()) {
                    sourceFile.copyTo(targetFile, overwrite = true)
                } else if (fallbackContent != null) {
                    FileOutputStream(targetFile).use { it.write(fallbackContent) }
                }

                try {
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                    dm?.addCompletedDownload(
                        targetFile.name,
                        "Routine Scrapper Download",
                        true,
                        mimeType,
                        targetFile.absolutePath,
                        targetFile.length(),
                        true
                    )
                } catch (e: Exception) {
                    // Ignore download manager registration failure
                }

                return Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Direct cache/external file fallback if MediaStore fails
            return try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val dest = File(fallbackDir, fileName)
                sourceFile?.copyTo(dest, overwrite = true)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", dest)
            } catch (err: Exception) {
                null
            }
        }
    }

    /**
     * Opens a QuizFile using an Intent chooser and external viewer application.
     */
    fun openQuizFile(context: Context, quizFile: QuizFile): Boolean {
        return try {
            val fileOnDisk = ensureQuizFileOnDisk(context, quizFile)
            val mimeType = quizFile.mimeType.ifBlank {
                getMimeTypeFromExtension(fileOnDisk.name) ?: "*/*"
            }
            openFile(context, fileOnDisk, mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Opens any local file using FileProvider and ACTION_VIEW.
     */
    fun openFile(context: Context, file: File, mimeType: String): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getMimeTypeFromExtension(filename: String): String? {
        val extension = filename.substringAfterLast('.', "")
        if (extension.isBlank()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }
}
