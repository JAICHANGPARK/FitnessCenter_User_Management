package com.dreamwalker.myapplication103.activity.appinfo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.activity.WebActivity
import com.dreamwalker.myapplication103.intent.AppConst.WEB_URL
import kotlinx.android.synthetic.main.activity_developer.*

class DeveloperActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer)

        setSupportActionBar(toolbar)

        GithubButton.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra(WEB_URL, "https://github.com/JAICHANGPARK")
            startActivity(intent)
        }
        qiitaButton.setOnClickListener {

            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra(WEB_URL, "https://qiita.com/Dreamwalker")
            startActivity(intent)
        }
    }
}
