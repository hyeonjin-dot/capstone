package com.example.capstone

import com.google.firebase.auth.FirebaseAuth
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

object utils {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var database = MyApplication.db


    fun getUserDocId(onSuccess: (String) -> Unit) {
        if (auth.currentUser != null) {
            val usersCollection = database.collection("users")
            val query = usersCollection.whereEqualTo("email", currentUser?.email)
            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.first() // 이메일과 일치하는 첫 번째 문서 가져오기
                        val docId = userDoc.id
                        onSuccess(docId)
                    }
                }
        }
    }


    fun changecolor(args: Array<String>) {
        // OpenCV 초기화
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

        // 이미지 로드
        val imageBgr = Imgcodecs.imread("./data/images/plane_256x256.jpg")

        // BGR에서 HSV로 변환
        val imageHsv = Mat()
        Imgproc.cvtColor(imageBgr, imageHsv, Imgproc.COLOR_BGR2HSV)

        // 파랑 값의 범위를 정의하여 마스크 생성
        val lowerRed = Scalar(146.0, 20.0, 35.0)
        val upperRed = Scalar(246.0, 189.0, 165.0)
        val mask = Mat()
        Core.inRange(imageHsv, lowerRed, upperRed, mask)

        // 이미지에 마스크 적용
        val imageBgrMasked = Mat()
        Core.bitwise_and(imageBgr, imageBgr, imageBgrMasked, mask)

        // BGR에서 RGB로 변환
        val imageRgb = Mat()
        Imgproc.cvtColor(imageBgrMasked, imageRgb, Imgproc.COLOR_BGR2RGB)

        // 이미지 출력 (출력 코드는 Android에서 사용하는 것과는 다를 수 있음)
        // OpenCV에서 이미지 출력은 Android와 다른 방식을 사용합니다.
        // Android에서는 Bitmap 및 ImageView를 사용하여 이미지를 출력하는 것이 일반적입니다.

        // 마스크를 그레이 스케일로 변환하여 출력
        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2BGR)

        // 이미지 및 마스크 출력
        Imgcodecs.imwrite("output_image.jpg", imageRgb)
        Imgcodecs.imwrite("output_mask.jpg", mask)
    }


    //Utils.getUserDocumentId { docId ->
    //    // documentId를 이용한 작업 수행
    //}ㅡ
}