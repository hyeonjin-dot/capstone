package com.example.capstone

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import com.example.capstone.MyApplication.Companion.db
import com.example.capstone.databinding.ActivityInfowritingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.Timestamp
import com.google.firebase.installations.Utils
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class Musical(val name: String, val date: com.google.firebase.Timestamp, val seat: String, val note: String)

class InfoWritingActivity : AppCompatActivity() {

    lateinit var binding: ActivityInfowritingBinding
    var database = MyApplication.db
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser //지금 유저 확인
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private var timestamp: Timestamp = Timestamp.now()
    private var selectedDateTime: java.util.Date = Date()
    var bitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfowritingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editBtnDate.text = dateFormat.format(calendar.time) // 날짜 today로 설정

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val seat = binding.editTextSeat.text.toString()
            val note = binding.editTextNote.text.toString()

            if (auth.currentUser != null) {
                val usersCollection = database.collection("users")
                val query = usersCollection.whereEqualTo("email", currentUser?.email)

                query.get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                            val documentId = userDoc.id
                            val data = Musical(name, timestamp, seat, note)

                            val newDocRef = usersCollection.document(documentId).collection("musical").add(data)

                            // 이제 newDocRef.id를 사용하여 이미지 업로드 등에 활용할 수 있음
                            newDocRef.addOnSuccessListener { documentReference ->
                                val nowdoc2id = documentReference.id
                                if (bitmap != null) {
                                    // 이미지가 존재하는 경우에만 업로드 수행
                                    val storageRef = FirebaseStorage.getInstance().reference
                                    val fileRef =
                                        storageRef.child("$documentId/data/$nowdoc2id.jpg")

                                    val baos = ByteArrayOutputStream()
                                    bitmap!!.compress(
                                        Bitmap.CompressFormat.JPEG,
                                        100,
                                        baos
                                    )
                                    val imageData = baos.toByteArray()

                                    val uploadTask = fileRef.putBytes(imageData)

                                    uploadTask.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("mytag", "이미지 업로드 성공")
                                        } else {
                                            Log.e(
                                                "mytag",
                                                "이미지 업로드 실패: ${task.exception}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e: Exception ->
                        Toast.makeText(this, "Something Wrong, Try again", Toast.LENGTH_SHORT)
                            .show()
                    }

                finish()
                val intent = Intent(this, AddInfoActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show()
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        binding.buttonCancel.setOnClickListener {
            // 화면을 닫음
            finish()
            val intent = Intent(this, AddInfoActivity::class.java)
            startActivity(intent)
        }

        // DatePicker를 클릭했을 때 DatePickerDialog를 표시하여 날짜를 선택하도록 합니다.
        binding.editBtnDate.setOnClickListener {
            showDatePicker()
        }

        val requestGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            try {
                val option = BitmapFactory.Options()
                option.inSampleSize = 1
                //이미지 로딩
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null
                bitmap?.let {
                    binding.ImgView.setImageBitmap(bitmap)
                }?: let {
                    Log.d("mytag", "bitmap null")
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        binding.AddPicbtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // 선택한 날짜를 저장
            selectedDateTime = selectedCalendar.time

            // 시간 선택 다이얼로그 표시
            showTimePicker()
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = selectedDateTime // 이전에 선택한 날짜 설정
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedCalendar.set(Calendar.MINUTE, minute)

            // 선택한 날짜와 시간을 Timestamp로 변환하여 Firestore에 저장
            timestamp = Timestamp(selectedCalendar.time)

            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:MM", Locale.getDefault())
            val dateString = dateFormat.format(timestamp.toDate()) // Timestamp를 Date로 변환 후 포맷팅
            binding.editBtnDate.text = dateString

        }, hour, minute, true)

        timePickerDialog.show()

    }

}
