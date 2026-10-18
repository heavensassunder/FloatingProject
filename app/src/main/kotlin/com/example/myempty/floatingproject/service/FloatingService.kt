package com.example.myempty.floatingproject.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.myempty.floatingproject.MainActivity
import com.example.myempty.floatingproject.store.SlimeDataStore
import com.example.myempty.floatingproject.ui.ChatBubbleView
import com.example.myempty.floatingproject.ui.SlimeView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FloatingService : LifecycleService() {
    private lateinit var windowManager: WindowManager
    private var slimeView: SlimeView? = null
    private var params: WindowManager.LayoutParams? = null
    private var chatBubble: ChatBubbleView? = null
    private var apiKey: String? = null

    private val TAG = "FloatingService"

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
        // Check overlay permission first - avoid trying to addView if we don't have it
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "No overlay permission - cannot add slime overlay.")
            try {
                Toast.makeText(this, "Please grant \"Display over other apps\" permission for Slime.", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {}
            val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(i)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch overlay settings intent: ${e.message}", e)
            }
            stopSelf()
            return
        }

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
            Log.e(TAG, "Failed to add overlay view: ${e.message}", e)
            try { Toast.makeText(this, "Unable to show overlay: ${e.message}", Toast.LENGTH_LONG).show() } catch (_: Exception) {}
            stopSelf()
            return
        }
        chatBubble = ChatBubbleView(this)
        Log.d(TAG, "Slime overlay added successfully")
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

        private val gesture = GestureDetector(
            this@FloatingService,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    slimeTapTalk()
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    slimeAskWhatYouSee()
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    slimeFeed()
                }
            }
        )

        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            gesture.onTouchEvent(event)
            when (event.action) {
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
                    try {
                        windowManager.updateViewLayout(slimeView, params)
                    } catch (_: Exception) {
                    }
                }
                MotionEvent.ACTION_UP -> {
                    params?.let {
                        val displayWidth = resources.displayMetrics.widthPixels
                        it.x = if (it.x < displayWidth / 2) 0 else displayWidth
                        try {
                            windowManager.updateViewLayout(slimeView, it)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            return true
        }
    }

    private fun slimeTapTalk() {
        chatBubble?.show("Thinking...", params?.x ?: 0, (params?.y ?: 0) - 150)
        val key = apiKey
        if (key.isNullOrEmpty()) {
            chatBubble?.show("No API key. Open app to set it.", params?.x ?: 0, (params?.y ?: 0) - 150)
            return
        }
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
    }
}