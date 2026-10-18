package com.example.myempty.floatingproject.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.myempty.floatingproject.MainActivity
import com.example.myempty.floatingproject.R
import com.example.myempty.floatingproject.store.SlimeDataStore
import com.example.myempty.floatingproject.ui.ChatBubbleView
import com.example.myempty.floatingproject.ui.SlimeView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

class FloatingService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private var slimeView: SlimeView? = null
    private var params: WindowManager.LayoutParams? = null
    private var chatBubble: ChatBubbleView? = null
    private var apiKey: String? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForegroundServiceWithNotification()

        lifecycleScope.launch {
            apiKey = SlimeDataStore.apiKeyFlow(applicationContext).first()
        }

        setupSlimeOverlay()
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "slime_channel"
        val notif = buildNotification(channelId)
        startForeground(1, notif)
    }

    private fun buildNotification(channelId: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Slime is floating 💧")
            .setContentText("Tap to open settings")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(openIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel("slime_channel", "Slime", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    private fun setupSlimeOverlay() {
        slimeView = SlimeView(this).apply {
            setOnTouchListener(SlimeTouchListener())
        }
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }
        try {
            windowManager.addView(slimeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            // if overlay permission missing, launch settings
            val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
        chatBubble = ChatBubbleView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        slimeView?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        chatBubble?.hide()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    inner class SlimeTouchListener : View.OnTouchListener {
        private var startX = 0
        private var startY = 0
        private var initialX = 0
        private var initialY = 0
        private val gesture = GestureDetector(this@FloatingService, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                // Talk - call AI
                slimeTapTalk()
                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                // Double tap - ask what you see (screenshot if available)
                slimeAskWhatYouSee()
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                // Feed menu - simple demo: increase hunger
                slimeFeed()
            }
        })

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            gesture.onTouchEvent(event)
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX.toInt()
                    startY = event.rawY.toInt()
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - startX
                    val dy = event.rawY.toInt() - startY
                    params?.x = initialX + dx
                    params?.y = initialY + dy
                    try { windowManager.updateViewLayout(slimeView, params) } catch (_: Exception) {}
                }
                MotionEvent.ACTION_UP -> {
                    // snap to edge if close
                    params?.let {
                        val displayWidth = resources.displayMetrics.widthPixels
                        if (it.x < displayWidth / 2) it.x = 0 else it.x = displayWidth
                        try { windowManager.updateViewLayout(slimeView, it) } catch (_: Exception) {}
                    }
                }
            }
            return true
        }
    }

    private fun slimeTapTalk() {
        // show small 'thinking' bubble and then run OpenRouter if API key present
        chatBubble?.show("Thinking...", params?.x ?: 0, (params?.y ?: 0) - 150)
        val key = apiKey
        if (key.isNullOrEmpty()) {
            chatBubble?.show("No API key. Open app to set it.", params?.x ?: 0, (params?.y ?: 0) - 150)
            return
        }
        // Call OpenRouter in coroutine
        lifecycleScope.launch {
            try {
                val client = com.example.myempty.floatingproject.network.OpenRouterClient(key)
                val sys = "You are Slime, a cute floating buddy on user's phone. Short playful replies (max 2 sentences)."
                val text = client.chat(sys, "Hello slime! Say something playful.")
                chatBubble?.show(text, params?.x ?: 0, (params?.y ?: 0) - 150)
            } catch (e: Exception) {
                e.printStackTrace()
                chatBubble?.show("AI error: ${e.message}", params?.x ?: 0, (params?.y ?: 0) - 150)
            }
        }
    }

    private fun slimeAskWhatYouSee() {
        chatBubble?.show("Looking...", params?.x ?: 0, (params?.y ?: 0) - 150)
        // TODO: If you have MediaProjection result stored, call ScreenshotHelper to capture and send to OpenRouter
        // For now, send a text-only prompt
        val key = apiKey
        if (key.isNullOrEmpty()) {
            chatBubble?.show("No API key", params?.x ?: 0, (params?.y ?: 0) - 150)
            return
        }
        lifecycleScope.launch {
            try {
                val client = com.example.myempty.floatingproject.network.OpenRouterClient(key)
                val sys = "You are Slime, a cute floating buddy. Short description of the screen when user asks to describe the screen."
                val resp = client.chat(sys, "Describe what you think is on my screen (text-only).")
                chatBubble?.show(resp, params?.x ?: 0, (params?.y ?: 0) - 150)
            } catch (e: Exception) {
                e.printStackTrace()
                chatBubble?.show("AI error: ${e.message}", params?.x ?: 0, (params?.y ?: 0) - 150)
            }
        }
    }

    private fun slimeFeed() {
        chatBubble?.show("Nom! +20 hunger", params?.x ?: 0, (params?.y ?: 0) - 150)
        // TODO: update SlimeStats via SlimeDataStore
    }
}
