package com.example.capstone

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.capstone.MyApplication.Companion.db
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.capstone.databinding.ActivityMainBinding
import com.example.capstone.databinding.ActivitySettingBinding
import com.google.android.play.core.integrity.p
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth // 로그인 인증
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var filePath: String
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendarView: CalendarView = findViewById(R.id.calendarView)
        val dateTextView: TextView = findViewById(R.id.dateTextView)

        if(MyApplication.checkAuth()){
            changeVisibility("login")
        }else {
            changeVisibility("logout")
        }

        //기본 날짜 입력
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일")
        dateTextView.text = sdf.format(Date())
        var targetDate = Calendar.getInstance()
        targetDate.set(Calendar.HOUR_OF_DAY, 0)  // 시간을 24시간 형식으로 설정
        targetDate.set(Calendar.MINUTE, 0)  // 분 설정
        targetDate.set(Calendar.SECOND, 0)  // 초 설정
        targetDate.set(Calendar.MILLISECOND, 0)  // 밀리초 설정

        //버튼 개수 확인하기(db확인)
        val startTimestamp = Timestamp(targetDate.time)
        targetDate.add(Calendar.DAY_OF_MONTH, 1)
        val endTimestamp = Timestamp(targetDate.time)
        checkdatabasenum(startTimestamp, endTimestamp)

        //날짜 선택
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d년 %02d월 %02d일", year, month + 1, dayOfMonth)
            dateTextView.text = selectedDate

            var targetDate = Calendar.getInstance()
            targetDate.set(Calendar.YEAR, year)  // 년도 설정
            targetDate.set(Calendar.MONTH, month)  // 월 설정
            targetDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)  // 일 설정
            targetDate.set(Calendar.HOUR_OF_DAY, 0)  // 시간을 24시간 형식으로 설정
            targetDate.set(Calendar.MINUTE, 0)  // 분 설정
            targetDate.set(Calendar.SECOND, 0)  // 초 설정
            targetDate.set(Calendar.MILLISECOND, 0)  // 밀리초 설정

            //버튼 개수 확인하기(db확인)
            val startTimestamp = Timestamp(targetDate.time)
            targetDate.add(Calendar.DAY_OF_MONTH, 1)  // 다음 날의 00:00:00을 얻기 위해 1일을 더합니다.
            val endTimestamp = Timestamp(targetDate.time)

            checkdatabasenum(startTimestamp, endTimestamp)
        }



        //첫번째 이미지 버튼 누른 경우
        binding.firBtn.setOnClickListener{
            val intent = Intent(this, FirBtnActivity::class.java)
            intent.putExtra("content", "1")
            intent.putExtra("date", dateTextView.text.toString())
            startActivity(intent)
        }

        //두번째 이미지 버튼 누른 경우
        binding.secBtn.setOnClickListener{
            val intent = Intent(this, FirBtnActivity::class.java)
            intent.putExtra("content", "2")
            intent.putExtra("date", dateTextView.text.toString())
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

    fun checkdatabasenum(startTimestamp:Timestamp, endTimestamp: Timestamp){
        var nowdocid = ""
        var nowdocid2 = ""

        if (auth.currentUser != null) {
            val usersCollection = db.collection("users")
            val query = usersCollection.whereEqualTo("email", currentUser?.email)

            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                        val documentId = userDoc.id
                        db.collection("users").document(documentId).collection("musical")
                            .whereGreaterThanOrEqualTo("date", startTimestamp)
                            .whereLessThan("date", endTimestamp)
                            .get()
                            .addOnSuccessListener { documents ->
                                val tmp = documents.size()
                                Log.d("mytag", "num : $tmp")
                                // 1개
                                if (tmp == 1) {
                                    binding.run {
                                        firBtn.visibility = View.VISIBLE
                                        secBtn.visibility = View.GONE
                                    }
                                    utils.getUserDocId { docId ->
                                        nowdocid = docId //docId 가져오기
                                        val doc = documents.first()
                                        nowdocid2 = doc.id
                                        val storageRef = FirebaseStorage.getInstance().reference
                                        val imageRef =
                                            storageRef.child("$nowdocid/data/$nowdocid2.jpg")
                                        val localFile = File.createTempFile("tempImage", "jpg")

                                        imageRef.getFile(localFile)
                                            .addOnSuccessListener {
                                                // 이미지 다운로드 성공
                                                Log.d("mytag", "download o")
                                                val bitmap =
                                                    BitmapFactory.decodeFile(localFile.absolutePath)
                                                binding.firBtn.setImageBitmap(bitmap)
                                            }
                                            .addOnFailureListener {
                                                binding.firBtn.setImageResource(R.drawable.empty)
                                            }
                                    }
                                }
                                // 2개
                                else if (tmp == 2){
                                    binding.run {
                                        firBtn.visibility = View.VISIBLE
                                        secBtn.visibility = View.VISIBLE
                                    }
                                    utils.getUserDocId { docId ->
                                        nowdocid = docId //docId 가져오기
                                        val doc = documents.first()
                                        nowdocid2 = doc.id
                                        val storageRef = FirebaseStorage.getInstance().reference
                                        val imageRef =
                                            storageRef.child("$nowdocid/data/$nowdocid2.jpg")
                                        val localFile = File.createTempFile("tempImage", "jpg")

                                        imageRef.getFile(localFile)
                                            .addOnSuccessListener {
                                                // 이미지 다운로드 성공
                                                Log.d("mytag", "download o")
                                                val bitmap =
                                                    BitmapFactory.decodeFile(localFile.absolutePath)
                                                binding.firBtn.setImageBitmap(bitmap)
                                            }
                                            .addOnFailureListener {
                                                binding.firBtn.setImageResource(R.drawable.empty)
                                            }
                                    }//1번 버튼
                                    utils.getUserDocId { docId ->
                                        nowdocid = docId //docId 가져오기
                                        for ((index, document) in documents.withIndex()) {
                                            if (index == 1) {
                                                val doc = document
                                                nowdocid2 = doc.id
                                                val storageRef =
                                                    FirebaseStorage.getInstance().reference
                                                val imageRef =
                                                    storageRef.child("$nowdocid/data/$nowdocid2.jpg")
                                                val localFile =
                                                    File.createTempFile("tempImage", "jpg")

                                                imageRef.getFile(localFile)
                                                    .addOnSuccessListener {
                                                        // 이미지 다운로드 성공
                                                        Log.d("mytag", "download o")
                                                        val bitmap =
                                                            BitmapFactory.decodeFile(localFile.absolutePath)
                                                        binding.secBtn.setImageBitmap(bitmap)
                                                    }
                                                    .addOnFailureListener {
                                                        binding.secBtn.setImageResource(R.drawable.empty)
                                                    }
                                                break
                                            }
                                        }
                                    }//2번 버튼
                                }
                                else{
                                    binding.run {
                                        firBtn.visibility = View.GONE
                                        secBtn.visibility = View.GONE
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Stn Wrong", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }else {
            binding.run {
                firBtn.visibility = View.GONE
                secBtn.visibility = View.GONE
            }
        }

    }

    fun changeVisibility(mode: String) {
        if (mode === "login") {
            binding.run {
               mainTextView.text= "${MyApplication.email} 님 반갑습니다."
            }
        } else if (mode === "logout") {
            binding.run {
                mainTextView.text= "로그인 부탁드립니다."
            }
        }
    }
}
