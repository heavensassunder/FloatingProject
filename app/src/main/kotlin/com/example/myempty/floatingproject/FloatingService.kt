package com.example.myempty.floatingproject

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.Calendar

class SlimeView(context: Context) : View(context) {
    var squish = 0f
    var bounce = 0f
    var mood = 0
        set(v) { field = v % 5; invalidate() }
    var message: String? = null
    var scale = 1f

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#5DADE2") }
    private val bodyDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2E86C1") }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 150 }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val blushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F1948A"); alpha = 120 }
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; setShadowLayer(10f, 0f, 4f, Color.GRAY) }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 38f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
    private val rect = RectF()

    fun bounceAnim() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 500
            addUpdateListener { bounce = sin(it.animatedFraction * Math.PI).toFloat() * 35f; squish = sin(it.animatedFraction * Math.PI).toFloat() * 0.35f; invalidate() }
            start()
        }
    }
    fun say(text: String) {
        message = text
        invalidate()
        postDelayed({ message = null; invalidate() }, 3000)
    }
    fun startIdle() {
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2000; repeatCount = ValueAnimator.INFINITE
            addUpdateListener { val deg = it.animatedValue as Float; squish = sin(Math.toRadians(deg.toDouble())).toFloat() * 0.05f; invalidate() }
            start()
        }
    }
    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2; val cy = h / 2 + bounce
        canvas.save(); canvas.scale(scale, scale, cx, cy)
        bodyDarkPaint.alpha = 70; rect.set(cx - w*0.4f, cy + h*0.3f, cx + w*0.4f, cy + h*0.45f); canvas.drawOval(rect, bodyDarkPaint); bodyDarkPaint.alpha = 255
        val squishH = 1f - squish; val squishW = 1f + squish
        rect.set(cx - w*0.42f * squishW, cy - h*0.32f * squishH, cx + w*0.42f * squishW, cy + h*0.36f * squishH); canvas.drawOval(rect, bodyPaint)
        rect.set(cx - w*0.28f, cy - h*0.25f, cx - w*0.05f, cy - h*0.05f); canvas.drawOval(rect, highlightPaint)
        val eyeY = cy - h*0.05f; val leftEyeX = cx - w*0.16f; val rightEyeX = cx + w*0.16f
        when(mood) {
            0 -> { canvas.drawCircle(leftEyeX, eyeY, w*0.07f, eyeWhitePaint); canvas.drawCircle(leftEyeX, eyeY, w*0.035f, eyePaint); canvas.drawCircle(rightEyeX, eyeY, w*0.07f, eyeWhitePaint); canvas.drawCircle(rightEyeX, eyeY, w*0.035f, eyePaint); val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = w*0.022f; strokeCap = Paint.Cap.ROUND }; val sr = RectF(cx - w*0.12f, eyeY + h*0.05f, cx + w*0.12f, eyeY + h*0.18f); canvas.drawArc(sr, 20f, 140f, false, sp) }
            1 -> { val lp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = w*0.022f; strokeCap = Paint.Cap.ROUND }; canvas.drawLine(leftEyeX - w*0.06f, eyeY, leftEyeX + w*0.06f, eyeY, lp); canvas.drawLine(rightEyeX - w*0.06f, eyeY, rightEyeX + w*0.06f, eyeY, lp); val zp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = w*0.12f; typeface = Typeface.DEFAULT_BOLD }; canvas.drawText("Z", cx + w*0.25f, eyeY - h*0.15f, zp); canvas.drawText("z", cx + w*0.32f, eyeY - h*0.25f, zp) }
            2 -> { canvas.drawCircle(leftEyeX, eyeY, w*0.07f, eyeWhitePaint); canvas.drawCircle(leftEyeX, eyeY + h*0.02f, w*0.04f, eyePaint); canvas.drawCircle(rightEyeX, eyeY, w*0.07f, eyeWhitePaint); canvas.drawCircle(rightEyeX, eyeY + h*0.02f, w*0.04f, eyePaint); val ap = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = w*0.025f }; canvas.drawLine(leftEyeX - w*0.08f, eyeY - h*0.08f, leftEyeX + w*0.08f, eyeY - h*0.03f, ap); canvas.drawLine(rightEyeX - w*0.08f, eyeY - h*0.03f, rightEyeX + w*0.08f, eyeY - h*0.08f, ap); canvas.drawLine(cx - w*0.1f, eyeY + h*0.15f, cx + w*0.1f, eyeY + h*0.15f, ap) }
            3 -> { eyePaint.color = Color.parseColor("#E74C3C"); canvas.drawCircle(leftEyeX, eyeY, w*0.05f, eyePaint); canvas.drawCircle(rightEyeX, eyeY, w*0.05f, eyePaint); eyePaint.color = Color.BLACK; canvas.drawCircle(cx - w*0.06f, eyeY + h*0.15f, w*0.05f, blushPaint); canvas.drawCircle(cx + w*0.06f, eyeY + h*0.15f, w*0.05f, blushPaint); val hp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = w*0.02f }; val sr = RectF(cx - w*0.08f, eyeY + h*0.06f, cx + w*0.08f, eyeY + h*0.14f); canvas.drawArc(sr, 20f, 140f, false, hp) }
            4 -> { canvas.drawCircle(leftEyeX, eyeY, w*0.07f, eyeWhitePaint); canvas.drawCircle(rightEyeX, eyeY, w*0.07f, eyeWhitePaint); canvas.drawCircle(leftEyeX, eyeY - h*0.02f, w*0.03f, eyePaint); canvas.drawCircle(rightEyeX, eyeY - h*0.02f, w*0.03f, eyePaint); val mp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }; canvas.drawOval(RectF(cx - w*0.12f, eyeY + h*0.05f, cx + w*0.12f, eyeY + h*0.22f), mp); val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F1948A") }; canvas.drawOval(RectF(cx - w*0.05f, eyeY + h*0.12f, cx + w*0.05f, eyeY + h*0.20f), tp) }
        }
        if (mood != 1) { canvas.drawCircle(cx - w*0.22f, eyeY + h*0.12f, w*0.05f, blushPaint); canvas.drawCircle(cx + w*0.22f, eyeY + h*0.12f, w*0.05f, blushPaint) }
        // speech bubble
        message?.let { msg ->
            val bw = textPaint.measureText(msg) + 50; val bh = 70f
            val bx = cx; val by = cy - h*0.65f
            rect.set(bx - bw/2, by - bh/2, bx + bw/2, by + bh/2); canvas.drawRoundRect(rect, 25f, 25f, bubblePaint)
            // tail
            val path = Path(); path.moveTo(bx, by + bh/2); path.lineTo(bx - 15, by + bh/2 + 20); path.lineTo(bx + 15, by + bh/2); path.close(); canvas.drawPath(path, bubblePaint)
            canvas.drawText(msg, bx, by + 12, textPaint)
        }
        canvas.restore()
    }
    init { startIdle(); setLayerType(LAYER_TYPE_SOFTWARE, null) }
}

