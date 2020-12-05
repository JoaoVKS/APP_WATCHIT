package com.example.watchit


import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext


class HomeActivity : AppCompatActivity() {
    private val REQUEST_CODE_ENABLE_BT = 1
    private var job: Job = Job()

    companion object {
        var address = ""
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isconnected: Boolean = false
        lateinit var m_address: String
        val EXTRA_ADRESS: String = "Device_address"
        var m_threads: ArrayList<Thread> = ArrayList()
    }

    private var devicesbt: MutableList<BluetoothDevice> = ArrayList()

    val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //ligar bluetooth
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!mBluetoothAdapter.isEnabled)
            mBluetoothAdapter.enable()

        setContentView(R.layout.activity_home)
        val sharedpreferences = getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()

        val btnSair = findViewById<Button>(R.id.btnSair)
        var lit = findViewById<TextView>(R.id.litBPM)
        var spiBt = findViewById<Spinner>(R.id.spiDevices)
        var swt = findViewById<Switch>(R.id.swtSend)
        var controleMensagem = 0

        val idUsuarioLogado: String? = sharedpreferences.getString("usuario_logado", null)
        if (!idUsuarioLogado.isNullOrEmpty()) {
            //tem usuario
            //pega o nome do usuário pelo json da api usando o id guardado no login
            var usuariologado = getUser(idUsuarioLogado.toInt())
            val s: MutableList<BluetoothDeviceItem> = ArrayList()

            if (usuariologado != null && controleMensagem == 0) {
                Toast.makeText(
                    this@HomeActivity,
                    "Olá, ${usuariologado.first_name}",
                    Toast.LENGTH_SHORT
                ).show()

                //Listar aparelhos do bluetooth
                val pairedDevices = mBluetoothAdapter.bondedDevices

                val Stext: MutableList<String> = ArrayList()

                for (bt in pairedDevices) {
                    s.add(BluetoothDeviceItem(bt.name + " - " + bt.address, bt.address));
                    Stext.add(bt.name)
                    devicesbt.add(bt)
                }

                //Smartwatch de testes
                s.add(BluetoothDeviceItem("SmartWatch_WatchIt", "0"))
                Stext.add("SmartWatch_WatchIt")
                s.add(BluetoothDeviceItem("SmartWatch_WatchIt", ""))

                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Stext)

                spiBt.adapter = adapter

                val connDevice: String? = sharedpreferences.getString("device", null)
                if (!connDevice.isNullOrEmpty()) {
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
                ) {
                    try {
                        for (t in m_threads) {
                            if (!t.isInterrupted) {
                                t.interrupt()
                            }
                        }
                    }
                    catch (e: Exception)
                    {
                        //faz nada
                    }

                    //caso algo seja selecionado
                    var selecionado = s[position]
                    if (selecionado != null) {
                        if (selecionado.endereco == "0") {
                            //é o smartwatch do watchit
                            editor.putString("device", selecionado.nome)
                            editor.commit()
                            Toast.makeText(
                                this@HomeActivity,
                                "Pareado com ${selecionado.nome}",
                                Toast.LENGTH_SHORT
                            ).show()
                            var bpmFake = 0

                            //Cria thread que vai rodar a cada segundo para verificar o que deve ser feito em relação a medição
                            lit.text = "$bpmFake BPM"


                            var thread: Thread = object : Thread() {
                                override fun run() {
                                    try {
                                        while (!this.isInterrupted) {
                                            sleep(5000)
                                            runOnUiThread(Runnable {
                                                //código da Thread

                                                //pega o bpm falso
                                                Fuel.get("https://joaovks.pythonanywhere.com/set")
                                                    .response { result ->
                                                        when (result) {
                                                            is Result.Failure -> {
                                                            }
                                                            is Result.Success -> {
                                                                bpmFake =
                                                                    String(result.get()).toInt()
                                                            }
                                                        }
                                                    }

                                                //envia para a tela
                                                lit.text = "$bpmFake BPM"

                                                if (swt.isChecked) {
                                                    //faz o post com os dados se o usuário quiser
                                                    sendJsonData(
                                                        "https://webhook.site/56705da7-f8a1-489b-adda-5a3a098c5ba7",
                                                        bpmFake.toString(),
                                                        usuariologado!!.id,
                                                        1
                                                    )
                                                }
                                            })
                                        }
                                    } catch (e: InterruptedException) {
                                    }
                                }
                            }
                            thread.start()
                            m_threads.clear()
                            m_threads.add(thread)
                        } else {
                            var tentativas = 0
                            editor.putString("device", selecionado.nome.split("-").first().replace(" ", ""))
                            editor.commit()
                            var thread: Thread = object : Thread() {
                                override fun run() {
                                    try {
                                        var rodando = true
                                        while (rodando) {
                                            runOnUiThread(Runnable {
                                                if (tentativas < 1) {
                                                    var bpm_bt = ""
                                                    val btdevice =
                                                        devicesbt.filter { x -> x.address == selecionado.endereco }.firstOrNull()
                                                    if (btdevice != null) {
                                                        m_address = selecionado.endereco
                                                        if (m_bluetoothSocket == null || !m_isconnected) {
                                                            m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                                                            val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                                                            m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                                                            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                                                            try {
                                                                m_bluetoothSocket!!.connect()
                                                                m_isconnected = true
                                                                Toast.makeText(
                                                                    this@HomeActivity,
                                                                    "Pareado com ${selecionado.nome}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } catch (e: Exception) {
                                                                tentativas++
                                                                m_bluetoothSocket!!.close()
                                                                m_isconnected = false
                                                                Toast.makeText(
                                                                    this@HomeActivity,
                                                                    "Algo deu errado ao conectar com o dispositivo",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                        if (m_isconnected) {
                                                            bpm_bt = getBtData().replace("\n", "")
                                                            lit.text = "$bpm_bt BPM"
                                                            if (swt.isChecked && !bpm_bt.isNullOrEmpty()) {
                                                                //faz o post com os dados se o usuário quiser
                                                                sendJsonData("http://emerghelp1.pythonanywhere.com/insertDadosApp",
                                                                    bpm_bt.toString(),
                                                                    usuariologado!!.id,
                                                                    1
                                                                )
                                                            }

                                                        } else {
                                                            lit.text = "# BPM"
                                                            rodando = false

                                                        }
                                                    } else
                                                        throw (Exception("Não foi possível conectar com o dispositivo"))
                                                } else {
                                                    rodando = false
                                                }
                                            })
                                            sleep(3000)
                                        }
                                    } catch (e: InterruptedException) {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            thread.start()
                            m_threads.clear()
                            m_threads.add(thread)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    //caso nada seja selecionado

                }
            }

            //FUNCOES DE ACAO-------------------------------------------------------------------------------------------------
        } else {
            //n tem usuario
            val show = Intent(this, MainActivity::class.java)
            startActivity(show)
        }
    }

    public fun convertStreamToString(`is`: InputStream): String? {
        val reader = BufferedReader(InputStreamReader(`is`))
        val sb = StringBuilder()
        var line: String? = null
        try {
            while (reader.readLine().also({ line = it }) != null) {
                sb.append(line).append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }

    private fun getBtData(): String {
        var retorno = ""

        var bytes: Int // bytes returned from read()
        var length: Int
        if (m_bluetoothSocket != null) {
            var inputstream = m_bluetoothSocket!!.inputStream
            length = inputstream.available()
            val buffer = ByteArray(length)
            try {


                //retorno = convertStreamToString(inputstream)!!
                //var x: Byte
                //retorno = inputstream.read()
                //val readMessage = String(buffer, 0, inputstream.available())
                //var m_Headder = false
                val byteCount = inputstream!!.available()
                //inputstream.skip(byteCount.toLong())
                if (byteCount > 0) {
                    val rawBytes = ByteArray(byteCount)
                    inputstream.buffered().read(rawBytes)
                    val st = String(rawBytes, StandardCharsets.UTF_8)
                    retorno = st
                    //trata string recebida
                    if (!retorno.isNullOrEmpty()) {
                        var listaBpmTmp: List<String> = retorno.split("\r\n").toList()
                        if ((listaBpmTmp.size - 2) > 0) {
                            retorno = listaBpmTmp[listaBpmTmp.size - 2]
                        }
                    }
                }

                return retorno.replace("\r", "")
            } catch (e: IOException) {
                e.printStackTrace()
                retorno = "-1"
            }
        } else {
            retorno = "0"
        }
        return retorno
    }


    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isconnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

}


