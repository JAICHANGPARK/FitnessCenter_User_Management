package com.dreamwalker.myapplication103.activity

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.intent.AppConst.NFC_CACHE_PAPER_NAME
import com.dreamwalker.myapplication103.intent.AppConst.NFC_METHOD_INTENT
import io.paperdb.Paper

class CheckNFCActivity : AppCompatActivity() {


    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_nfc)
        Paper.init(this)

        val methodValue = intent.getIntExtra(NFC_METHOD_INTENT, 0x00)
        Paper.book().write(NFC_CACHE_PAPER_NAME, methodValue)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC NOT supported on this devices!", Toast.LENGTH_LONG).show()
            finish()
        } else if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC NOT Enabled!", Toast.LENGTH_LONG).show()
            val ad = AlertDialog.Builder(this)
            ad.setTitle("NFC Connection Error")
            ad.setMessage("설정에서 NFC을 ON 해주세요.")
            ad.setPositiveButton("OK") { dialog, which ->
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                } else {
                    startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
                }
                dialog.dismiss()
                finish()
            }

            ad.show()
        }

    }

}
