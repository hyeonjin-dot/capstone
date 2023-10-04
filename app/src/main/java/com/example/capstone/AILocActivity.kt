package com.example.capstone

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.capstone.databinding.ActivityAilocBinding
import net.daum.mf.map.api.MapView


class AILocActivity: AppCompatActivity() {

    lateinit var binding: ActivityAilocBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAilocBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCancel.setOnClickListener {
            finish()
            val intent = Intent(this, AIActivity::class.java)
            startActivity(intent)
        }




//        binding.mapView.start(object : MapLifeCycleCallback() {
//            override fun onMapDestroy() {
//                // 지도 API 가 정상적으로 종료될 때 호출됨
//            }
//
//            override fun onMapError(error: Exception) {
//                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
//            }
//        }, object : KakaoMapReadyCallback() {
//            override fun onMapReady(kakaoMap: KakaoMap) {
//                // 인증 후 API 가 정상적으로 실행될 때 호출됨
//            }
//        })

        val view = binding.root
        setContentView(view)

        val mapView = MapView(this)
        binding.clKakaoMapView.addView(mapView)
    }



}
