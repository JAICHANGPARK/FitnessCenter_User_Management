package com.dreamwalker.myapplication103.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.intent.AppConst.NFC_TAG_ID_INTENT

class UserRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_register)
        val tagId = intent.getStringExtra(NFC_TAG_ID_INTENT)
        title = tagId


    }
}
