package com.example.myempty.floatingproject.ui

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat

class ChatBubbleView(private val context: Context) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: TextView? = null
    private val handler = Handler(Looper.getMainLooper())

    fun show(text: String, x: Int = 0, y: Int = 0, durationMs: Long = 5000L) {
        hide()
        val tv = TextView(context).apply {
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            setTextColor(Color.BLACK)
            setPadding(20, 12, 20, 12)
            this.text = text
            maxLines = 5
        }
        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = android.graphics.PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x.also { x -> this.x = x }
            y.also { y -> this.y = y }
        }
        view = tv
        try {
            wm.addView(tv, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        handler.postDelayed({ hide() }, durationMs)
    }

    fun hide() {
        view?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
            view = null
        }
    }
}
