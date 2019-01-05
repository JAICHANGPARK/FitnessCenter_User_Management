package com.dreamwalker.myapplication103.activity.search

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.adapter.device.scan.DeviceItemClickListener
import com.dreamwalker.myapplication103.adapter.search.SearchAdapterV2
import com.dreamwalker.myapplication103.intent.AppConst
import com.dreamwalker.myapplication103.model.SearchResult
import com.dreamwalker.myapplication103.remote.ISearchAPI
import com.lapism.searchview.Search
import kotlinx.android.synthetic.main.activity_search_user_name.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchUserNameActivity : AppCompatActivity(), DeviceItemClickListener {
    override fun onItemClick(v: View?, position: Int) {
        toast("" + position)

    }

    override fun onItemLongClick(v: View?, position: Int) {
    }


    lateinit var retrofit: Retrofit
    lateinit var service: ISearchAPI
    private var tagID: String? = null

    lateinit var userList: List<SearchResult>
    lateinit var searchAdapter: SearchAdapterV2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user_name)
        updateStatusBarColor()
        setSupportActionBar(toolbar)
        toolbar.title = "이름으로 검색"


        userList = ArrayList()

        searchAdapter = SearchAdapterV2(userList as ArrayList<SearchResult>, this@SearchUserNameActivity )
        searchAdapter.setDeviceItemClickListener(this)
        with(recycler_view) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@SearchUserNameActivity)
            addItemDecoration(DividerItemDecoration(this@SearchUserNameActivity, DividerItemDecoration.VERTICAL))
            adapter = searchAdapter
        }

        retrofit = Retrofit.Builder().baseUrl(AppConst.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        service = retrofit.create(ISearchAPI::class.java)


        searchBar.setOnQueryTextListener(object : Search.OnQueryTextListener {
            override fun onQueryTextSubmit(query: CharSequence?): Boolean {
                toast(query.toString())
                if (query.toString().isNotEmpty()) {

                    val searchEnqueue = service.searchWithName(query.toString())

                    searchEnqueue.enqueue(object : Callback<List<SearchResult>> {
                        override fun onFailure(call: Call<List<SearchResult>>, t: Throwable) {
                            toast(t.message.toString())
                        }

                        override fun onResponse(call: Call<List<SearchResult>>, response: Response<List<SearchResult>>) {

                            // Logger.getLogger(packageName).warning(response.body().toString())
                            val result = response.body()
                            if (result != null) {
                                (userList as ArrayList<SearchResult>).clear()
                                (userList as ArrayList<SearchResult>).addAll(result)
                                searchAdapter.notifyDataSetChanged()
//                                searchAdapter = SearchAdapterV2( result as java.util.ArrayList<SearchResult>?, this@SearchUserNameActivity)
//                                recycler_view.adapter = searchAdapter
//                    for (sr : SearchResult in result){
//                        toast(sr.userName)
//                    }
                            }

                        }

                    }
                    )

                }


                return false
            }

            override fun onQueryTextChange(newText: CharSequence?) {
                toast(newText.toString())
            }

        })



    }


    //컬러 리소스로 변경(예 : R.color.deep_blue)
    private fun updateStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.sample)
        }
    }


}
