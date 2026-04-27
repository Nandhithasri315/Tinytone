package com.example.tinytone

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val amplitudes = mutableListOf<Float>()
    private val maxSpikes = 40

    fun addAmplitude(amp: Float) {
        amplitudes.add(amp.coerceIn(0f, 2000f))
        if (amplitudes.size > maxSpikes) {
            amplitudes.removeAt(0)
        }
        postInvalidate()
    }

    fun clear() {
        amplitudes.clear()
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (amplitudes.isEmpty()) {
            drawIdleWave(canvas)
            return
        }

        val midY = height / 2f
        val spacing = width / maxSpikes.toFloat()

        for (i in amplitudes.indices) {
            val x = i * spacing + spacing / 2f
            val amp = amplitudes[i]

            paint.color = when {
                amp > 900f -> Color.parseColor("#FF7043")
                amp > 300f -> Color.parseColor("#66BB6A")
                else -> Color.parseColor("#42A5F5")
            }

            // Fixed: Ensure min height doesn't exceed max height to avoid coerceIn crash
            val maxHeight = height / 2f
            val calculatedHeight = (amp * height / 1200f)
            val spikeHeight = if (maxHeight > 12f) {
                calculatedHeight.coerceIn(12f, maxHeight)
            } else {
                maxHeight
            }
            
            canvas.drawLine(x, midY - spikeHeight, x, midY + spikeHeight, paint)
        }
    }

    private fun drawIdleWave(canvas: Canvas) {
        val midY = height / 2f
        val spacing = width / maxSpikes.toFloat()
        paint.color = Color.parseColor("#BBDEFB")
        paint.strokeWidth = 10f

        for (i in 0 until maxSpikes) {
            val x = i * spacing + spacing / 2f
            val spikeHeight = if (i % 2 == 0) 16f else 26f
            val safeSpike = if (spikeHeight > midY) midY else spikeHeight
            canvas.drawLine(x, midY - safeSpike, x, midY + safeSpike, paint)
        }

        paint.strokeWidth = 14f
    }
}
