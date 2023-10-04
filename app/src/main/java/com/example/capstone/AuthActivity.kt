package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.MyApplication.Companion.db
import com.example.capstone.MyApplication.Companion.email
import com.example.capstone.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    var database = MyApplication.db

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(MyApplication.checkAuth()){
            changeVisibility("login")
        }else {
            changeVisibility("logout")
        }

        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            //구글 로그인 결과 처리
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                MyApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this){ task ->
                        if(task.isSuccessful){
                            database.collection("users")
                                .whereEqualTo("email", account.email)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        // 결과가 없는 경우 처리
                                        MyApplication.email = account.email
                                        val user = mapOf(
                                            "email" to MyApplication.email,
                                            "type" to "google"
                                        ) // db에 추가할 데이터 생성
                                        database.collection("users").add(user) //db 추가
                                    }
                                    changeVisibility("login")
                                    val intent = Intent(this, SettingActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener {exception->
                                    // 쿼리 실패 시 처리
                                    val errorMessage = "로그인 실패: ${exception.message}"
                                    // 오류 메시지를 띄우는 코드 또는 로그 기록 등을 추가합니다.
                                    Log.e("my tag", errorMessage)

                                }
                        }else {
                            changeVisibility("logout")
                        }
                    }
            }catch (e: ApiException){
                changeVisibility("logout")
            }
        }

        binding.logoutBtn.setOnClickListener {
            //로그아웃
            MyApplication.auth.signOut()
            MyApplication.email = null
            changeVisibility("logout")
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        binding.goSignInBtn.setOnClickListener{
            changeVisibility("signin")
        }

        binding.goMainBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.googleLoginBtn.setOnClickListener {
            //구글 로그인
            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            requestLauncher.launch(signInIntent)
        }

        binding.signBtn.setOnClickListener {
            //이메일,비밀번호 회원가입
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful){
                        MyApplication.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener{ sendTask ->
                                if(sendTask.isSuccessful){
                                    val user = mapOf(
                                        "email" to MyApplication.email,
                                        "type" to "other"
                                    ) // db에 추가할 데이터 생성
                                    database.collection("users").add(user) // db 추가
                                    Toast.makeText(baseContext, "회원가입에서 성공, 전송된 메일을 확인해 주세요",
                                        Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }else {
                                    Toast.makeText(baseContext, "메일 발송 실패", Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }
                            }
                    }else {
                        Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        changeVisibility("logout")
                    }
                }

        }

        binding.loginBtn.setOnClickListener {
            //이메일, 비밀번호 로그인
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            Log.d("mytag","email:$email, password:$password")
            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful){
                        if(MyApplication.checkAuth()){
                            MyApplication.email = email
                            changeVisibility("login")
                            val intent = Intent(this, SettingActivity::class.java)
                            startActivity(intent)
                        }else {
                            Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show()

                        }
                    }else {
                        Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun changeVisibility(mode: String){
        if(mode === "login"){
            binding.run {
                authMainTextView.text = "${MyApplication.email} 님 반갑습니다."
                logoutBtn.visibility= View.VISIBLE //보일 이유가 없음 수정해야 + 메인 화면 버튼 추가
                goSignInBtn.visibility= View.GONE
                googleLoginBtn.visibility= View.GONE
                authEmailEditView.visibility= View.GONE
                authPasswordEditView.visibility= View.GONE
                signBtn.visibility= View.GONE
                loginBtn.visibility= View.GONE
                goMainBtn.visibility=View.VISIBLE
            }

        }else if(mode === "logout"){
            binding.run {
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.GONE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                goMainBtn.visibility=View.GONE
            }
        }else if(mode === "signin"){
            binding.run {
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
                goMainBtn.visibility=View.GONE
            }
        }
    }
}