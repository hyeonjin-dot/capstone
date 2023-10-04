package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ActivityBoardBinding
import com.example.capstone.utils.currentUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date

class BoardActivity :AppCompatActivity() {

    lateinit var binding: ActivityBoardBinding
    private val currentUserUid = currentUser?.uid
    private val redatabase =
        Firebase.database("https://project-hj-4e44f-default-rtdb.firebaseio.com/")
    var postuid = ""
    var postkey:String? = ""
    var t_post:Posts = Posts()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.run {
            title.visibility = View.GONE
            content.visibility = View.GONE
            dateAnduser.visibility = View.GONE
            buttonEdit.visibility = View.GONE
            titleedit.visibility = View.GONE
            contentedit.visibility = View.GONE
            buttonSave.visibility = View.GONE
            buttonWrite.visibility = View.VISIBLE
            buttonDel.visibility = View.GONE
        }


        binding.buttonExit.setOnClickListener {
            finish()
            val intent = Intent(this, AIActivity::class.java)
            startActivity(intent)
        }

        binding.buttonWrite.setOnClickListener {
            finish()
            val intent = Intent(this, BoardWriteActivity::class.java)
            startActivity(intent)
        }

        binding.buttonEdit.setOnClickListener {
            //수정 어케 함
            binding.run {
                title.visibility = View.GONE
                content.visibility = View.GONE
                dateAnduser.visibility = View.GONE
                buttonEdit.visibility = View.GONE
                titleedit.visibility = View.VISIBLE
                contentedit.visibility = View.VISIBLE
                buttonSave.visibility = View.VISIBLE
                buttonWrite.visibility = View.GONE
                buttonDel.visibility = View.VISIBLE
            }

            val ref = redatabase.getReference("board/posts")
            val postRef = ref.child(postkey!!)
            postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val post = dataSnapshot.getValue(Posts::class.java)
                        binding.titleedit.setText(post?.title)
                        binding.contentedit.setText(post?.content)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("mytag", "error")
                }
            })

            binding.buttonSave.setOnClickListener {
                val currentDate = Date()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val formattedDate = formatter.format(currentDate)

                val updates = HashMap<String, Any>()
                updates["title"] = binding.titleedit.text.toString()
                updates["content"] = binding.contentedit.text.toString()
                updates["date"] = formattedDate

                postRef.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    }
                    else {
                    }
                }

                change()

                binding.run {
                    title.visibility = View.GONE
                    content.visibility = View.GONE
                    dateAnduser.visibility = View.GONE
                    buttonEdit.visibility = View.GONE
                    titleedit.visibility = View.GONE
                    contentedit.visibility = View.GONE
                    buttonSave.visibility = View.GONE
                    buttonWrite.visibility = View.VISIBLE
                    buttonDel.visibility = View.GONE
                    buttonContainer.visibility = View.VISIBLE
                }
            }
        }

        binding.buttonDel.setOnClickListener {
            //삭제
            val ref = redatabase.getReference("board/posts")
            val postRef = ref.child(postkey!!)
            postRef.removeValue()

            change()

            binding.run {
                title.visibility = View.GONE
                content.visibility = View.GONE
                dateAnduser.visibility = View.GONE
                buttonEdit.visibility = View.GONE
                titleedit.visibility = View.GONE
                contentedit.visibility = View.GONE
                buttonSave.visibility = View.GONE
                buttonWrite.visibility = View.VISIBLE
                buttonDel.visibility = View.GONE
                buttonContainer.visibility = View.VISIBLE
            }
        }
        change()
    }

    fun change(){
        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)
        buttonContainer.removeAllViews()
        val ref = redatabase.getReference("board/posts")

        // Attach a listener to read the data at our posts reference
        ref.orderByChild("date").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reversedList = dataSnapshot.children.reversed()
                for (postSnapshot in reversedList) {
                    val post = postSnapshot.getValue(Posts::class.java)
                    val button = Button(this@BoardActivity)
                    val timestamp =
                        post?.date

                    button.text =
                        post?.title + " " + timestamp

                    // 버튼 클릭 리스너 설정
                    button.setOnClickListener {
                        binding.run {
                            title.visibility = View.VISIBLE
                            content.visibility = View.VISIBLE
                            dateAnduser.visibility = View.VISIBLE
                            titleedit.visibility = View.GONE
                            contentedit.visibility = View.GONE
                            if (currentUserUid == post!!.uid){
                                buttonEdit.visibility = View.VISIBLE
                                buttonWrite.visibility = View.GONE
                                buttonDel.visibility = View.VISIBLE
                                buttonSave.visibility = View.GONE
                            }
                            buttonContainer.visibility = View.GONE
                        }
                        binding.title.text = post?.title
                        binding.content.text = post?.content
                        binding.date.text = post?.date
                        binding.user.text = post?.user
                        postkey = postSnapshot.key
                        t_post = post!!
                        postuid = post!!.uid
                    }

                    // LinearLayout에 버튼을 추가
                    buttonContainer.addView(button)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("mytag", "error")
            }
        })
    }

}

class Posts {
    var user: String = ""
    var uid: String = ""
    var content: String = ""
    var title: String = ""
    var date: String = ""

    constructor() {
        // 빈 생성자
    }

    constructor(user: String, uid: String, title: String, content: String, date: String) {
        this.user = user
        this.uid = uid
        this.title = title
        this.content = content
        this.date = date
    }
}
