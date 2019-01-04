package com.dreamwalker.myapplication103.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.activity.appinfo.DeveloperActivity
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        developer_button.setOnClickListener {
            val intent = Intent(this@SettingActivity, DeveloperActivity::class.java)
            startActivity(intent)
        }


    }
}
