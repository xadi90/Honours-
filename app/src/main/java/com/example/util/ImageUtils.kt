package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUtils {

    fun createTempCameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "question_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun decodeSampledBitmap(context: Context, uri: Uri, maxDimension: Int = 1600): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }
            val srcW = opts.outWidth
            val srcH = opts.outHeight
            if (srcW <= 0 || srcH <= 0) return null

            var sampleSize = 1
            while (srcW / sampleSize > maxDimension || srcH / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun rotateBitmap(source: Bitmap, degrees: Float = 90f): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun cropBitmapCenterRatio(source: Bitmap, ratioW: Int, ratioH: Int): Bitmap {
        val targetRatio = ratioW.toFloat() / ratioH.toFloat()
        val srcRatio = source.width.toFloat() / source.height.toFloat()
        val cropW: Int
        val cropH: Int
        if (srcRatio > targetRatio) {
            cropH = source.height
            cropW = (cropH * targetRatio).toInt().coerceAtLeast(1)
        } else {
            cropW = source.width
            cropH = (cropW / targetRatio).toInt().coerceAtLeast(1)
        }
        val x = ((source.width - cropW) / 2).coerceAtLeast(0)
        val y = ((source.height - cropH) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(source, x, y, cropW, cropH)
    }

    fun cropBitmapMargins(source: Bitmap, trimFraction: Float = 0.1f): Bitmap {
        val dx = (source.width * trimFraction).toInt()
        val dy = (source.height * trimFraction).toInt()
        val newW = (source.width - dx * 2).coerceAtLeast(120)
        val newH = (source.height - dy * 2).coerceAtLeast(120)
        val startX = dx.coerceAtMost(source.width - newW).coerceAtLeast(0)
        val startY = dy.coerceAtMost(source.height - newH).coerceAtLeast(0)
        return Bitmap.createBitmap(source, startX, startY, newW, newH)
    }
}
