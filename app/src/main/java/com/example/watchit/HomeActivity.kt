package com.example.watchit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        val sharedpreferences = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        val usuario_logado: String? = sharedpreferences.getString("usuario_logado", null)
        if(!usuario_logado.isNullOrEmpty())
        {
            //tem usuario
            Toast.makeText(this@HomeActivity, "Olá. $usuario_logado", Toast.LENGTH_SHORT).show()
        }
        else
        {
            //n tem usuario
            val show = Intent(this, MainActivity::class.java)
            startActivity(show)
        }
    }
}