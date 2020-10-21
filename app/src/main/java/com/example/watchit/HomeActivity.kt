package com.example.watchit

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ligar bluetooth
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter.enable()

        setContentView(R.layout.activity_home)
        val sharedpreferences = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        val btn_sair = findViewById<Button>(R.id.btnSair)



        var spi_bt = findViewById<Spinner>(R.id.spiDevices)
        val usuario_logado: String? = sharedpreferences.getString("usuario_logado", null)
        if(!usuario_logado.isNullOrEmpty())
        {
            //tem usuario

            //pega o nome do usuário pelo json da api usando o id guardado no login
            var usuario_string = getUser(usuario_logado)
            val json = JsonParser().parse(usuario_string)
            val result = json.asJsonObject["nome"].toString().replace("\"","")


            Toast.makeText(this@HomeActivity, "Olá. $result", Toast.LENGTH_SHORT).show()

            //Listar aparelhos do bluetooth
            val pairedDevices = mBluetoothAdapter.bondedDevices
            val s: MutableList<BluetoothDeviceItem> = ArrayList()
            val s_text: MutableList<String> = ArrayList()

            for (bt in pairedDevices)
            {
                s.add(BluetoothDeviceItem(bt.name, bt.address));
                s_text.add(bt.name)
            }

            //Smartwatch de testes
            s.add(BluetoothDeviceItem("SmartWatch_WatchIt", "0"))
            s_text.add("SmartWatch_WatchIt")

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, s_text)

            spi_bt.adapter = adapter



            //FUNCOES DE ACAO-------------------------------------------------------------------------------------------------

            //ao clicar no botão de sair limpar usuario e carregar outra activity
            btn_sair.setOnClickListener {
                editor.remove("usuario_logado")
                editor.commit()
                val show = Intent(this, MainActivity::class.java)
                startActivity(show)
            }

            //ao selecionar o aparelho, tenta conectar para buscar os sensores

            spi_bt.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                )
                {
                    //caso algo seja selecionado
                    var selecionado = s[position]
                    if(selecionado != null)
                    {
                        if(selecionado.endereco == "0")
                        {
                            //é o smartwatch do watchit
                            editor.putString("device", selecionado.nome)
                            editor.commit()
                            Toast.makeText(
                                this@HomeActivity, "Pareado com ${selecionado.nome}", Toast.LENGTH_SHORT).show()
                        }
                        else
                            Toast.makeText(this@HomeActivity, "Não foi possível parear com ${selecionado.nome}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    //caso nada seja selecionado
                }
            }
            //FUNCOES DE ACAO-------------------------------------------------------------------------------------------------
        }
        else
        {
            //n tem usuario
            val show = Intent(this, MainActivity::class.java)
            startActivity(show)
        }
    }
}