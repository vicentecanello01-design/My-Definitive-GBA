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
    private val densidade = context.resources.displayMetrics.density

    private val corpo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(28, 255, 255, 255)
        style = Paint.Style.FILL
    }
    private val contorno = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(60, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 3f * densidade
    }
    private val tela = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val detalhe = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(45, 255, 255, 255)
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawColor(Color.parseColor("#262626"))

        val bw = w * 0.94f
        val bh = bw / 1.76f
        val l = (w - bw) / 2f
        val t = h * 0.52f - bh / 2f
        val r = l + bw
        val b = t + bh
        val cx = w / 2f
        val cy = (t + b) / 2f

        // corpo do console
        val corpoRect = RectF(l, t, r, b)
        canvas.drawRoundRect(corpoRect, bh * 0.2f, bh * 0.2f, corpo)
        canvas.drawRoundRect(corpoRect, bh * 0.2f, bh * 0.2f, contorno)

        // moldura e tela
        val sw = bw * 0.40f
        val sh = sw * 0.68f
        val moldura = RectF(cx - sw * 0.62f, cy - sh * 0.68f, cx + sw * 0.62f, cy + sh * 0.68f)
        canvas.drawRoundRect(moldura, bh * 0.06f, bh * 0.06f, tela)
        canvas.drawRoundRect(moldura, bh * 0.06f, bh * 0.06f, contorno)
        canvas.drawRect(RectF(cx - sw / 2f, cy - sh / 2f, cx + sw / 2f, cy + sh / 2f), tela)

        // direcional
        val dx = l + bw * 0.12f
        val arm = bh * 0.11f
        val len = bh * 0.18f
        canvas.drawRoundRect(RectF(dx - len, cy - arm / 2f, dx + len, cy + arm / 2f), arm / 3f, arm / 3f, detalhe)
        canvas.drawRoundRect(RectF(dx - arm / 2f, cy - len, dx + arm / 2f, cy + len), arm / 3f, arm / 3f, detalhe)

        // botões A e B
        canvas.drawCircle(r - bw * 0.09f, cy - bh * 0.07f, bh * 0.085f, detalhe)
        canvas.drawCircle(r - bw * 0.18f, cy + bh * 0.04f, bh * 0.085f, detalhe)

        // botões pequenos embaixo da tela
        val pw = bw * 0.07f
        val ph = bw * 0.02f
        val py = cy + bw * 0.22f
        canvas.drawRoundRect(RectF(cx - bw * 0.06f - pw / 2f, py, cx - bw * 0.06f + pw / 2f, py + ph), ph, ph, detalhe)
        canvas.drawRoundRect(RectF(cx + bw * 0.06f - pw / 2f, py, cx + bw * 0.06f + pw / 2f, py + ph), ph, ph, detalhe)
    }
}
