package com.example.capstone

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.capstone.MyApplication.Companion.db
import com.example.capstone.databinding.ActivityInfopic2Binding
import com.example.capstone.databinding.ActivitySearchBinding
import com.example.capstone.databinding.ActivitySettingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SearchActivity:AppCompatActivity() {

    lateinit var binding: ActivitySearchBinding
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var num = 0
    var check_date = 0
    var check_scope = 0
    var startTimestamp: Timestamp = Timestamp.now()
    var endTimestamp: Timestamp = Timestamp.now()
    var mon = 0
    var day = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //bottomNavigation
        val bottomNavigationView: BottomNavigationView =
            findViewById(R.id.bottomNavigationView)

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

        binding.run {
            textView1.visibility = View.GONE
            //yddCheckLayout.visibility = View.GONE //날짜 선택
        }

        binding.chatbotBtn.setOnClickListener {
            val intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
        }

        binding.togglebtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {//on -> my
                check_scope = 1
            } else {//off -> all
                check_scope = 0
            }
        }

    binding.searchButton.setOnClickListener {
        binding.run {
            textView1.visibility = View.GONE //일단 안보이게
        }
        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)
        buttonContainer.removeAllViews() // 버튼 지우기
        val searchTerm = binding.searchEditText.text.toString()
        if (searchTerm.isNotEmpty())
            isitdate(searchTerm)//check date
        Log.d("mytag", "first check_date : $check_date")
        if (check_date == 0) {

            if (searchTerm.isNotEmpty()) {
                val usersRef = db.collection("users")

                    usersRef.get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val userId = document.id
                                if (check_scope == 1){//only mine
                                    if(currentUser?.email != document.getString("email")){
                                        continue
                                    }
                                }

                                val musicalRef =
                                    db.collection("users").document(userId).collection("musical")

                                musicalRef.get()
                                    .addOnSuccessListener { musicalDocuments ->
                                        for (musicalDocument in musicalDocuments) {
                                            val data = musicalDocument.data
                                            val docId = musicalDocument.id
                                            var matches = false
                                            Log.d("mytag", "musical id : $docId")

                                            for ((key, value) in data) {
                                                if (value is String && value.contains(searchTerm)) {
                                                    matches = true
                                                    break
                                                }
                                            }
                                            if (matches) {
                                                num++
                                                Log.d("mytag", "it matched!")
                                                val button = Button(this)
                                                val timestamp = musicalDocument.getTimestamp("date")
                                                Log.d("mytag", "$timestamp")
                                                val selectedDate =
                                                    SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(
                                                        timestamp?.toDate()
                                                    )
                                                button.text =
                                                    musicalDocument.getString("name") + " " + selectedDate

                                                // 버튼 클릭 리스너 설정
                                                button.setOnClickListener {
                                                    val intent =
                                                        Intent(this, SearchBtnActivity::class.java)
                                                    intent.putExtra("userid", userId)
                                                    intent.putExtra("docid", docId)
                                                    startActivity(intent)
                                                }

                                                // LinearLayout에 버튼을 추가
                                                buttonContainer.addView(button)

                                            }
                                        }
                                        if (num == 0) {
                                            binding.run {
                                                textView1.visibility = View.VISIBLE
                                            }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d("mytag", "musical collection search 오류")
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d("mytag", "users collection search 오류")
                        }

            } else {
                binding.run {
                    textView1.visibility = View.VISIBLE
                }
            }
        } else {
            val usersRef = db.collection("users")

            usersRef.get()
                .addOnSuccessListener { userdocuments ->
                    for (userdocument in userdocuments) {
                        val userId = userdocument.id

                        if (check_scope == 1) {//only mine
                            if (currentUser?.email != userdocument.getString("email")) {
                                continue
                            }
                        }
                        val musicalRef =
                            db.collection("users").document(userId).collection("musical")

                        if (mon == 0) {
                            Log.d("mytag", "mon is 0")

                            musicalRef
                                .whereGreaterThanOrEqualTo("date", startTimestamp)
                                .whereLessThan("date", endTimestamp).get()
                                .addOnSuccessListener { musicalDocuments ->
                                    for (musicalDocument in musicalDocuments) {
                                        val docId = musicalDocument.id

                                        num++
                                        Log.d("mytag", "it matched!")
                                        val buttonContainer =
                                            findViewById<LinearLayout>(R.id.buttonContainer)
                                        val button = Button(this)
                                        val timestamp =
                                            musicalDocument.getTimestamp("date")
                                        Log.d("mytag", "$timestamp")
                                        val selectedDate =
                                            SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(
                                                timestamp?.toDate()
                                            )
                                        button.text =
                                            musicalDocument.getString("name") + " " + selectedDate

                                        // 버튼 클릭 리스너 설정
                                        button.setOnClickListener {
                                            val intent = Intent(
                                                this,
                                                SearchBtnActivity::class.java
                                            )
                                            intent.putExtra("userid", userId)
                                            intent.putExtra("docid", docId)
                                            startActivity(intent)
                                        }

                                        // LinearLayout에 버튼을 추가
                                        buttonContainer.addView(button)

                                    }
                                    if (num == 0) {
                                        binding.run {
                                            textView1.visibility = View.VISIBLE
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("mytag", "musical collection search 오류")
                                }
                        } else {//월일의 경우 10년전부터 찾음
                            val calendar = Calendar.getInstance()
                            val currentYear = calendar.get(Calendar.YEAR)

                            for (year in (currentYear - 9) until (currentYear + 1)) {
                                calendar.set(year, mon - 1, day, 0, 0, 0) // 해당 연도의 대상 날짜로 설정

                                if (calendar.get(Calendar.MONTH) == mon - 1 && calendar.get(Calendar.DAY_OF_MONTH) == day) {
                                    startTimestamp = Timestamp(calendar.time)
                                    calendar.add(Calendar.DATE, 1)
                                    endTimestamp = Timestamp(calendar.time)

                                    Log.d("mytag", "start : ${startTimestamp.toDate()}")
                                    Log.d("mytag", "end : ${endTimestamp.toDate()}")


                                    val timeref = db.collection("users").document(userId)
                                        .collection("musical")
                                        .whereGreaterThanOrEqualTo("date", startTimestamp)
                                        .whereLessThan("date", endTimestamp)

                                    timeref.get()
                                        .addOnSuccessListener { musicalDocuments ->
                                            for (musicalDocument in musicalDocuments) {
                                                val docId = musicalDocument.id

                                                num++
                                                Log.d("mytag", "it matched!")
                                                val buttonContainer =
                                                    findViewById<LinearLayout>(R.id.buttonContainer)
                                                val button = Button(this)
                                                val timestamp =
                                                    musicalDocument.getTimestamp("date")
                                                Log.d("mytag", "$timestamp")
                                                val selectedDate =
                                                    SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(
                                                        timestamp?.toDate()
                                                    )
                                                button.text =
                                                    musicalDocument.getString("name") + " " + selectedDate

                                                // 버튼 클릭 리스너 설정
                                                button.setOnClickListener {
                                                    val intent = Intent(
                                                        this,
                                                        SearchBtnActivity::class.java
                                                    )
                                                    intent.putExtra("userid", userId)
                                                    intent.putExtra("docid", docId)
                                                    startActivity(intent)
                                                }

                                                // LinearLayout에 버튼을 추가
                                                buttonContainer.addView(button)

                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.d("mytag", "musical collection search 오류")
                                        }
                                }
                            }
                            if (num == 0) {
                                binding.run {
                                    textView1.visibility = View.VISIBLE
                                }
                            }
                            else{
                                binding.run {
                                    textView1.visibility = View.GONE
                                }
                            }
                        }
                    }
                            }
                            .addOnFailureListener { exception ->
                                Log.d("mytag", "users collection search 오류")
                            }
                    }
        }
    }



    private fun isitdate(date:String){
        var newdate :Date = Date()
        val dateFormat1 = "yyyy년 MM월 dd일"
        val dateFormat2 = "yyyy년 MM월"
        val dateFormat3 = "MM월 dd일"// 저장할 유형

        check_date = 1
        if (isDateValid(date, dateFormat1)){
            newdate = SimpleDateFormat(dateFormat1).parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = newdate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            startTimestamp = Timestamp(calendar.time)
            calendar.add(Calendar.DATE, 1)
            endTimestamp = Timestamp(calendar.time)
        }
        else if(isDateValid(date, dateFormat2)){
            newdate = SimpleDateFormat(dateFormat2).parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = newdate
            calendar.set(Calendar.DATE, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            startTimestamp = Timestamp(calendar.time)
            calendar.add(Calendar.MONTH, 1)
            endTimestamp = Timestamp(calendar.time)
        }
        else if(isDateValid(date, dateFormat3)){//월 일
            var tmp = 0
            for (str in date) {
                if (str in '0'..'9') {
                    tmp = tmp * 10 + (str - '0')
                } else {
                    if (mon == 0) {
                        mon = tmp
                    } else {
                        day = tmp
                    }
                    tmp = 0
                }
            }
        }
        else{
            check_date = 0
            return
        }
        return
    }

    private fun isDateValid(dateString: String, format: String): Boolean {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.isLenient = false

        return try {
            val date = sdf.parse(dateString)
            date != null
        } catch (e: Exception) {
            false
        }
    }
}
