package com.example.myfirebase

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myfirebase.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        /*if (Firebase.auth.currentUser == null) {
           /* startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()*/
        }*/


        replaceFragment(HomeFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment)


            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filtering_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_item_all -> {
                Toast.makeText(this, "전체 선택", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.menu_item_soldout -> {
                Toast.makeText(this, "판매완료 선택", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.menu_item_onsale -> {
                Toast.makeText(this, "판매중 선택", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)

    }
}
