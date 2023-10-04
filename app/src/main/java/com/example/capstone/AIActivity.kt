package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AIActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai)

        val imageButton1 = findViewById<ImageButton>(R.id.musicalButton)
        val imageButton2 = findViewById<ImageButton>(R.id.seatButton)
        val imageButton3 = findViewById<ImageButton>(R.id.locaButton)
        val imageButton4 = findViewById<ImageButton>(R.id.boardButton)

        imageButton1.setOnClickListener {
            val intent = Intent(this, AIMusicalActivity::class.java)
            startActivity(intent)
        }

        imageButton2.setOnClickListener {
            val intent = Intent(this, AISeatActivity::class.java)
            startActivity(intent)
        }

        imageButton3.setOnClickListener {
            val intent = Intent(this, AILocActivity::class.java)
            startActivity(intent)
        }

        imageButton4.setOnClickListener {
            val intent = Intent(this, BoardActivity::class.java)
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
