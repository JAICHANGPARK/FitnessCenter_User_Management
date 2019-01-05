package com.dreamwalker.myapplication103.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.activity.device.esp32.DeviceScanActivity
import com.dreamwalker.myapplication103.intent.AppConst.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home_v2.*
import kotlinx.android.synthetic.main.app_bar_home_activity_v2.*
import kotlinx.android.synthetic.main.content_home_activity_v2.*
import org.jetbrains.anko.toast

class HomeActivityV2 : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_v2)
        setSupportActionBar(toolbar)

        animation_view.setOnClickListener {
            toast("꿀렁꿀렁")
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        card_register.setOnClickListener {
            toast("회원 등록")
            val intent = Intent(this@HomeActivityV2, CheckNFCActivity::class.java)
            intent.putExtra(NFC_METHOD_INTENT, NFC_USER_REGISTER)
            startActivity(intent)

        }

        card_search.setOnClickListener {
            toast("회원 조회")
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("검색 방식")
            alertDialog.setMessage("어떻게 검색하시겠어요?")
            alertDialog.setPositiveButton("NFC 사용") { dialog, which ->
                dialog.dismiss()
                val intent = Intent(this@HomeActivityV2, CheckNFCActivity::class.java)
                intent.putExtra(NFC_METHOD_INTENT, NFC_USER_SEARCH)
                startActivity(intent)
            }
            alertDialog.setNegativeButton("이름으로 검색") { dialog, which ->

                dialog.dismiss()
            }

            alertDialog.show()
        }

        device_connect.setOnClickListener {
            val intent = Intent(this, DeviceScanActivity::class.java)
            startActivity(intent)

        }

        card_management.setOnClickListener {
            toast("관리")
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_activity_v2, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this@HomeActivityV2, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_register -> {
                // Handle the camera action
                val intent = Intent(this@HomeActivityV2, CheckNFCActivity::class.java)
                intent.putExtra(NFC_METHOD_INTENT, NFC_USER_REGISTER)
                startActivity(intent)

            }

            R.id.nav_search -> {

                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("검색 방식")
                alertDialog.setMessage("어떻게 검색하시겠어요?")
                alertDialog.setPositiveButton("NFC 사용") { dialog, which ->
                    dialog.dismiss()
                    val intent = Intent(this@HomeActivityV2, CheckNFCActivity::class.java)
                    intent.putExtra(NFC_METHOD_INTENT, NFC_USER_SEARCH)
                    startActivity(intent)
                }
                alertDialog.setNegativeButton("이름으로 검색") { dialog, which ->

                    dialog.dismiss()
                }

                alertDialog.show()

            }

            R.id.nav_nfc -> {

//                val intent = Intent(this, MainActivity::class.java)
//                intent.putExtra(NFC_METHOD_INTENT, NFC_USER_DEBUG)
//                startActivity(intent)
                val intent = Intent(this, TagManagementActivity::class.java)
//                intent.putExtra(NFC_METHOD_INTENT, NFC_USER_DEBUG)
                startActivity(intent)

            }

            R.id.nav_setting -> {

                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)

            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
