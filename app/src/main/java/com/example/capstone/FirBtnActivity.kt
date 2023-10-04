package com.example.capstone

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.MyApplication.Companion.db
import com.example.capstone.databinding.ActivityFirbtnBinding
import com.example.capstone.databinding.ActivityInfowritingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FirBtnActivity:AppCompatActivity() {

    lateinit var binding: ActivityFirbtnBinding
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    var nowdocid = ""
    var nowdocid2 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirbtnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //취소 버튼
        binding.buttonCancel.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val content = intent.getStringExtra("content")!!.toInt() //1혹은2
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일")
        var date= sdf.format(Date())

        date = intent.getStringExtra("date") //string으로 받음 ㅁㅁㅁㅁ년 ㅁㅁ월 ㅁㅁ일
        binding.editBtnDate.text = date

        //string to timestamp로 찾기 -> 1번 2번 찾기 후 출력

        val year = date.slice(0..3).toInt()
        val month = date.slice(6..7).toInt()
        val day = date.slice(10..11).toInt()

        // 검색하고자 하는 년, 월, 일 값을 가진 Date 객체를 생성합니다.
        val targetDate = Calendar.getInstance()
        targetDate.set(Calendar.YEAR, year)  // 년도 설정
        targetDate.set(Calendar.MONTH, month - 1)  // 월 설정
        targetDate.set(Calendar.DAY_OF_MONTH, day)  // 일 설정
        targetDate.set(Calendar.HOUR_OF_DAY, 0)  // 시간을 24시간 형식으로 설정
        targetDate.set(Calendar.MINUTE, 0)  // 분 설정
        targetDate.set(Calendar.SECOND, 0)  // 초 설정
        targetDate.set(Calendar.MILLISECOND, 0)  // 밀리초 설정

        // Timestamp 범위를 설정하여 쿼리합니다.
        val startTimestamp = Timestamp(targetDate.time)
        targetDate.add(Calendar.DAY_OF_MONTH, 1)  // 다음 날의 00:00:00을 얻기 위해 1일을 더합니다.
        val endTimestamp = Timestamp(targetDate.time)

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
                                for ((index, document) in documents.withIndex()) {
                                    if (index + 1 == content){
                                        binding.editTextName.text = document.getString("name")
                                        binding.editTextSeat.text = document.getString("seat")
                                        val timestamp = document.getTimestamp("date")
                                        val selectedDate = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(timestamp?.toDate())
                                        binding.editBtnDate.text = selectedDate
                                        binding.editTextNote.text = document.getString("note")
                                        //사진 추가
                                        utils.getUserDocId { docId ->
                                            nowdocid = docId //docId 가져오기
                                            val parentCollectionRef = MyApplication.db.collection("users")
                                            val childCollectionRef = parentCollectionRef.document(nowdocid).collection("musical")

                                            childCollectionRef.get()
                                                .addOnSuccessListener { documents ->
                                                    if (!documents.isEmpty) {
                                                        for (doc in documents) {
                                                            if (doc.getString("name") == binding.editTextName.text) {
                                                                nowdocid2 = doc.id
                                                                Log.d("mytag", "fir $nowdocid2")
                                                                break
                                                            }
                                                        }
                                                    }
                                                    val storageRef = FirebaseStorage.getInstance().reference
                                                    val imageRef = storageRef.child("$nowdocid/data/$nowdocid2.jpg")
                                                    Log.d("mytag", "$nowdocid2 nowdocid2")

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
                                                }
                                        }
                                        break
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Stn Wrong", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }else {
            Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show()
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}
