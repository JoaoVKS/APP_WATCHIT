package com.example.watchit


import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlin.concurrent.thread


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ligar bluetooth
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter.enable()

        setContentView(R.layout.activity_home)
        val sharedpreferences = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        val btnSair = findViewById<Button>(R.id.btnSair)

        var spiBt = findViewById<Spinner>(R.id.spiDevices)
        val idUsuarioLogado: String? = sharedpreferences.getString("usuario_logado", null)
        if(!idUsuarioLogado.isNullOrEmpty())
        {
            //tem usuario
            //pega o nome do usuário pelo json da api usando o id guardado no login
            var usuariologado = getUser(idUsuarioLogado.toInt())
            val s: MutableList<BluetoothDeviceItem> = ArrayList()
            if(usuariologado != null)
            {
                Toast.makeText(this@HomeActivity, "Olá, ${usuariologado.first_name}", Toast.LENGTH_SHORT).show()

                //Listar aparelhos do bluetooth
                val pairedDevices = mBluetoothAdapter.bondedDevices

                val Stext: MutableList<String> = ArrayList()

                for (bt in pairedDevices)
                {
                    s.add(BluetoothDeviceItem(bt.name, bt.address));
                    Stext.add(bt.name)
                }

                //Smartwatch de testes
                s.add(BluetoothDeviceItem("SmartWatch_WatchIt", "0"))
                Stext.add("SmartWatch_WatchIt")

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Stext)

                spiBt.adapter = adapter

                val connDevice: String? = sharedpreferences.getString("device", null)
                if(!connDevice.isNullOrEmpty())
                {
                    //ja tem um aparelho
                    spiBt.setSelection(adapter.getPosition(connDevice))
                }
            }

            //FUNCOES DE ACAO-------------------------------------------------------------------------------------------------

            //ao clicar no botão de sair limpar usuario e carregar outra activity
            btnSair.setOnClickListener {
                editor.remove("usuario_logado")
                editor.commit()
                val show = Intent(this, MainActivity::class.java)
                startActivity(show)
            }

            //ao selecionar o aparelho, tenta conectar para buscar os sensores
            spiBt.onItemSelectedListener = object :
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
                            Toast.makeText(this@HomeActivity, "Pareado com ${selecionado.nome}", Toast.LENGTH_SHORT).show()
                            var bpmFake = rand(60, 80)

                            //Cria thread que vai rodar a cada segundo para verificar o que deve ser feito em relação a medição
                            var lit = findViewById<TextView>(R.id.litBPM)
                            var swt = findViewById<Switch>(R.id.swtSend)
                            var thread: Thread = object : Thread() {
                                override fun run() {
                                    try {
                                        while (!this.isInterrupted) {
                                            sleep(10000)
                                            runOnUiThread(Runnable {
                                                //código da theread

                                                //pega o bpm falso
                                                bpmFake = rand(60, 80)

                                                //envia para a tela
                                                lit.text = "$bpmFake BPM"

                                                if(swt.isChecked)
                                                {
                                                    //faz o post com os dados se o usuário quiser
                                                    sendJsonData("https://webhook.site/56705da7-f8a1-489b-adda-5a3a098c5ba7", bpmFake.toString(), usuariologado!!.id, 1)
                                                }
                                            })
                                        }
                                    } catch (e: InterruptedException) {
                                    }
                                }
                            }
                            thread.start()
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

