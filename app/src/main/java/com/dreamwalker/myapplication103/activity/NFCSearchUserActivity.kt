package com.dreamwalker.myapplication103.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import retrofit2.Retrofit

class NFCSearchUserActivity : AppCompatActivity() {

    lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfcsearch_user)



    }
}
