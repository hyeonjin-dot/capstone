package com.example.capstone

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.capstone.MyApplication.Companion.db
import com.example.capstone.databinding.ActivityFirbtnBinding
import com.example.capstone.databinding.ActivityInfowritingBinding
import com.example.capstone.databinding.ActivitySearchbtnBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class SearchBtnActivity:AppCompatActivity() {

    lateinit var binding: ActivitySearchbtnBinding
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    var userId = ""
    var docId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchbtnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //취소 버튼
        binding.buttonCancel.setOnClickListener{
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        if (intent.getStringExtra("userid") != null) {
            userId = intent.getStringExtra("userid")!!
        }
        if (intent.getStringExtra("docid") != null) {
            docId = intent.getStringExtra("docid")!!
        }

        if (auth.currentUser != null) {
            val userRef = db.collection("users").document(userId)
            val musicalRef = userRef.collection("musical").document(docId)

            musicalRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // 문서가 존재하는 경우
                        binding.editTextName.text = document.getString("name")
                        binding.editTextSeat.text = document.getString("seat")
                        val timestamp = document.getTimestamp("date")
                        val selectedDate = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(timestamp?.toDate())
                        binding.editBtnDate.text = selectedDate
                        binding.editTextNote.text = document.getString("note")

                        val storageRef = FirebaseStorage.getInstance().reference
                        val imageRef = storageRef.child("$userId/data/$docId.jpg")

                        val localFile = File.createTempFile("tempImage", "jpg")
                        imageRef.getFile(localFile)
                            .addOnSuccessListener {
                                // 이미지 다운로드 성공
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                binding.ImgView.setImageBitmap(bitmap)
                            }
                            .addOnFailureListener {
                                binding.ImgView.setImageResource(R.drawable.empty)
                            }
                    } else {
                        // 문서가 존재하지 않는 경우
                    }
                }
                .addOnFailureListener { exception ->
                    // 오류 처리
                }
        }

    }
}
