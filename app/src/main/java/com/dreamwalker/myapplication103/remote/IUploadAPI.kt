package com.dreamwalker.myapplication103.remote

import com.dreamwalker.myapplication103.model.Validate
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST



interface IUploadAPI {

    @FormUrlEncoded
    @POST("fitness_center/UserLogin.php")
    fun userLogin(@Field("userID") id: String, @Field("userPassword") pwd: String): Call<Validate>

    @FormUrlEncoded
    @POST("fitness_center/UserRegister.php")
    fun registerUser(@Field("userID") name: String, @Field("userPassword") pwd: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST("fitness_center/UserInfoRegister.php")
    fun userInfoRegister(@Field("userTag") userTag:String,
                         @Field("userName") userName:String,

                         @Field("userSex") userSex:String,
                         @Field("userAge") userAge:String,
                         @Field("userBirth") userBirth:String,
                         @Field("userPhone") userPhone:String,
                         @Field("userEmail") userEmail:String
                         ) : Call<Validate>
}