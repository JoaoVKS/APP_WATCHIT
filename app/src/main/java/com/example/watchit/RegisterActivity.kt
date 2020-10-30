package com.example.watchit

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.txtEmail
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //ler se tem usuário logado
        val sharedpreferences = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        val usuario_logado: String? = sharedpreferences.getString("usuario_logado", null)
        if (!usuario_logado.isNullOrEmpty()) {
            //se tiver usuário logado carrega activity da home
            val show = Intent(this, HomeActivity::class.java)
            startActivity(show)
        }

        val btn_cadastro = findViewById<Button>(R.id.btnCadastro)
        btn_cadastro.setOnClickListener {

            //VALIDACAO DOS CAMPOS DE LOGIN
            var erros = ""
            if (txtName.text.isNullOrEmpty()) {
                if (erros.isNullOrEmpty())
                    erros += "Nome"
                else
                    erros += "\nNome"
            }
            if (txtEmail.text.isNullOrEmpty()) {
                if (erros.isNullOrEmpty())
                    erros += "E-mail"
                else
                    erros += "\nE-mail"
            }
            if (txtBirthbay.text.isNullOrEmpty()) {
                if (erros.isNullOrEmpty())
                    erros += "Aniversário"
                else
                    erros += "\nAniversário"
            }
            if (txtSenha.text.isNullOrEmpty()) {
                if (erros.isNullOrEmpty())
                    erros += "Senha"
                else
                    erros += "\nSenha"
            }
            if (!erros.isNullOrEmpty())
                Toast.makeText(
                    this@RegisterActivity,
                    "Por favor verifique os seguintes campos:\n$erros",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                //sem inputs invalidos, cadastra na api/json
                var novoUsuario: User? = User("", "", "", "", 0, "", "")
                if (novoUsuario != null) {
                    novoUsuario.first_name = txtName.text.split(" ").first()
                    novoUsuario.last_name = txtName.text.split(" ").last()
                    novoUsuario.email = txtEmail.text.toString()
                    novoUsuario.birthday = txtBirthbay.text.toString()
                    novoUsuario.password = txtPassword.text.toString()
                    novoUsuario.aditional_infos = txtAdditional_Infos.text.toString()

                    var retornoCadastro = ""
                    retornoCadastro = cadastro(novoUsuario)
                    var log = retornoCadastro.toIntOrNull()
                    if (retornoCadastro != null && retornoCadastro.toInt() >= 0) {
                        log = 1
                        editor.putString("usuario_logado", retornoCadastro.toString())
                        editor.commit()
                        val show = Intent(this, HomeActivity::class.java)
                        startActivity(show)
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Algo deu errado, tente novamente.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        val btnVoltar = findViewById<Button>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            val show = Intent(this, MainActivity::class.java)
            startActivity(show)
        }
    }
}