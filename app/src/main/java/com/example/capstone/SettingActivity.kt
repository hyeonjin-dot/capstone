package com.example.capstone

import android.app.DownloadManager.Query
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.capstone.MyApplication.Companion.db
import com.example.capstone.databinding.ActivitySettingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingActivity:AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding
    lateinit var filePath: String
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var database = MyApplication.db


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(MyApplication.checkAuth()){
            changeVisibility("login")
        }else {
            changeVisibility("logout")
        }

        //정보 입력 파트
        if (auth.currentUser != null) {
            val usersCollection = database.collection("users")
            val query = usersCollection.whereEqualTo("email", currentUser?.email)
            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                        var name = userDoc.getString("name")
                        var bd = userDoc.getTimestamp("birthday")
                        if (name === null)
                            name = "이름을 입력하세요"
                        if (bd === null)
                            binding.birthday.text = "생일을 입력하세요"
                        else {
                            val dateFormat = SimpleDateFormat("MM월 dd일", Locale.getDefault())
                            val dateString =
                                dateFormat.format(bd?.toDate()) // Timestamp를 Date로 변환 후 포맷팅

                            binding.username.text = name
                            binding.birthday.text = dateString
                        }
                    }
                }
        }

        //횟 수
        val startOfMonth = Calendar.getInstance()
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        startOfMonth.set(Calendar.MINUTE, 0)
        startOfMonth.set(Calendar.SECOND, 0)
        startOfMonth.set(Calendar.MILLISECOND, 0)
        val endOfMonth = Calendar.getInstance()
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))

        if (auth.currentUser != null) {
            val usersCollection = database.collection("users")
            val query = usersCollection.whereEqualTo("email", currentUser?.email)
            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                        val userId = userDoc.id

                        MyApplication.db.collection("users").document(userId).collection("musical")
                            .whereGreaterThanOrEqualTo("date", startOfMonth.time)
                            .whereLessThan("date", endOfMonth.time)
                            .get()
                            .addOnSuccessListener { documents ->
                                val uniqueDates = HashSet<String>()

                                for (document in documents) {
                                    val documentId = document.id
                                    uniqueDates.add(documentId)
                                }

                                val count = uniqueDates.size
                                binding.thismonth.setText(count.toString()+"번") // 횟수
                            }
                        }
                    }
        }

        //가까운 극
        if (auth.currentUser != null) {
            val usersCollection = database.collection("users")
            val query = usersCollection.whereEqualTo("email", currentUser?.email)
            query.get()
                .addOnSuccessListener { userdocs ->
                    if (!userdocs.isEmpty) {
                        val userDoc = userdocs.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                        val userId = userDoc.id
                        var diff:Long = 0
                        var tmptime = Timestamp.now()
                        var resultname = ""


                        MyApplication.db.collection("users").document(userId).collection("musical")
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    val tmp = documents.first().getTimestamp("date")!!.seconds - tmptime.seconds
                                    diff = Math.abs(tmp)
                                    resultname = documents.first().getString("name")!!
                                    for(document in documents){
                                        val tmp = document.getTimestamp("date")!!.seconds - tmptime.seconds
                                        if(diff > Math.abs(tmp)){
                                            diff = Math.abs(tmp)
                                            resultname = document.getString("name")!!
                                        }
                                    }
                                    binding.closeone.setText(resultname)
                                } else {
                                    binding.closeone.setText("저장된 내용이 없습니다.")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.d("mytag", "가까운 극 에러")
                            }
                    }
                }
        }




        //프로필 사진 설정
        var nowdocid = ""
        utils.getUserDocId { docId ->
            nowdocid = docId //docId 가져오기

            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("$nowdocid/profile/setting_profile.jpg")

            val localFile = File.createTempFile("tempImage", "jpg")

            imageRef.getFile(localFile)
                .addOnSuccessListener {
                    // 이미지 다운로드 성공
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    binding.userImageView.setImageBitmap(bitmap)
                }
                .addOnFailureListener {
                    binding.userImageView.setImageResource(R.drawable.defaultprofile)
                }
        }


        val requestGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            try {
                // inSampleSize 비율 계산, 지정
                val calRatio = calculateInSampleSize(
                    it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize)
                )
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio
                //이미지 로딩
                var inputStream = contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream!!.close()
                inputStream = null
                bitmap?.let {
                    binding.userImageView.setImageBitmap(bitmap)
                    //사진 업로드
                    val storageRef = FirebaseStorage.getInstance().reference
                    val fileRef = storageRef.child("$nowdocid/profile/setting_profile.jpg")
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    val uploadTask = fileRef.putBytes(data)

                    uploadTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "업로드 성공!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "업로드 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }?: let {
                    Log.d("mytag", "bitmap null")
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        binding.galleryButton.setOnClickListener{
            //갤러리 앱
            val intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        //카메라 요청
        val requestCameraFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            val calRatio = calculateInSampleSize(
                Uri.fromFile(File(filePath)),
                resources.getDimensionPixelSize(R.dimen.imgSize),
                resources.getDimensionPixelSize(R.dimen.imgSize)
            )
            val option = BitmapFactory.Options()
            option.inSampleSize = calRatio
            val bitmap = BitmapFactory.decodeFile(filePath, option)
            bitmap?.let {
                binding.userImageView.setImageBitmap(bitmap)
                //사진 업로드
                val storageRef = FirebaseStorage.getInstance().reference
                val fileRef = storageRef.child("$nowdocid/profile/setting_profile.jpg")
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val uploadTask = fileRef.putBytes(data)

                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "업로드 성공!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "업로드 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.cameraButton.setOnClickListener{
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )
            filePath = file.absolutePath
            val photoURI: Uri = FileProvider.getUriForFile(
                this, "com.example.capstone.fileprovider",file
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            requestCameraFileLauncher.launch(intent)
        }

        binding.loginButton.setOnClickListener{
            //로그인 버튼
            val intent = Intent(this, AuthActivity::class.java)//인증으로 이동
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener{
            //로그아웃 버튼
            MyApplication.auth.signOut()
            MyApplication.email = null
            changeVisibility("logout")
            val intent = Intent(this, MainActivity::class.java)//인증으로 이동
            startActivity(intent)
        }

        binding.editButton.setOnClickListener {
            // 정보 수정 버튼
            val intent = Intent(this, EditActivity::class.java)//정보수정으로 이동
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
    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            var inputStream = contentResolver.openInputStream(fileUri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        // inSampleSize 비율 계산
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun changeVisibility(mode: String) {
        if (mode === "login") {
            binding.run {
                loginButton.visibility = View.GONE
                logoutButton.visibility = View.VISIBLE
                editButton.visibility = View.VISIBLE
                loginplz.visibility = View.GONE
                username.visibility = View.VISIBLE
                birthday.visibility = View.VISIBLE
                thismonth.visibility = View.VISIBLE
                closeone.visibility = View.VISIBLE
                galleryButton.visibility = View.VISIBLE
                cameraButton.visibility = View.VISIBLE
            }
        } else if (mode === "logout") {
            binding.run {
                loginButton.visibility = View.VISIBLE
                logoutButton.visibility = View.GONE
                editButton.visibility = View.GONE
                loginplz.visibility = View.VISIBLE
                username.visibility = View.GONE
                birthday.visibility = View.GONE
                thismonth.visibility = View.GONE
                closeone.visibility = View.GONE
                galleryButton.visibility = View.GONE
                cameraButton.visibility = View.GONE
            }
        }
    }
}
