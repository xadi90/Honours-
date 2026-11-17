package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.DeveloperConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    private const val PAGE_WIDTH = 595 // A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 44

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun sharePlainText(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "$subject\n\n$body\n\n— ${DeveloperConstants.APP_NAME} (${DeveloperConstants.UNIVERSITY})")
        }
        context.startActivity(Intent.createChooser(intent, "উত্তর শেয়ার করুন"))
    }

    fun sharePdfFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "PDF শেয়ার করুন"))
    }

    suspend fun exportAnswerToPdf(
        context: Context,
        fileNamePrefix: String,
        title: String,
        subtitle: String,
        bodyText: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val safeName = fileNamePrefix.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(36)
            val file = File(exportDir, "${safeName}_${System.currentTimeMillis()}.pdf")
            generatePdfInternal(file, title, subtitle, bodyText)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureMaterialPdfExists(
        context: Context,
        materialId: Long,
        title: String,
        subtitle: String,
        bodyText: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "materials").apply { mkdirs() }
            val file = File(dir, "material_$materialId.pdf")
            if (!file.exists() || file.length() == 0L) {
                generatePdfInternal(file, title, subtitle, bodyText)
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteMaterialPdf(context: Context, materialId: Long) {
        val file = File(File(context.filesDir, "materials"), "material_$materialId.pdf")
        if (file.exists()) file.delete()
    }

    fun calculateOfflineStorageKb(context: Context): Long {
        val dir = File(context.filesDir, "materials")
        if (!dir.exists()) return 0L
        val bytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        return bytes / 1024L
    }

    suspend fun getPdfPageCount(file: File): Int = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext 0
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                renderer.pageCount
            }
        }
    }

    suspend fun renderPdfPage(file: File, pageIndex: Int, targetWidthPx: Int = 1080): Bitmap? =
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext null
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null
                    renderer.openPage(pageIndex).use { page ->
                        val ratio = page.height.toFloat() / page.width.toFloat()
                        val w = targetWidthPx.coerceIn(480, 1800)
                        val h = (w * ratio).toInt().coerceAtLeast(100)
                        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        }

    private fun generatePdfInternal(file: File, title: String, subtitle: String, bodyText: String) {
        val pdfDocument = PdfDocument()
        val contentWidth = PAGE_WIDTH - MARGIN * 2

        val headerBgPaint = Paint().apply { color = Color.rgb(13, 31, 75) }
        val goldLinePaint = Paint().apply {
            color = Color.rgb(201, 162, 39)
            strokeWidth = 2f
        }
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = TextPaint().apply {
            color = Color.rgb(219, 234, 254)
            textSize = 10.5f
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11.5f
            isAntiAlias = true
        }
        val footerPaint = TextPaint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            isAntiAlias = true
        }

        val fullLayout = StaticLayout.Builder.obtain(
            bodyText, 0, bodyText.length, bodyPaint, contentWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1.15f)
            .build()

        var currentLine = 0
        var pageNumber = 1

        while (currentLine < fullLayout.lineCount || pageNumber == 1) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            var yCursor = MARGIN.toFloat()

            if (pageNumber == 1) {
                canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 96f, headerBgPaint)
                canvas.drawLine(0f, 96f, PAGE_WIDTH.toFloat(), 96f, goldLinePaint)

                val titleLayout = StaticLayout.Builder.obtain(
                    title, 0, title.length, titlePaint, contentWidth
                ).build()
                canvas.save()
                canvas.translate(MARGIN.toFloat(), 20f)
                titleLayout.draw(canvas)
                canvas.restore()

                val subLayout = StaticLayout.Builder.obtain(
                    subtitle, 0, subtitle.length, subtitlePaint, contentWidth
                ).build()
                canvas.save()
                canvas.translate(MARGIN.toFloat(), 24f + titleLayout.height + 6f)
                subLayout.draw(canvas)
                canvas.restore()

                yCursor = 118f
            }

            val bottomLimit = PAGE_HEIGHT - MARGIN - 24f
            while (currentLine < fullLayout.lineCount) {
                val lineTop = fullLayout.getLineTop(currentLine)
                val lineBottom = fullLayout.getLineBottom(currentLine)
                val lineHeight = lineBottom - lineTop
                if (yCursor + lineHeight > bottomLimit) {
                    break
                }
                val lineStart = fullLayout.getLineStart(currentLine)
                val lineEnd = fullLayout.getLineEnd(currentLine)
                val lineStr = bodyText.substring(lineStart, lineEnd).trimEnd('\n')
                canvas.drawText(lineStr, MARGIN.toFloat(), yCursor - fullLayout.getLineAscent(currentLine), bodyPaint)
                yCursor += lineHeight
                currentLine++
            }

            canvas.drawText(
                "${DeveloperConstants.APP_NAME} • ${DeveloperConstants.UNIVERSITY} • Page $pageNumber",
                MARGIN.toFloat(),
                (PAGE_HEIGHT - 22).toFloat(),
                footerPaint
            )

            pdfDocument.finishPage(page)
            pageNumber++
            if (currentLine >= fullLayout.lineCount) break
        }

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
    }
}
