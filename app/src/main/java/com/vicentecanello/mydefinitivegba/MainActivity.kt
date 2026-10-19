package com.vicentecanello.mydefinitivegba

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile

class MainActivity : AppCompatActivity() {

    private lateinit var vazio: TextView
    private lateinit var adaptador: ArrayAdapter<String>
    private val nomes = mutableListOf<String>()

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

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#212121")
        window.navigationBarColor = Color.parseColor("#262626")

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

        adaptador = object : ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, nomes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent) as TextView
                v.setTextColor(Color.WHITE)
                v.textSize = 18f
                v.setPadding(dp(16), dp(18), dp(16), dp(18))
                return v
            }
        }

        val lista = ListView(this)
        lista.adapter = adaptador
        lista.setBackgroundColor(Color.TRANSPARENT)
        lista.divider = ColorDrawable(Color.parseColor("#444444"))
        lista.dividerHeight = 2
        lista.setOnItemClickListener { _, _, posicao, _ ->
            Toast.makeText(
                this,
                "${nomes[posicao]}: o núcleo ainda não foi instalado",
                Toast.LENGTH_SHORT
            ).show()
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

    private fun carregarJogos(uri: Uri) {
        val pasta = DocumentFile.fromTreeUri(this, uri)
        nomes.clear()
        pasta?.listFiles()
            ?.filter { it.isFile && (it.name ?: "").lowercase().endsWith(".gba") }
            ?.sortedBy { (it.name ?: "").lowercase() }
            ?.forEach { nomes.add((it.name ?: "").substringBeforeLast('.')) }
        adaptador.notifyDataSetChanged()
        vazio.visibility = if (nomes.isEmpty()) View.VISIBLE else View.GONE
    }
}
