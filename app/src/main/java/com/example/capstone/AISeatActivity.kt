package com.example.capstone

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivityAiseatBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class AISeatActivity:AppCompatActivity() {

    lateinit var binding: ActivityAiseatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aiseat)

        binding = ActivityAiseatBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.buttonExit.setOnClickListener {
            finish()
            val intent = Intent(this, AIActivity::class.java)
            startActivity(intent)
        }

        binding.buttonTheater.setOnClickListener {
            finish()
            val intent = Intent(this, TheaterActivity::class.java)
            startActivity(intent)
        }


    }


}

