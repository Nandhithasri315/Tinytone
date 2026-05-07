package com.example.tinytone

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.sin

class WaveformView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint().apply {
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val amplitudes = mutableListOf<Float>()
    // Track the rolling max for normalisation so the bar scales correctly
    private var rollingMax = 1f
    private val maxSpikes = 40

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun addAmplitude(amp: Float) {
        val clamped = amp.coerceAtLeast(0f)
        amplitudes.add(clamped)
        if (clamped > rollingMax) rollingMax = clamped
        if (amplitudes.size > maxSpikes) {
            amplitudes.removeAt(0)
            // Recompute rolling max after trim
            rollingMax = amplitudes.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        }
        postInvalidate()
    }

    fun clear() {
        amplitudes.clear()
        rollingMax = 1f
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        if (amplitudes.isEmpty()) {
            drawIdleWave(canvas)
            return
        }

        val midY = height / 2f
        val spacing = width / maxSpikes.toFloat()
        // Maximum visual spike = 45% of half-height so bars don't clip
        val maxVisualHalf = (height / 2f) * 0.90f

        for (i in amplitudes.indices) {
            val x = i * spacing + spacing / 2f
            val normalised = amplitudes[i] / rollingMax   // 0..1

            // Color gradient: quiet = sky-blue, medium = mint, loud = coral
            val color = when {
                normalised > 0.75f -> Color.parseColor("#F15BB5")
                normalised > 0.40f -> Color.parseColor("#00F5D4")
                else               -> Color.parseColor("#00BBF9")
            }
            paint.color = color
            paint.setShadowLayer(8f, 0f, 0f, color)

            val spikeHalf = (normalised * maxVisualHalf).coerceAtLeast(8f)
            canvas.drawLine(x, midY - spikeHalf, x, midY + spikeHalf, paint)
        }
    }

    private fun drawIdleWave(canvas: Canvas) {
        val midY = height / 2f
        val spacing = width / maxSpikes.toFloat()
        paint.color = Color.parseColor("#D0D0E0")
        paint.strokeWidth = 10f
        paint.clearShadowLayer()

        for (i in 0 until maxSpikes) {
            val x = i * spacing + spacing / 2f
            // Gentle sine idle animation
            val spikeHalf = (8f + 10f * abs(sin(i * 0.45)).toFloat()).coerceAtMost(midY - 4f)
            canvas.drawLine(x, midY - spikeHalf, x, midY + spikeHalf, paint)
        }
    }
}
