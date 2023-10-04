package com.example.capstone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivityChatbotBinding
import com.example.capstone.utils.currentUser
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ChatbotActivity: AppCompatActivity() {

    lateinit var binding: ActivityChatbotBinding
    private val currentUserUid = currentUser?.uid
    private val redatabase =
        Firebase.database("https://project-hj-4e44f-default-rtdb.firebaseio.com/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        //ㅈㅔ출
        binding.submit.setOnClickListener {
            binding.mytxt.text = binding.question.text.toString()
            binding.question.setText("")
            val myRef1 = redatabase.getReference("chatbot")
            val txtRef = myRef1.child("$currentUserUid")
            val updates = HashMap<String, Any>()
            updates["user"] = "user"
            updates["txt"] = binding.mytxt.text
            updates["timestamp"] = System.currentTimeMillis()
            val newTxt = txtRef.push()
            newTxt.setValue(updates) //보냄


            //가져오기
            val ref = FirebaseDatabase.getInstance().getReference("chatbot/$currentUserUid")
            val query = ref.orderByChild("timestamp").limitToLast(1)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val user = childSnapshot.child("user").getValue(String::class.java)
                        if (user == "bot") {
                            val txtValue = childSnapshot.child("txt").getValue(String::class.java)
                            binding.botxt.text = (txtValue)
                        } else {
                            binding.botxt.text = ("잠시만 기다려주세요")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to read value: ${error.toException()}")
                }
            })
        }

    }

}