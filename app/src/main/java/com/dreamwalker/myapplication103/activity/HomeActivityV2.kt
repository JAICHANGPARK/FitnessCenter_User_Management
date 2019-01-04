package com.dreamwalker.myapplication103.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dreamwalker.myapplication103.MainActivity
import com.dreamwalker.myapplication103.R
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
        }

        card_search.setOnClickListener {
            toast("회원 조회")
        }

        card_management.setOnClickListener {
            toast("관리")
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
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_register -> {
                // Handle the camera action
            }

            R.id.nav_search -> {

            }

            R.id.nav_nfc -> {


                val intent = Intent(this, MainActivity::class.java)
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
