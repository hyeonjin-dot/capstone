package com.example.capstone

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.installations.Utils
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.android.Utils as Utils2
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.ScrollView
import org.opencv.core.CvType.CV_8U
import java.io.*

class InfoPicActivity : AppCompatActivity() {
    private var image: Bitmap? = null
    private var mTess: TessBaseAPI? = null
    private var datapath = ""
    private var intent : Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infopic)


        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        intent = Intent(this, InfoPic2Activity::class.java)

        buttonCancel.setOnClickListener {
            // 화면을 닫음
            finish()
            val intent2 = Intent(this, AddInfoActivity::class.java)
            startActivity(intent2)

        }
        //정보 넘기기 준비해야함
        buttonSave.setOnClickListener {
            finish()
            startActivity(intent)
        }

        // 이미지 디코딩을 위한 초기화
        //image = BitmapFactory.decodeResource(resources, R.drawable.img)
        image = changecolorWithBitmap(BitmapFactory.decodeResource(resources, R.drawable.img)) // 샘플 이미지 파일
        val imageView: ImageView = findViewById(R.id.imageView)
        imageView.setImageResource(R.drawable.img) //원래 이미지로 나타남

        // 언어 파일 경로
        datapath = filesDir.toString() + "/tesseract/"


        // 트레이닝 데이터가 카피되어 있는지 체크
        checkFile(File(datapath + "tessdata/"))

        // Tesseract API 언어 세팅
        val lang = "kor+eng"

        // OCR 세팅
        mTess = TessBaseAPI()
        mTess!!.init(datapath, lang)
    }

    //img에서 text읽기
    fun processImage(view: View?) {
        Log.d("mytag", "in processImage")
        var OCRresult: String? = null
        mTess!!.setImage(image)
        OCRresult = mTess!!.utF8Text

        processOCRResult(OCRresult)
    }

    fun processOCRResult(result: String) {
        val textLines = result.split("\n").filter { it.isNotBlank() }.toTypedArray()

        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)
        var num = 1

        //ㅇㅕ기서 버튼 생성
        for (text in textLines) {
            Log.d("mytag", "in processOCRResult")
            val button = Button(this)
            button.text = text

            // 버튼 클릭 리스너 설정
            button.setOnClickListener {
                intent?.putExtra(num.toString(), text)
                num++
            }

            // LinearLayout에 버튼을 추가
            buttonContainer.addView(button)
        }
    }


    // 언어 파일 이름
    private val langFileNames = listOf("kor.traineddata", "eng.traineddata")

    /**
     * 언어 데이터 파일, 디바이스에 복사
     */
    private fun copyFiles() {
        try {
            val assetManager = this.assets
            for (langFileName in langFileNames) {
                val instream: InputStream = assetManager.open("tesseract/tessdata/$langFileName")
                val outstream: OutputStream = FileOutputStream("$filesDir/tesseract/tessdata/$langFileName")
                val buffer = ByteArray(4096)
                var read: Int
                while (instream.read(buffer).also { read = it } != -1) {
                    outstream.write(buffer, 0, read)
                }
                outstream.flush()
                outstream.close()
                instream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkFile(dir: File) {
        // 디렉토리가 없으면 디렉토리를 만들고 그 후에 파일을 카피
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles()
        }
        // 디렉토리가 있지만 파일이 없으면 파일 카피 진행
        if (dir.exists()) {
            for (langFileName in langFileNames) {
                val datafilepath = datapath + "tessdata/$langFileName"
                val datafile = File(datafilepath)
                if (!datafile.exists()) {
                    copyFiles()
                }
            }
        }
    }

    //색 반전
    fun invertColors(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val invertedBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = bitmap.getPixel(x, y)
                val red = 255 - Color.red(color)
                val green = 255 - Color.green(color)
                val blue = 255 - Color.blue(color)
                val invertedColor = Color.argb(Color.alpha(color), red, green, blue)
                invertedBitmap.setPixel(x, y, invertedColor)
            }
        }
        return invertedBitmap
    }

    private fun changecolorWithBitmap(inputBitmap: Bitmap):Bitmap {
        // OpenCV 초기화
    //    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        System.loadLibrary("opencv_java4");

        // Bitmap을 Mat으로 변환
        var resbitmap = invertColors(inputBitmap)//색 반전
        val imageMat = Mat(resbitmap.height, resbitmap.width, CvType.CV_8UC4)
        Utils2.bitmapToMat(resbitmap, imageMat)

        // BGR에서 HSV로 변환
        val imageHsv = Mat()
        Imgproc.cvtColor(imageMat, imageHsv, Imgproc.COLOR_BGR2HSV)

        // 값의 범위를 정의하여 마스크 생성
        val lowerWhite = Scalar(0.0, 0.0, 100.0)
        val upperWhite = Scalar(255.0, 100.0, 255.0)

        val mask = Mat()
        Core.inRange(imageHsv, lowerWhite, upperWhite, mask)

        // 이미지에 마스크 적용
        val imageBgrMasked = Mat()
        Core.bitwise_and(imageMat, imageMat, imageBgrMasked, mask)

        // BGR에서 RGB로 변환
        val imageRgb = Mat()
        Imgproc.cvtColor(imageBgrMasked, imageRgb, Imgproc.COLOR_BGR2RGB)

        // Mat을 Bitmap으로 변환
        val outputBitmap = Bitmap.createBitmap(imageRgb.cols(), imageRgb.rows(), Bitmap.Config.ARGB_8888)
        Utils2.matToBitmap(imageRgb, outputBitmap)

        return outputBitmap
    }
}
