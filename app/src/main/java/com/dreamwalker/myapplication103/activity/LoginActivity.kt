package com.dreamwalker.myapplication103.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import kotlinx.android.synthetic.main.activity_login2.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        next_button.setOnClickListener {
            if (!isPasswordValid(password_edt.text!!)) {
                password_text_input.error = "error"

            } else {
                password_text_input.error = null
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }

        password_edt.setOnKeyListener { _, _, _ ->
            if (isPasswordValid(password_edt.text!!)) {
                password_text_input.error = null
            }
            false
        }

        cancel_button.setOnClickListener {
            val dialog = AlertDialog.Builder(this)

            dialog.setTitle("알림")
            dialog.setMessage("본 앱은 회원가입이 불가능합니다. 관리자에게 문의하세요")

            dialog.setPositiveButton(android.R.string.yes) { dialog, _ ->
                dialog.dismiss()
            }

            dialog.show()
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
        }

    }

    private fun isPasswordValid(text: Editable?): Boolean {
        return text != null && text.length >= 8
    }

}
