package com.example.capstone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivityBoardwriteBinding
import com.example.capstone.utils.currentUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date


class BoardWriteActivity: AppCompatActivity() {

    lateinit var binding: ActivityBoardwriteBinding
    private val currentUserUid = currentUser?.uid
    private val redatabase =
        Firebase.database("https://project-hj-4e44f-default-rtdb.firebaseio.com/")
    private val userEmail = currentUser?.email


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boardwrite)

        binding = ActivityBoardwriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonExit.setOnClickListener {
            finish()
            val intent = Intent(this, BoardActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSave.setOnClickListener {
            //저장하기
            val myRef1 = redatabase.getReference("board")
            val currentDate = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val formattedDate = formatter.format(currentDate)
            val updates = HashMap<String, Any>()
            updates["user"] = maskEmail(userEmail!!)
            updates["uid"] = currentUserUid!!
            updates["title"] = binding.title.text.toString()
            updates["content"] = binding.content.text.toString()
            updates["date"] = formattedDate
            val postsRef = myRef1.child("posts")
            val newPostRef = postsRef.push()
            newPostRef.setValue(updates)

            //다시 board로 이동
            finish()
            val intent = Intent(this, BoardActivity::class.java)
            startActivity(intent)
        }
    }

    fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex >= 3) {
            val masked = email.substring(0, 3) + "*".repeat(atIndex - 3) + email.substring(atIndex)
            return masked
        }
        return email
    }

}