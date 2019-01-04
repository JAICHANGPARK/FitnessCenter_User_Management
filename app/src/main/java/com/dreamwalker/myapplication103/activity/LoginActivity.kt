package com.dreamwalker.myapplication103.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.intent.AppConst.BASE_URL
import com.dreamwalker.myapplication103.intent.AppConst.PAPER_AUTO_LOGIN_NAME
import com.dreamwalker.myapplication103.model.Validate
import com.dreamwalker.myapplication103.remote.IUploadAPI
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_login2.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.logging.Logger


class LoginActivity : AppCompatActivity() {

    lateinit var retrofit: Retrofit
    lateinit var service: IUploadAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        Paper.init(this)
        val autoLogin = Paper.book().read<Boolean>(PAPER_AUTO_LOGIN_NAME, false)

        if(autoLogin){
            val intent = Intent(this, HomeActivityV2::class.java)
            startActivity(intent)
            finish()
        }

        retrofit = Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        service = retrofit.create(IUploadAPI::class.java)




        next_button.setOnClickListener {
            if (!isPasswordValid(password_edt.text!!)) {
                password_text_input.error = "error"

            } else {
                password_text_input.error = null

                if (username_edt.text.toString().isNotEmpty()) {
                    Logger.getLogger(packageName).warning(username_edt.text.toString() +  password_edt.text.toString())
                    val loginEnqueue: Call<Validate> = service.userLogin(username_edt.text.toString(), password_edt.text.toString())

                    loginEnqueue.enqueue(object : Callback<Validate> {

                        override fun onResponse(call: Call<Validate>, response: Response<Validate>) {
                            val result = response.body()?.success
                            Logger.getLogger(packageName).warning("" + result)

                            if (result.equals("true")) {
                                Paper.book().write(PAPER_AUTO_LOGIN_NAME, true)

                                val intent = Intent(this@LoginActivity, HomeActivityV2::class.java)
                                startActivity(intent)
                                finish()

                            } else {
                                val builder = AlertDialog.Builder(this@LoginActivity)
                                builder.setTitle("Error")
                                builder.setMessage("로그인 실패")
                                builder.setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
                                builder.show()
                            }
                        }

                        override fun onFailure(call: Call<Validate>, t: Throwable) {
                            toast(t.message.toString())
                        }
                    })
                }


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

                //                val register: Call<ResponseBody> = service.registerUser("knu2019", "kangwon2019")
//                register.enqueue(object : Callback<ResponseBody> {
//                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//
//                    }
//
//                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                        Logger.getLogger(LoginActivity::class.java.name).warning(
//                                " " + response.body()
//                        )
//                    }
//
//                })

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
