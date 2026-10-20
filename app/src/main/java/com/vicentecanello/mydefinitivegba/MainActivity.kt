package com.vicentecanello.mydefinitivegba

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private class Jogo(val nome: String, val chave: String)

    private val urlBase =
        "https://thumbnails.libretro.com/Nintendo%20-%20Game%20Boy%20Advance/Named_Boxarts/"

    private lateinit var vazio: TextView
    private lateinit var adaptador: BaseAdapter
    private val jogos = mutableListOf<Jogo>()
    private val cacheCapas = HashMap<String, Bitmap>()
    private val executor = Executors.newSingleThreadExecutor()
    private var indice: List<String>? = null
    private var jogoAlvo: Jogo? = null

    private val pastaCapas by lazy { File(filesDir, "capas").apply { mkdirs() } }

    private val escolherPasta = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            getSharedPreferences("config", Context.MODE_PRIVATE)
                .edit().putString("pasta", uri.toString()).apply()
            carregarJogos(uri)
        }
    }

    private val escolherImagem = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val alvo = jogoAlvo
        if (uri != null && alvo != null) {
            Thread {
                try {
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null && salvarCapa(alvo.chave, bytes)) {
                        runOnUiThread {
                            cacheCapas.remove(alvo.chave)
                            adaptador.notifyDataSetChanged()
                        }
                    }
                } catch (e: Exception) {
                    // ignora
                }
            }.start()
        }
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private fun arquivoCapa(chave: String): File = File(pastaCapas, "$chave.png")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#212121")
        window.navigationBarColor = Color.BLACK

        val raiz = FrameLayout(this)
        raiz.addView(
            FundoGba(this),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val coluna = LinearLayout(this)
        coluna.orientation = LinearLayout.VERTICAL

        // barra de cima, só com a engrenagem
        val barra = FrameLayout(this)
        barra.setBackgroundColor(Color.parseColor("#212121"))
        val areaEngrenagem = FrameLayout(this)
        areaEngrenagem.addView(
            IconeEngrenagem(this),
            FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER)
        )
        areaEngrenagem.setOnClickListener {
            Toast.makeText(this, "Configurações: em breve", Toast.LENGTH_SHORT).show()
        }
        barra.addView(
            areaEngrenagem,
            FrameLayout.LayoutParams(dp(56), dp(56), Gravity.END)
        )
        coluna.addView(
            barra,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56))
        )

        adaptador = object : BaseAdapter() {
            override fun getCount(): Int = jogos.size
            override fun getItem(position: Int): Any = jogos[position]
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val linha = (convertView as? LinearLayout) ?: criarLinha()
                val imagem = linha.getChildAt(0) as ImageView
                val texto = linha.getChildAt(1) as TextView
                val jogo = jogos[position]
                texto.text = jogo.nome
                imagem.setImageBitmap(capaBitmap(jogo.chave))
                imagem.setOnClickListener { abrirJogo(jogo) }
                imagem.setOnLongClickListener {
                    menuCapa(jogo)
                    true
                }
                return linha
            }
        }

        val lista = ListView(this)
        lista.adapter = adaptador
        lista.setBackgroundColor(Color.TRANSPARENT)
        lista.divider = ColorDrawable(Color.parseColor("#444444"))
        lista.dividerHeight = 2
        lista.setOnItemClickListener { _, _, posicao, _ ->
            abrirJogo(jogos[posicao])
        }
        coluna.addView(
            lista,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        raiz.addView(
            coluna,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        vazio = TextView(this).apply {
            text = "Toque na pasta para escolher onde estão seus jogos"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#AAAAAA"))
            setPadding(dp(32), 0, dp(32), dp(300))
        }
        raiz.addView(
            vazio,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // botão redondo com a pasta dentro
        val botao = FrameLayout(this)
        botao.elevation = dp(6).toFloat()
        botao.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#80CBC4"))
        }
        botao.addView(
            IconePasta(this),
            FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER)
        )
        botao.setOnClickListener { escolherPasta.launch(null) }
        val parametros = FrameLayout.LayoutParams(
            dp(56), dp(56), Gravity.BOTTOM or Gravity.END
        )
        parametros.setMargins(0, 0, dp(16), dp(16))
        raiz.addView(botao, parametros)

        setContentView(raiz)

        val salva = getSharedPreferences("config", Context.MODE_PRIVATE)
            .getString("pasta", null)
        if (salva != null) {
            try {
                carregarJogos(Uri.parse(salva))
            } catch (e: Exception) {
                // pasta não está mais disponível; o usuário escolhe outra
            }
        }
    }

    private fun criarLinha(): LinearLayout {
        val linha = LinearLayout(this)
        linha.orientation = LinearLayout.HORIZONTAL
        linha.gravity = Gravity.CENTER_VERTICAL
        linha.setPadding(dp(12), dp(8), dp(12), dp(8))

        val imagem = ImageView(this)
        imagem.scaleType = ImageView.ScaleType.FIT_CENTER
        imagem.setBackgroundColor(Color.parseColor("#1E1E1E"))
        linha.addView(imagem, LinearLayout.LayoutParams(dp(72), dp(72)))

        val texto = TextView(this)
        texto.textSize = 18f
        texto.setTextColor(Color.WHITE)
        val parametrosTexto = LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        )
        parametrosTexto.leftMargin = dp(14)
        linha.addView(texto, parametrosTexto)
        return linha
    }

    private fun abrirJogo(jogo: Jogo) {
        Toast.makeText(
            this,
            "${jogo.nome}: o núcleo ainda não foi instalado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun menuCapa(jogo: Jogo) {
        jogoAlvo = jogo
        val opcoes = arrayOf(
            "Escolher foto da galeria",
            "Buscar capa na internet (Google)",
            "Remover capa e buscar de novo"
        )
        AlertDialog.Builder(this)
            .setTitle(jogo.nome)
            .setItems(opcoes) { _, qual ->
                when (qual) {
                    0 -> escolherImagem.launch("image/*")
                    1 -> {
                        try {
                            val endereco = "https://www.google.com/search?tbm=isch&q=" +
                                Uri.encode(jogo.nome + " GBA capa")
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(endereco)))
                            Toast.makeText(
                                this,
                                "Salve a imagem (segure nela > Baixar), volte aqui e use \"Escolher foto da galeria\"",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Não consegui abrir o navegador", Toast.LENGTH_SHORT).show()
                        }
                    }
                    2 -> {
                        arquivoCapa(jogo.chave).delete()
                        cacheCapas.remove(jogo.chave)
                        adaptador.notifyDataSetChanged()
                        buscarCapas()
                    }
                }
            }
            .show()
    }

    private fun carregarJogos(uri: Uri) {
        val pasta = DocumentFile.fromTreeUri(this, uri)
        jogos.clear()
        val arquivos = pasta?.listFiles() ?: emptyArray<DocumentFile>()
        val encontrados = mutableListOf<String>()
        for (arquivo in arquivos) {
            val nome = arquivo.name ?: continue
            if (arquivo.isFile && nome.lowercase().endsWith(".gba")) {
                encontrados.add(nome.substringBeforeLast('.'))
            }
        }
        encontrados.sortBy { nome -> nome.lowercase() }
        for (nome in encontrados) {
            jogos.add(Jogo(nome, nome.replace(Regex("[^A-Za-z0-9._-]"), "_")))
        }
        adaptador.notifyDataSetChanged()
        vazio.visibility = if (jogos.isEmpty()) View.VISIBLE else View.GONE
        buscarCapas()
    }

    // ---------- capas ----------

    private fun capaBitmap(chave: String): Bitmap? {
        val guardada = cacheCapas[chave]
        if (guardada != null) return guardada
        val arquivo = arquivoCapa(chave)
        if (!arquivo.exists()) return null
        val bmp = BitmapFactory.decodeFile(arquivo.absolutePath) ?: return null
        cacheCapas[chave] = bmp
        return bmp
    }

    private fun salvarCapa(chave: String, bytes: ByteArray): Boolean {
        val limites = BitmapFactory.Options()
        limites.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
        var amostra = 1
        while (limites.outWidth / amostra > 600 || limites.outHeight / amostra > 600) {
            amostra *= 2
        }
        val opcoes = BitmapFactory.Options()
        opcoes.inSampleSize = amostra
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opcoes)
            ?: return false
        val maior = maxOf(original.width, original.height)
        val final = if (maior > 300) {
            val fator = 300f / maior
            Bitmap.createScaledBitmap(
                original,
                maxOf(1, (original.width * fator).toInt()),
                maxOf(1, (original.height * fator).toInt()),
                true
            )
        } else {
            original
        }
        FileOutputStream(arquivoCapa(chave)).use { saida ->
            final.compress(Bitmap.CompressFormat.PNG, 100, saida)
        }
        return true
    }

    private fun baixarBytes(endereco: String): ByteArray {
        val conexao = URL(endereco).openConnection() as HttpURLConnection
        conexao.connectTimeout = 15000
        conexao.readTimeout = 20000
        try {
            return conexao.inputStream.use { it.readBytes() }
        } finally {
            conexao.disconnect()
        }
    }

    private fun obterIndice(): List<String> {
        val pronto = indice
        if (pronto != null) return pronto
        val arquivo = File(filesDir, "indice.txt")
        val lista: List<String> = if (arquivo.exists()) {
            arquivo.readLines()
        } else {
            val html = String(baixarBytes(urlBase), Charsets.UTF_8)
            val nomesPng = Regex("href=\"([^\"]+\\.png)\"")
                .findAll(html)
                .map { Uri.decode(it.groupValues[1]) }
                .toList()
            if (nomesPng.isNotEmpty()) arquivo.writeText(nomesPng.joinToString("\n"))
            nomesPng
        }
        if (lista.isNotEmpty()) indice = lista
        return lista
    }

    private fun normalizar(texto: String): String {
        var s = Normalizer.normalize(texto, Normalizer.Form.NFD)
        s = s.replace(Regex("\\p{M}+"), "")
        s = s.replace(Regex("\\(.*?\\)|\\[.*?\\]"), "")
        s = s.lowercase()
        return s.replace(Regex("[^a-z0-9]"), "")
    }

    private fun acharNoIndice(nome: String): String? {
        val alvo = normalizar(nome)
        if (alvo.length < 3) return null
        var melhor: String? = null
        var melhorPontos = Int.MAX_VALUE
        for (arquivo in obterIndice()) {
            val candidato = normalizar(arquivo.removeSuffix(".png"))
            if (!candidato.contains(alvo)) continue
            var pontos = candidato.length - alvo.length
            if (arquivo.contains("(USA")) pontos -= 1
            if (pontos < melhorPontos) {
                melhorPontos = pontos
                melhor = arquivo
            }
        }
        return melhor
    }

    private fun buscarCapas() {
        for (jogo in jogos.toList()) {
            if (arquivoCapa(jogo.chave).exists()) continue
            executor.execute {
                try {
                    if (arquivoCapa(jogo.chave).exists()) return@execute
                    val encontrado = acharNoIndice(jogo.nome) ?: return@execute
                    val bytes = baixarBytes(urlBase + Uri.encode(encontrado))
                    if (salvarCapa(jogo.chave, bytes)) {
                        runOnUiThread {
                            cacheCapas.remove(jogo.chave)
                            adaptador.notifyDataSetChanged()
                        }
                    }
                } catch (e: Exception) {
                    // sem internet ou capa não encontrada: fica sem capa
                }
            }
        }
    }
}
