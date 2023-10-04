package com.example.capstone

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.MyApplication.Companion.auth
import com.example.capstone.databinding.ActivityEditBinding
import com.example.capstone.databinding.ActivitySettingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.core.View
import java.util.Calendar
import java.util.Date

class EditActivity:AppCompatActivity() {
    lateinit var binding: ActivityEditBinding
    private var selectedDate: java.util.Date = Date()
    var database = MyApplication.db
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser //지금 유저 확인
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editBtnDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSave.setOnClickListener {
            var name = binding.editTextName.text.toString()

            if(name.isEmpty())
            {
                Toast.makeText(this, "Please Input all", Toast.LENGTH_SHORT).show()
            }
            else if (auth.currentUser != null) {
                val usersCollection = database.collection("users")
                val query = usersCollection.whereEqualTo("email", currentUser?.email)

                query.get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                            val documentId = userDoc.id
                            val data = mapOf(
                                "name" to name, "birthday" to Timestamp(selectedDate)
                            )

                            usersCollection.document(documentId)
                                .update(data)
                                .addOnSuccessListener {
                                    // 업데이트 성공 시 처리할 내용을 추가합니다.
                                    Toast.makeText(this, "새 필드가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e:Exception ->
                                    // 업데이트 실패 시 처리할 내용을 추가합니다.
                                    Toast.makeText(this, "업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(this, "Something Wrong, Try again", Toast.LENGTH_SHORT)
                            .show()
                    }

                finish()
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show()
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        binding.buttonCancel.setOnClickListener {
            finish()
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, _, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(Calendar.MONTH, month)
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // 선택한 월과 일을 저장
            selectedDate = selectedCalendar.time

            val dateFormat = SimpleDateFormat("MM월 dd일", Locale.getDefault())
            val dateString = dateFormat.format(selectedDate)
            binding.editBtnDate.text = dateString

            // 시간 선택 다이얼로그 표시
        }, year, month, day)

        // 연도 선택을 비활성화
        datePickerDialog.datePicker.findViewById<android.view.View>(resources.getIdentifier("year", "id", "android"))?.visibility = android.view.View.GONE

        datePickerDialog.show()
    }

}
