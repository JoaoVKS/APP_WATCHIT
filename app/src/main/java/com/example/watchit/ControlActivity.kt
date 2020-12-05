package com.example.watchit

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import java.io.IOException
import java.util.*


class ControlActivity: AppCompatActivity() {


    companion object{
        var address = Intent.getIntentOld("EXTRA_ADDRESS").toString()
        //TALVEZ MUDAR O address para um UUID fixo do codigo de exemplo
        //var tManager: TelephonyManager? = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        //var uuid = tManager!!.deviceId 00001101-0000-1000-8000-00805F9B34FB
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isconnected: Boolean = false
        lateinit var m_address: String

    }
    var out = findViewById<TextView>(R.id.txtRetorno)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)


        if(out != null)
        {
            out.text = "Carregando..."
        }

        m_address = intent.getStringExtra(HomeActivity.EXTRA_ADRESS).toString()

        connectDevice(this).execute()

        out.text = "Conectado! Buscando dados..."

        //aqui diferencia do tutorial, ele envia comandos eu quero receber
        var BPM = getBtData()

        if(!BPM.isNullOrEmpty())
        {
            out.text = "BPM: " + BPM
        }
    }

    private fun getBtData():String
    {
        var retorno = ""
        if(m_bluetoothSocket != null)
        {
            try {
                retorno = m_bluetoothSocket!!.inputStream.read().toString()
                return retorno
            }
            catch (e: IOException)
            {
                e.printStackTrace()
                retorno = "-1"
            }
        }
        else
        {
            retorno = "0"
        }
        return retorno
    }


    private fun disconnect()
    {
        if(m_bluetoothSocket != null)
        {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isconnected = false
            }
            catch (e: IOException)
            {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class connectDevice(c: Context) : AsyncTask<Void, Void, String>()
    {
        private var connectSuccess:Boolean = true
        private val context:Context

        init{
            this.context = c
        }
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "conectando", "aguarde")
        }
        override fun doInBackground(vararg params: Void?): String? {
            try {
                 if(m_bluetoothSocket == null || !m_isconnected)
                 {
                     m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                     val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                     m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                     BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                     m_bluetoothSocket!!.connect()
                 }
            }
            catch (e: IOException)
            {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess)
            {
                //deu errado
            }else
            {
                m_isconnected = true
                m_progress.dismiss()
            }
        }
    }

}