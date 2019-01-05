package com.dreamwalker.myapplication103.activity

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.intent.AppConst
import com.dreamwalker.myapplication103.intent.AppConst.NFC_TAG_ID_INTENT
import com.dreamwalker.myapplication103.model.Validate
import com.dreamwalker.myapplication103.remote.IUploadAPI
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.activity_user_register.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class UserRegisterActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {


    var userTag: String? = null
    var userName: String? = null
    var userAge: String? = null
    var userBirth: String? = null
    var userPhone: String? = null
    var userSex: String? = null
    var userEmail: String? = null


    lateinit var retrofit: Retrofit
    lateinit var service: IUploadAPI

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        userBirth = "$year-${monthOfYear + 1}-$dayOfMonth"
        date_tiet.setText(userBirth)
//        toast("$year-$monthOfYear-$dayOfMonth")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_register)
        val tagId = intent.getStringExtra(NFC_TAG_ID_INTENT)
        userTag = tagId
        title = "TAG Address : " + tagId.toUpperCase()

        retrofit = Retrofit.Builder().baseUrl(AppConst.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        service = retrofit.create(IUploadAPI::class.java)

        val now = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(
                this@UserRegisterActivity,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        )
// If you're calling this from a support Fragment


        user_sex_tiet.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            val selectedItem = arrayOf("남성", "여성")
            alertDialog.setTitle("성별 선택")
            alertDialog.setSingleChoiceItems(selectedItem, -1) { dialog, which ->
                toast(selectedItem[which])
                user_sex_tiet.setText(selectedItem[which])
                dialog.dismiss()
            }
            alertDialog.show()
        }

        date_tiet.setOnClickListener {
            dpd.show(supportFragmentManager, "Datepickerdialog")
        }
//        user_sex_tiet.setOnTouchListener(OnTouchListener { v, event ->
//            val DRAWABLE_LEFT = 0
//            val DRAWABLE_TOP = 1
//            val DRAWABLE_RIGHT = 2
//            val DRAWABLE_BOTTOM = 3
//
//            if (event.action == MotionEvent.ACTION_UP) {
//                if (event.rawX >= user_sex_tiet.getRight() - user_sex_tiet.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) {
//                    // your action here
//
//                    return@OnTouchListener true
//                }
//            }
//            false
//        })

        checkout_button.setOnClickListener {

            if (name_tiet.text.toString().isEmpty()) {
                name_textinputlayout.error = "이름을 입력해주세요"
            } else {

                userName = name_tiet.text.toString()
                name_textinputlayout.error = null
            }

            if (age_tiet.text.toString().isEmpty()) {
                age_textinputlayout.error = "나이를 입력해주세요"
            } else {
                userAge = age_tiet.text.toString()
                age_textinputlayout.error = null
            }

            if (date_tiet.text.toString().isEmpty()) {

                date_textinputlayer.error = "생년월일을 입력해주세요"
            } else {
                userBirth = date_tiet.text.toString()
                date_textinputlayer.error = null
            }

            if (phone_tiet.text.toString().isEmpty()) {
                phone_textinputlayout.error = "전화번호를 입력해주세요"
            } else {
                userPhone = phone_tiet.text.toString()
                phone_textinputlayout.error = null
            }

            if (user_sex_tiet.text.toString().isEmpty()) {
                user_sex_til.error = "성별을 입력해주세요"
            } else {
                when (user_sex_tiet.text.toString()) {
                    "남성" -> userSex = "M"
                    "여성" -> userSex = "F"
                }
                user_sex_til.error = null
            }

            if (email_tiet.text.toString().isEmpty()) {
                email_textinputlayout.error = "이메일을 입력해주세요"
            } else {
                userEmail = email_tiet.text.toString()
                email_textinputlayout.error = null
            }

            if (userName != null && userAge != null && userBirth!!.isNotEmpty()
                    && userPhone!!.isNotEmpty() && userSex!!.isNotEmpty() && userEmail!!.isNotEmpty()) {

                val registerEnqueue: Call<Validate> = service.userInfoRegister(
                        userTag!!,
                        userName!!,
                        userSex!!,
                        userAge!!,
                        userBirth!!,
                        userPhone!!,
                        userEmail!!
                )

                registerEnqueue.enqueue(object : Callback<Validate> {

                    override fun onResponse(call: Call<Validate>, response: Response<Validate>) {
                        val result = response.body()?.success
                        if (result.equals("true")) {
                            finish()
                        } else {
                            val builder = AlertDialog.Builder(this@UserRegisterActivity)
                            builder.setTitle("Error")
                            builder.setMessage("등록 실패")
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
}
