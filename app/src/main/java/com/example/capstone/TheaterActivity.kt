package com.example.capstone

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.MyApplication.Companion.db
import com.example.capstone.R
import com.example.capstone.databinding.ActivityTheaterBinding
import com.example.capstone.utils.database
import com.google.firebase.auth.FirebaseAuth

class TheaterActivity: AppCompatActivity() {

    lateinit var binding: ActivityTheaterBinding
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var myList: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theater)

        binding = ActivityTheaterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonExit.setOnClickListener {
            finish()
            val intent = Intent(this, AIActivity::class.java)
            startActivity(intent)
        }

        binding.buttonNext.setOnClickListener {
            finish()
            val intent = Intent(this, Theater2Activity::class.java)
            intent.putStringArrayListExtra("seatList", myList)
            startActivity(intent)
        }

        //"아트윈씨어터 2관 a1"
        val color_list:List<String> = listOf("#e4ff33", "#FF5733", "#3370ff", "#33ffe0", "#ff33f8")
        val searchTerm = "아트윈씨어터 2관" //

        if (auth.currentUser != null) {
            val usersCollection = database.collection("users")
            val query = usersCollection.whereEqualTo("email", currentUser?.email)

            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                        val documentId = userDoc.id

                        usersCollection.document(documentId).collection("musical")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    val data = document.data
                                    var matches = false
                                    val seatValue = data["seat"] as? String
                                    if (seatValue != null && seatValue.contains(searchTerm)) {
                                        matches = true
                                    }
                                    if(matches){
                                        val regex = Regex("[a-zA-Z]\\d+")
                                        val matchResult = regex.find(seatValue!!)
                                        var result = ""
                                        matchResult?.let {
                                            result = it.value
                                        } ?: Log.d("mytag", "error")

                                        val textViewId = resources.getIdentifier(result, "id", packageName)
                                        val textView = findViewById<TextView>(textViewId)

                                        myList.add(result)

                                        val backgroundDrawable = textView.background

                                        if (backgroundDrawable is ColorDrawable) {
                                            val backgroundColor = backgroundDrawable.color
                                            for (clr in color_list){
                                                if(backgroundColor == Color.parseColor(clr)){
                                                    val currentIndex = color_list.indexOf(clr)
                                                    val nextIndex = (currentIndex + 1) % color_list.size
                                                    textView.setBackgroundColor(Color.parseColor(color_list[nextIndex]))
                                                    break
                                                }
                                            }
                                        } else {
                                            textView.setBackgroundColor(Color.parseColor(color_list[0]))
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                // 오류 발생 시 처리
                                Log.d("mytag", "nothing")
                            }
                    }
                    else{
                        Log.d("mytag", "nothing")
                    }
                }
        }
    }
}
