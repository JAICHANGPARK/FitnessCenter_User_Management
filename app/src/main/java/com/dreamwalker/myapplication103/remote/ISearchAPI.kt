package com.dreamwalker.myapplication103.remote

import com.dreamwalker.myapplication103.model.SearchResult
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ISearchAPI {


    @FormUrlEncoded
    @POST("fitness_center/UserSearchName.php")
    fun searchWithName(@Field("userName") userName: String): Call<List<SearchResult>>

    @FormUrlEncoded
    @POST("fitness_center/UserSearchTag.php")
    fun searchWithTag(@Field("userTag") userTag: String): Call<List<SearchResult>>

}