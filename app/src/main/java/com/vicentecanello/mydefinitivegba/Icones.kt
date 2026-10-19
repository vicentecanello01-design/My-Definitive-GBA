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
        color = Color.argb(18, 255, 255, 255)
        style = Paint.Style.FILL
    }
    private val contorno = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(38, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 3f * densidade
    }
    private val tela = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val detalhe = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(28, 255, 255, 255)
        style = Paint.Style.FILL
    }

    private fun caminhoArredondado(r: RectF, topo: Float, base: Float): android.graphics.Path {
        val caminho = android.graphics.Path()
        caminho.addRoundRect(
            r,
            floatArrayOf(topo, topo, topo, topo, base, base, base, base),
            android.graphics.Path.Direction.CW
        )
        return caminho
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawColor(Color.parseColor("#262626"))

        val bw = w * 0.86f
        val bh = bw / 1.76f
        val cx = w / 2f
        val cy = h * 0.55f
        val l = cx - bw / 2f
        val r = cx + bw / 2f
        val t = cy - bh / 2f
        val b = cy + bh / 2f

        canvas.save()
        canvas.rotate(45f, cx, cy)

        // botões de ombro (L e R)
        canvas.drawRoundRect(
            RectF(l + bw * 0.02f, t - bh * 0.05f, l + bw * 0.16f, t + bh * 0.04f),
            bh * 0.04f, bh * 0.04f, detalhe
        )
        canvas.drawRoundRect(
            RectF(r - bw * 0.16f, t - bh * 0.05f, r - bw * 0.02f, t + bh * 0.04f),
            bh * 0.04f, bh * 0.04f, detalhe
        )

        // corpo do console
        val forma = caminhoArredondado(RectF(l, t, r, b), bh * 0.14f, bh * 0.36f)
        canvas.drawPath(forma, corpo)
        canvas.drawPath(forma, contorno)

        // moldura e tela
        val moldura = RectF(cx - bw * 0.26f, cy - bh * 0.32f, cx + bw * 0.26f, cy + bh * 0.32f)
        canvas.drawRoundRect(moldura, bh * 0.08f, bh * 0.08f, tela)
        canvas.drawRoundRect(moldura, bh * 0.08f, bh * 0.08f, contorno)
        canvas.drawRect(
            RectF(cx - bw * 0.21f, cy - bh * 0.25f, cx + bw * 0.21f, cy + bh * 0.25f),
            tela
        )

        // direcional
        val dx = l + bw * 0.12f
        val dy = cy - bh * 0.04f
        val arm = bh * 0.11f
        val len = bh * 0.17f
        canvas.drawRoundRect(
            RectF(dx - len, dy - arm / 2f, dx + len, dy + arm / 2f),
            arm / 3f, arm / 3f, detalhe
        )
        canvas.drawRoundRect(
            RectF(dx - arm / 2f, dy - len, dx + arm / 2f, dy + len),
            arm / 3f, arm / 3f, detalhe
        )

        // botões A (mais alto) e B (mais baixo)
        canvas.drawCircle(r - bw * 0.075f, cy - bh * 0.12f, bh * 0.095f, detalhe)
        canvas.drawCircle(r - bw * 0.15f, cy + bh * 0.04f, bh * 0.095f, detalhe)

        // Start e Select, embaixo à direita
        val sy = cy + bh * 0.30f
        for (sx in listOf(r - bw * 0.15f, r - bw * 0.09f)) {
            canvas.drawRoundRect(
                RectF(sx - bw * 0.025f, sy - bh * 0.025f, sx + bw * 0.025f, sy + bh * 0.025f),
                bh * 0.025f, bh * 0.025f, detalhe
            )
        }

        canvas.restore()
    }
}
