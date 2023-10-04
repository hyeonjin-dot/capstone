package com.example.capstone

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.databinding.ActivityAimusicalBinding
import com.example.capstone.utils.database
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.checkerframework.checker.units.qual.s


class AIMusicalActivity:AppCompatActivity() {

    lateinit var binding: ActivityAimusicalBinding
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    private lateinit var itemAdapter: ItemAdapter
    var totalDocuments = 0
    private var selectedItemsList = mutableListOf<Pair<String, Int>>()
    private val currentUserUid = currentUser?.uid
    private val redatabase =
        Firebase.database("https://project-hj-4e44f-default-rtdb.firebaseio.com/")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aimusical)

        binding = ActivityAimusicalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.run {
            resBox.visibility = View.GONE
            textAndbox.visibility = View.VISIBLE
            buttonNext.visibility = View.GONE
            buttonSave.visibility = View.VISIBLE
            resLayout.visibility = View.GONE
        }

        // 현재 로그인한 사용자 가져오기
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val usersCollection = database.collection("users")
            val query = usersCollection.whereEqualTo("email", utils.currentUser?.email)
            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                        val docId = userDoc.id
                        // Firestore에서 해당 사용자의 문서 가져오기
                        database.collection("users")
                            .document(docId).collection("musical")
                            .get()
                            .addOnSuccessListener { documents ->
                                val nameCounts = mutableMapOf<String, Int>()

                                for (document in documents) {
                                    val name = document.getString("name")
                                    if (name != null) {
                                        if (nameCounts.containsKey(name)) {
                                            nameCounts[name] = nameCounts[name]!! + 1
                                        } else {
                                            nameCounts[name] = 1
                                        }
                                    }
                                }

                                // 총 문서 수 계산
                                totalDocuments = documents.size()
                                val sortedNameCounts = nameCounts.entries.sortedByDescending { it.value }

                                // RecyclerView에서 사용할 데이터 리스트
                                val itemList = sortedNameCounts.map { it.key to it.value }

                                // RecyclerView 초기화
                                itemAdapter = ItemAdapter(itemList)
                                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                                recyclerView.layoutManager = LinearLayoutManager(this)
                                recyclerView.adapter = itemAdapter

                            }
                            .addOnFailureListener { exception ->
                                // 문서를 가져오지 못했을 때의 처리
                                Log.d("mytag", "not get doc")
                            }
                    }
                }
        }

        binding.buttonCancel.setOnClickListener {
            // 화면을 닫음
            finish()
            val intent = Intent(this, AIActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSave.setOnClickListener {
            val selectedItemCount = itemAdapter.getSelectedItemCount()
            if (selectedItemCount != 5){
                showCancelButtonDialog()
            }
            else{
                drawChart() //5개면 그래프
            }
        }

        binding.buttonNext.setOnClickListener {
            //그래프 이후 상황
            binding.run {
                resBox.visibility = View.GONE
                textAndbox.visibility = View.GONE
                buttonNext.visibility = View.GONE
                buttonSave.visibility = View.GONE
                resLayout.visibility = View.VISIBLE
            }
            //top5 recommendation에 저장함
            val new_list = mutableListOf<Pair<String, Float>>()
            for ((name, cnt) in selectedItemsList) {
                val weight = "${"%.2f".format(cnt.toDouble() / totalDocuments)}".toFloat()
                new_list.add(Pair(name, weight))
            }
            //realtimedatabase로 다시 저장
            val myRef1 = redatabase.getReference("${currentUserUid}")
            myRef1.setValue(new_list)  // 데이터 1개가 계속 수정되는 방식

            //여기서 보내고 확인하고 싶은데
            val myRef = FirebaseDatabase.getInstance().getReference("res/$currentUserUid")

            //val parentLayout = findViewById<LinearLayout>(R.id.res_layout)

            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("mytag", "changed")
                    val value = dataSnapshot.getValue<List<String>>()
                    if (value != null) {
                        var i = 1
                        for (item in value) {
                            val textViewId = resources.getIdentifier("res${i}_txt", "id", packageName)
                            Log.d("mytag", "$textViewId")
                            val textView = findViewById<TextView>(textViewId)
                            textView.text = item
                            Log.d("mytag", "$item")
                            i += 1
                        }
                    }
                    else{
                        Log.d("mytag", "value is null")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("mytag", "error")
                }
            })

            //체크버튼 처리 && 피드백
            binding.res1G.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res1B.isChecked = false
                    // 체크박스 1이 선택됐을 때 할 일
                }
            }

            binding.res1B.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res1G.isChecked = false
                    // 체크박스 2가 선택됐을 때 할 일
                }
            }

            binding.res2G.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res2B.isChecked = false
                    // 체크박스 1이 선택됐을 때 할 일
                }
            }

            binding.res2B.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res2G.isChecked = false
                    // 체크박스 2가 선택됐을 때 할 일
                }
            }

            binding.res3G.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res3B.isChecked = false
                    // 체크박스 1이 선택됐을 때 할 일
                }
            }

            binding.res3B.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res3G.isChecked = false
                    // 체크박스 2가 선택됐을 때 할 일
                }
            }

            binding.res4G.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res4B.isChecked = false
                    // 체크박스 1이 선택됐을 때 할 일
                }
            }

            binding.res4B.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res4G.isChecked = false
                    // 체크박스 2가 선택됐을 때 할 일
                }
            }

            binding.res5G.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res5B.isChecked = false
                    // 체크박스 1이 선택됐을 때 할 일
                }
            }

            binding.res5B.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.res5G.isChecked = false
                    // 체크박스 2가 선택됐을 때 할 일
                }
            }


        }

    }


    private fun drawChart() {
        binding.run {
            resBox.visibility = View.VISIBLE
            buttonNext.visibility = View.VISIBLE
            buttonSave.visibility = View.GONE
            textAndbox.visibility = View.GONE
            resLayout.visibility = View.GONE
        }
        // PieChart 객체 생성
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        // 그래프 데이터 설정
        selectedItemsList = itemAdapter.getSelectedItemsList()
        val entries = mutableListOf<PieEntry>()
        for ((name, cnt) in selectedItemsList) {
            val label = "$name\n(${String.format("%.2f", (cnt.toDouble() / totalDocuments) * 100)}%)"
            entries.add(PieEntry(cnt.toFloat(), label))
        }


        val dataSet = PieDataSet(entries, "Label")
        dataSet.colors = listOf(
            Color.rgb(255, 99, 71),  // 빨강
            Color.rgb(30, 144, 255), // 파랑
            Color.rgb(50, 205, 50),  // 녹색
            Color.rgb(255,255,0),  //노랑
            Color.rgb(230,230,250) //보라
        )
        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.setHoleRadius(0f)
        pieChart.setTransparentCircleAlpha(0)
        dataSet.setDrawValues(false)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setDrawEntryLabels(true)
        pieChart.invalidate()

        // 그래프를 화면에 추가
        val container = findViewById<RelativeLayout>(R.id.resBox)
        container.removeView(pieChart)
        container.addView(pieChart)
    }

    private fun showCancelButtonDialog() {
        val builder = AlertDialog.Builder(this)
        val num = itemAdapter.getSelectedItemCount()

        builder.setTitle("${num}/5 선택")
            .setMessage("5개를 모두 선택해주세요")
            .setPositiveButton("예") { _, _ ->
            }

        val dialog = builder.create()
        dialog.show()
    }
}

// ItemAdapter 클래스 정의
class ItemAdapter(private val itemList: List<Pair<String, Int>>) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private val checkedItems = mutableSetOf<Int>()
    var selectedItemAdapterList = mutableListOf<Pair<String, Int>>()

    fun getSelectedItemCount(): Int {
        return checkedItems.size
    }

    fun getSelectedItemsList() : MutableList<Pair<String, Int>>{
        return selectedItemAdapterList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, count) = itemList[position]
        holder.nameTextView.text = "$name, 관람 횟수: $count"

        // 체크박스 리스너 설정
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 최대 5개까지만 선택 가능하도록 설정
                if (checkedItems.size >= 5) {
                    holder.checkBox.isChecked = false
                } else {
                    checkedItems.add(position)
                    selectedItemAdapterList.add(itemList[position])
                }
            } else {
                checkedItems.remove(position)
                selectedItemAdapterList.remove(itemList[position])
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }


}
