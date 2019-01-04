package com.dreamwalker.myapplication103.activity

import android.os.Bundle
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.intent.AppConst.NFC_TAG_ID_INTENT
import kotlinx.android.synthetic.main.activity_user_register.*



class UserRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_register)
        val tagId = intent.getStringExtra(NFC_TAG_ID_INTENT)
        title = "TAG Address : " + tagId.toUpperCase()



        user_sex_tiet.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= user_sex_tiet.getRight() - user_sex_tiet.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) {
                    // your action here

                    return@OnTouchListener true
                }
            }
            false
        })


    }
}
