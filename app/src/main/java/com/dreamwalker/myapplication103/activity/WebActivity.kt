package com.dreamwalker.myapplication103.activity

import android.app.AlertDialog
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.intent.AppConst.WEB_URL
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity() {

    internal lateinit var alertDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        alertDialog = SpotsDialog.Builder().setContext(this).build()
        alertDialog.show()

        with(web_view) {
            settings.javaScriptEnabled = true
            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    alertDialog.dismiss()
                }
            }
        }

        if (intent != null) {
            if (!intent.getStringExtra(WEB_URL).isEmpty()) {
                web_view.loadUrl(intent.getStringExtra(WEB_URL))
            }
        }
    }
}
