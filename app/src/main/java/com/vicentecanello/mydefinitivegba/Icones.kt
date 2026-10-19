package com.vicentecanello.mydefinitivegba

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class IconePasta(context: Context) : View(context) {
    private val tinta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val l = w * 0.08f
        val r = w * 0.92f
        val t = h * 0.22f
        val b = h * 0.80f
        // aba da pasta
        canvas.drawRoundRect(
            RectF(l, t, l + (r - l) * 0.42f, t + h * 0.14f),
            w * 0.04f, w * 0.04f, tinta
        )
        // corpo da pasta
        canvas.drawRoundRect(
            RectF(l, t + h * 0.08f, r, b),
            w * 0.06f, w * 0.06f, tinta
        )
    }
}

class IconeEngrenagem(context: Context) : View(context) {
    private val tinta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val cx = w / 2f
        val cy = height / 2f
        tinta.strokeWidth = w * 0.10f
        canvas.drawCircle(cx, cy, w * 0.20f, tinta)
        tinta.strokeWidth = w * 0.13f
        for (i in 0 until 8) {
            canvas.save()
            canvas.rotate(i * 45f, cx, cy)
            canvas.drawLine(cx, cy - w * 0.25f, cx, cy - w * 0.38f, tinta)
            canvas.restore()
        }
    }
}

class FundoGba(context: Context) : View(context) {
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
    }
}
