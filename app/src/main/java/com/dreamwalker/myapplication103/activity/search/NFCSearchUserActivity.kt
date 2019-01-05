package com.dreamwalker.myapplication103.activity.search

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.adapter.search.SearchAdapter
import com.dreamwalker.myapplication103.intent.AppConst.BASE_URL
import com.dreamwalker.myapplication103.intent.AppConst.NFC_SEARCH_TAG_ID_INTENT
import com.dreamwalker.myapplication103.model.SearchResult
import com.dreamwalker.myapplication103.remote.ISearchAPI
import kotlinx.android.synthetic.main.activity_nfcsearch_user.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NFCSearchUserActivity : AppCompatActivity() {
    val TAG = "NFCSearchUserActivity"

    lateinit var retrofit: Retrofit
    lateinit var service: ISearchAPI
    private var tagID: String? = null

    var userList : List<SearchResult>? = null
    lateinit var searchAdapter :SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfcsearch_user)

        userList = ArrayList()

        searchAdapter = SearchAdapter(this@NFCSearchUserActivity, userList as ArrayList<SearchResult>)
        searchAdapter.setOnSearchItemClickListener { v, position ->
            toast(position)
        }

        with(recycler_view){
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@NFCSearchUserActivity)
            addItemDecoration(DividerItemDecoration(this@NFCSearchUserActivity, DividerItemDecoration.VERTICAL))
            adapter = searchAdapter
        }

        tagID = intent.getStringExtra(NFC_SEARCH_TAG_ID_INTENT)
        toast(tagID!!)

        retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        service = retrofit.create(ISearchAPI::class.java)

        val searchEnqueue = service.searchWithTag(tagID!!)

        searchEnqueue.enqueue(object : Callback<List<SearchResult>> {
            override fun onFailure(call: Call<List<SearchResult>>, t: Throwable) {
                toast(t.message.toString())
            }

            override fun onResponse(call: Call<List<SearchResult>>, response: Response<List<SearchResult>>) {
                Log.e(TAG, "" + response.body().toString())
               // Logger.getLogger(packageName).warning(response.body().toString())
                val result = response.body()
                if (result != null) {
                    userList = result
                    searchAdapter = SearchAdapter(this@NFCSearchUserActivity, result as java.util.ArrayList<SearchResult>?)
                    recycler_view.adapter = searchAdapter
//                    for (sr : SearchResult in result){
//                        toast(sr.userName)
//                    }
                }

            }

        }
        )

    }
}
