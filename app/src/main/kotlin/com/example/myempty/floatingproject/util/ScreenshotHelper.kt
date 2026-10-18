package com.example.myempty.floatingproject.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import java.io.ByteArrayOutputStream

/**
 * Usage:
 * - From an Activity call:
 *     val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
 *     startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_CODE_CAPTURE)
 *
 * - In onActivityResult (resultCode, data):
 *     val helper = ScreenshotHelper(context)
 *     helper.startCapture(resultCode, data) { bitmap -> ... }
 *
 * This helper is a simple capture helper — you must hold the data Intent and pass it in.
 */
class ScreenshotHelper(private val context: Context) {
    private var projection: MediaProjection? = null
    private var virtualDisplaySurface: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    fun createMediaProjection(resultCode: Int, data: Intent) {
        val mgr = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mgr.getMediaProjection(resultCode, data)
    }

    /**
     * Capture a single screenshot. Call createMediaProjection(...) first from Activity's onActivityResult.
     * The callback runs on the main thread.
     */
    @SuppressLint("WrongConstant")
    fun takeScreenshot(callback: (Bitmap?) -> Unit) {
        val proj = projection
        if (proj == null) {
            callback(null)
            return
        }

        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        handlerThread = HandlerThread("screencap").also { it.start() }
        handler = Handler(handlerThread!!.looper)

        virtualDisplaySurface = ImageReader.newInstance(width, height, 0x1, 2)
        val imageReader = virtualDisplaySurface!!
        proj.createVirtualDisplay(
            "slime-screencap",
            width,
            height,
            density,
            /*flags*/ 0,
            imageReader.surface,
            null,
            handler
        )

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                val plane = image.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * width
                val bmp = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                bmp.copyPixelsFromBuffer(buffer)
                image.close()
                // crop extra padding
                val cropped = Bitmap.createBitmap(bmp, 0, 0, width, height)

                // cleanup
                reader.close()
                projection?.stop()
                handlerThread?.quitSafely()
                callback(cropped)
            } else {
                callback(null)
            }
        }, handler)
    }

    fun bitmapToJpegBytes(bitmap: Bitmap, quality: Int = 70): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        return baos.toByteArray()
    }
}
