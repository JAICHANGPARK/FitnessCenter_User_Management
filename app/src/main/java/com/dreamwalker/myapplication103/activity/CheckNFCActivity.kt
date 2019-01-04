package com.dreamwalker.myapplication103.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import org.jetbrains.anko.toast

class CheckNFCActivity : AppCompatActivity() {

    val broadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                "android.nfc.action.TECH_DISCOVERED" -> {
                    toast("tag인식 ")
                }
            }
        }

    }

    private fun settingIntentFilter() : IntentFilter{
        var intentFilter = IntentFilter()
        intentFilter.addAction("android.nfc.action.TECH_DISCOVERED")
        return intentFilter
    }


    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_nfc)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC NOT supported on this devices!", Toast.LENGTH_LONG).show()
            finish()
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC NOT Enabled!", Toast.LENGTH_LONG).show()
            finish()
        }


    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, settingIntentFilter())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
