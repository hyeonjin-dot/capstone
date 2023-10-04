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
import com.example.capstone.databinding.ActivityTheater2Binding
import com.example.capstone.databinding.ActivityTheaterBinding
import com.example.capstone.utils.database
import com.google.firebase.auth.FirebaseAuth

class Theater2Activity: AppCompatActivity() {

    lateinit var binding: ActivityTheater2Binding
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var rowlst: ArrayList<Char> = arrayListOf()
    var collst: ArrayList<Int> = arrayListOf()
    val rescollist: ArrayList<Int> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theater2)

        binding = ActivityTheater2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonExit.setOnClickListener {
            finish()
            val intent = Intent(this, AIActivity::class.java)
            startActivity(intent)
        }

        binding.buttonBefore.setOnClickListener {
            finish()
            val intent = Intent(this, TheaterActivity::class.java)
            startActivity(intent)
        }

        val receivedList = intent.getStringArrayListExtra("seatList")
        val linearLayout = binding.reSeat
        if (receivedList != null) {
            // 리스트가 null이 아니라면 여기서 처리할 수 있습니다.
            for (item in receivedList) {
                rowlst.add(item[0])
                collst.add(item.substring(1).toInt())

                val textView = TextView(this)
                textView.text = item
                textView.textSize = 20f
                linearLayout.addView(textView)
            }
        }
        else{
            Log.d("mytag", "nothing")
        }

        findbestcolseat() //col고르기
        val row = findbestrowseat()

        val linearLayout2 = binding.recSeat
        for (i in rescollist){
            val item = row + i.toString()
            Log.d("mytag", "rec seat $item")
            val textView = TextView(this)
            textView.text = item
            textView.textSize = 20f
            linearLayout2.addView(textView)
        }

    }

    fun findbestrowseat(): Char {
        val elementCount = mutableMapOf<Char, Int>()

        for (element in rowlst) {
            if (elementCount.containsKey(element)) {
                elementCount[element] = elementCount[element]!! + 1
            } else {
                elementCount[element] = 1
            }
        }

        return elementCount.maxByOrNull { it.value }!!.key
    }


    fun findbestcolseat() {
        val counters = mutableMapOf(
            "left" to 0, "mid" to 0, "rig" to 0,
            "side" to 0, "cen" to 0,
            "ss" to 0, "ls" to 0,
            "cc" to 0, "lc" to 0
        )

        for (i in collst) {
            when (i) {
                in 1..7 -> {
                    counters["left"] = counters["left"]!! + 1
                    when (i) {
                        in listOf(1, 2, 6, 7) -> counters["side"] = counters["side"]!! + 1
                        in listOf(3, 4, 5) -> counters["cen"] = counters["cen"]!! + 1
                    }
                }
                in 8..15 -> {
                    counters["mid"] = counters["mid"]!! + 1
                    when (i) {
                        in listOf(8, 9, 14, 15) -> counters["side"] = counters["side"]!! + 1
                        in listOf(10, 11, 12, 13) -> counters["cen"] = counters["cen"]!! + 1
                    }
                }
                else -> {
                    counters["rig"] = counters["rig"]!! + 1
                    when (i) {
                        in listOf(16, 22) -> counters["side"] = counters["side"]!! + 1
                        in listOf(17, 21) -> counters["cen"] = counters["cen"]!! + 1
                    }
                }
            }
        }

        val max = counters.maxByOrNull { it.value }?.key

        val pairs = if (max == "side") {
            listOf(
                listOf(1, 7) to listOf(2, 6),
                listOf(8, 15) to listOf(9, 14),
                listOf(16, 22) to listOf(17, 21)
            )
        } else {
            listOf(
                listOf(3, 5) to listOf(4),
                listOf(10, 13) to listOf(11, 12),
                listOf(18, 20) to listOf(19)
            )
        }

        val chosenList = pairs.maxByOrNull { (large, small) ->
            val largeCount = large.count { it in collst }
            val smallCount = small.count { it in collst }
            maxOf(largeCount, smallCount)
        }

        if (chosenList != null) {
            rescollist.addAll(chosenList.first)
        }
    }



    fun findMax(a: Int, b: Int, c: Int): Int {
        var max = a
        if (b > max) {
            max = b
        }
        if (c > max) {
            max = c
        }
        return max
    }
}
