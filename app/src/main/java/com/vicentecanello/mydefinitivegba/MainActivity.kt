package com.vicentecanello.mydefinitivegba

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val texto = TextView(this).apply {
            text = "My Definitive GBA"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        setContentView(texto)
    }
}