class FloatingService : Service(), SensorEventListener {
    private var windowManager: WindowManager? = null
    private var floatingView: SlimeView? = null
    private var params: WindowManager.LayoutParams? = null
    private var initialX = 0; private var initialY = 0; private var initialTouchX = 0f; private var initialTouchY = 0f
    private var moved = false; private var lastTapTime = 0L; private var lastInteraction = System.currentTimeMillis()
    private var sensorManager: SensorManager? = null
    private var lastShake = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val idleRunnable = object : Runnable { override fun run() { val idleFor = System.currentTimeMillis() - lastInteraction; if (idleFor > 120000) { floatingView?.say(listOf("Hey! 😢", "Play with me!", "I'm lonely...", "Tap me!").random()); floatingView?.mood = 0; floatingView?.bounceAnim() }; handler.postDelayed(this, 120000) } }
    private val timeRunnable = object : Runnable { override fun run() { val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); if (hour in 22..23 || hour in 0..5) { if (floatingView?.mood != 1) { floatingView?.mood = 1; floatingView?.say("Go sleep! 🌙") } }; handler.postDelayed(this, 60000) } }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val pct = level * 100 / scale
                    if (pct < 15) { floatingView?.mood = 1; floatingView?.say("I'm tired... 🔋 $pct%") }
                }
                Intent.ACTION_POWER_CONNECTED -> { floatingView?.mood = 3; floatingView?.say("Yummy energy! ⚡"); floatingView?.bounceAnim(); floatingView?.scale = 1.2f; floatingView?.postDelayed({ floatingView?.scale = 1f }, 800) }
                Intent.ACTION_POWER_DISCONNECTED -> { floatingView?.say("Nooo energy! 😭"); floatingView?.mood = 2 }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("floating_pet_channel", "Smart Slime", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
            val n = Notification.Builder(this, "floating_pet_channel").setContentTitle("Smart Slime is alive 🧠💧").setContentText("He feels battery, time, and shakes!").setSmallIcon(android.R.drawable.star_big_on).build()
            startForeground(1, n)
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = SlimeView(this)
        params = WindowManager.LayoutParams(350, 500, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 200; y = 400 }

        // sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager?.registerListener(this, sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)

        // receivers
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

        handler.post(idleRunnable); handler.post(timeRunnable)

        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { initialX = params!!.x; initialY = params!!.y; initialTouchX = event.rawX; initialTouchY = event.rawY; moved = false; lastInteraction = System.currentTimeMillis(); true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt(); val dy = (event.rawY - initialTouchY).toInt()
                    if (dx*dx + dy*dy > 100) moved = true
                    if (moved) { params!!.x = initialX + dx; params!!.y = initialY + dy; windowManager?.updateViewLayout(floatingView, params) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 300) {
                            floatingView?.postDelayed({ try { windowManager?.removeView(floatingView); } catch(e:Exception){}; stopSelf() }, 400); floatingView?.bounceAnim()
                        } else {
                            floatingView?.mood = floatingView!!.mood + 1
                            floatingView?.bounceAnim()
                            val msgs = when(floatingView?.mood) { 0->listOf("Hehe! 😊","Hi!"); 1->listOf("Zzz... 😴","Sleepy..."); 2->listOf("Grrr! 😠","Don't tap!"); 3->listOf("Love you! 🥰","<3"); 4->listOf("Nom nom! 😋","Yummy!"); else->listOf("Hi!") }
                            floatingView?.say(msgs.random())
                            if (floatingView?.mood == 4) { floatingView?.scale = 1.25f; floatingView?.postDelayed({ floatingView?.scale = 1f; floatingView?.mood = 0 }, 1200) }
                        }
                        lastTapTime = now
                    } else { floatingView?.bounceAnim() }
                    // Long press detection is handled via postDelayed - if still down for 600ms, feed
                    true
                }
                else -> false
            }
        }
        // long press to feed - extra listener
        floatingView?.setOnLongClickListener {
            floatingView?.mood = 4; floatingView?.say("THANK YOU! 🍔"); floatingView?.bounceAnim()
            floatingView?.scale = 1.35f; floatingView?.postDelayed({ floatingView?.scale = 1f }, 600)
            lastInteraction = System.currentTimeMillis(); true
        }

        windowManager?.addView(floatingView, params)
        floatingView?.postDelayed({ floatingView?.say("I'm alive! 🧠") }, 1000)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
            val gForce = sqrt(x*x + y*y + z*z) / SensorManager.GRAVITY_EARTH
            if (gForce > 2.5f) {
                val now = System.currentTimeMillis()
                if (now - lastShake > 1500) {
                    lastShake = now; floatingView?.mood = 2; floatingView?.say(listOf("Stop shaking me!! 🤢","I'm dizzy! 😵‍💫","Noooo!").random()); floatingView?.bounceAnim()
                    lastInteraction = now
                }
            }
        }
    }
    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(batteryReceiver) } catch(e:Exception){}
        sensorManager?.unregisterListener(this)
        handler.removeCallbacks(idleRunnable); handler.removeCallbacks(timeRunnable)
        try { floatingView?.let { windowManager?.removeView(it) } } catch(e:Exception){}
    }
}