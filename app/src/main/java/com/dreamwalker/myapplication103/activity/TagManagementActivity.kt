package com.dreamwalker.myapplication103.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.intent.AppConst
import kotlinx.android.synthetic.main.activity_tag_management.*

class TagManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_management)

        device_button.setOnClickListener {


        }

        tag_button.setOnClickListener {
            val intent = Intent(this@TagManagementActivity, CheckNFCActivity::class.java)
            intent.putExtra(AppConst.NFC_METHOD_INTENT, AppConst.NFC_USER_DEBUG)
            startActivity(intent)
        }
    }
}
