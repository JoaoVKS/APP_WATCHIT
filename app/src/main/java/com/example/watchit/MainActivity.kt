package com.example.watchit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

const val PREFERENCES_FILE_NAME = "SharedPreferences"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ler se tem usuário logado
        val sharedpreferences = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        val usuario_logado: String? = sharedpreferences.getString("usuario_logado", null)
        if(!usuario_logado.isNullOrEmpty())
        {
            //se tiver usuário logado carrega activity da home
            val show = Intent(this, HomeActivity::class.java)
            startActivity(show)
        }

        //FUNCOES DE ACAO-------------------------------------------------------------------------------------------------

        //ao clicar no botão de login, validar as informações e fazer login
        val btn_login = findViewById<Button>(R.id.btnLogin)
        btn_login.setOnClickListener {

            //VALIDACAO DOS CAMPOS DE LOGIN
            var erros = ""
            if(txtEmail.text.isNullOrEmpty())
            {
                if(erros.isNullOrEmpty())
                    erros += "E-mail"
                else
                    erros += "\nE-mail"
            }
            if(txtSenha.text.isNullOrEmpty())
            {
                if(erros.isNullOrEmpty())
                    erros += "Senha"
                else
                    erros += "\nSenha"
            }
            if(!erros.isNullOrEmpty())
                Toast.makeText(this@MainActivity, "Por favor verifique os seguintes campos:\n$erros" , Toast.LENGTH_SHORT).show()
            else
            {
                //sem inputs invalidos, pesquisa na api/json
                var retorno_login = login(txtEmail.text.toString(), txtSenha.text.toString())
                var log = 0
                if (!retorno_login.isNullOrEmpty()) {
                    log = 1
                    editor.putString("usuario_logado", retorno_login)
                    editor.commit()
                    val show = Intent(this, HomeActivity::class.java)
                    startActivity(show)
                }
                if(log == 0)
                {
                    Toast.makeText(this@MainActivity, "E-mail ou senha incorretos!" , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

