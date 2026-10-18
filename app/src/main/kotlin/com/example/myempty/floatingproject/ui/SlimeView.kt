package com.example.myempty.floatingproject.ui

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.min

enum class SlimeMood { HAPPY, SAD, SLEEPY, NEUTRAL }

class SlimeView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private var mood: SlimeMood = SlimeMood.NEUTRAL
    private var bounce = 0f

    fun setMood(m: SlimeMood) {
        mood = m
        invalidate()
    }

    fun setBounce(b: Float) {
        bounce = b
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)).coerceAtLeast(200)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2 + bounce
        val r = min(w, h) * 0.45f

        // Body gradient
        val gradient = RadialGradient(cx - r*0.2f, cy - r*0.3f, r,
            intArrayOf(Color.parseColor("#4FC3F7"), Color.parseColor("#0288D1")),
            floatArrayOf(0.0f, 1.0f), Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawOval(cx - r, cy - r, cx + r, cy + r, paint)

        // Shine
        paint.shader = null
        paint.color = Color.argb(80, 255, 255, 255)
        canvas.drawOval(cx - r*0.6f, cy - r*0.8f, cx - r*0.2f, cy - r*0.5f, paint)

        // Eyes
        val eyeOffsetX = r * 0.4f
        val eyeOffsetY = r * -0.1f
        val eyeR = r * 0.14f

        when (mood) {
            SlimeMood.HAPPY -> {
                // bigger eyes, light shine
                drawEye(canvas, cx - eyeOffsetX, cy + eyeOffsetY, eyeR * 1.1f)
                drawEye(canvas, cx + eyeOffsetX, cy + eyeOffsetY, eyeR * 1.1f)
            }
            SlimeMood.SAD -> {
                // smaller, drooping eyes
                drawEye(canvas, cx - eyeOffsetX, cy + eyeOffsetY + r*0.06f, eyeR * 0.8f)
                drawEye(canvas, cx + eyeOffsetX, cy + eyeOffsetY + r*0.06f, eyeR * 0.8f)
            }
            SlimeMood.SLEEPY -> {
                // lines for eyes
                eyePaint.style = Paint.Style.STROKE
                eyePaint.strokeWidth = 6f
                canvas.drawLine(cx - eyeOffsetX - eyeR, cy + eyeOffsetY, cx - eyeOffsetX + eyeR, cy + eyeOffsetY, eyePaint)
                canvas.drawLine(cx + eyeOffsetX - eyeR, cy + eyeOffsetY, cx + eyeOffsetX + eyeR, cy + eyeOffsetY, eyePaint)
                eyePaint.style = Paint.Style.FILL
            }
            SlimeMood.NEUTRAL -> {
                drawEye(canvas, cx - eyeOffsetX, cy + eyeOffsetY, eyeR)
                drawEye(canvas, cx + eyeOffsetX, cy + eyeOffsetY, eyeR)
            }
        }

        // Smile (vary by mood)
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(3f, r * 0.06f)
        val smileRect = RectF(cx - r*0.4f, cy + r*0.05f, cx + r*0.4f, cy + r*0.45f)
        when (mood) {
            SlimeMood.SAD -> canvas.drawArc(smileRect, 20f, 140f, false, paint) // frown-ish arc (but simple)
            SlimeMood.SLEEPY -> canvas.drawArc(smileRect, 180f, 180f, false, paint)
            else -> canvas.drawArc(smileRect, 200f, 140f, false, paint)
        }
    }

    private fun drawEye(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        canvas.drawCircle(cx, cy, r, whitePaint)
        canvas.drawCircle(cx, cy, r * 0.5f, eyePaint)
        // small highlight
        canvas.drawCircle(cx - r*0.25f, cy - r*0.25f, r*0.15f, Paint().apply { color = Color.WHITE })
    }
}
