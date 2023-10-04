package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivityAddinfoBinding
import com.example.capstone.databinding.ActivityInfopic2Binding
import com.example.capstone.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AddInfoActivity:AppCompatActivity() {

    lateinit var binding: ActivityAddinfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButton1.setOnClickListener {
            val intent = Intent(this, InfoPicActivity::class.java)
            startActivity(intent)
        }

        binding.imageButton2.setOnClickListener {
            val intent = Intent(this, InfoWritingActivity::class.java)
            startActivity(intent)
        }

        //bottomNavigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_calendar -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_settings -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_addinfo -> {
                    val intent = Intent(this, AddInfoActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_recommand -> {
                    val intent = Intent(this, AIActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
