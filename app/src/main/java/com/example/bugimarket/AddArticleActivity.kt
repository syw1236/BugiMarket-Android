package com.example.bugimarket

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.bugimarket.DBKey.Companion.DB_ARTICLES
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

//글 추가를 위한 액티비티
class AddArticleActivity : AppCompatActivity() {

    //선택된 이미지 URI
    private var selectedUri: Uri? = null

    //Firebase Authentication, Storage 및 Database를 위한 지연 초기화
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_ARTICLES)
    }

    //글 설명을 위한 지연 초기화
    private val description: String by lazy {
        findViewById<EditText>(R.id.detailDescriptionTextView).text.toString().orEmpty()
    }

    //활동이 생성될 때 호출되는 메서드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_article)

        //이미지 추가 버튼에 대한 클릭 리스너 설정
        findViewById<Button>(R.id.imageAddButton).setOnClickListener {
            when {
                //미디어 이미지 읽기 권한이 부여된 경우
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }

                //사용자가 이전에 권한을 거부한 경우
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES) -> {
                    showPermissionContextPopup()
                }

                //권한이 부여되지 않은 경우 권한 요청
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                        1010
                    )
                }

            }
        }


        //제출 버튼에 대한 클릭 리스너 설정
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            //입력 필드에서 글 세부 정보 가져오기
            val title = findViewById<EditText>(R.id.titleEditText).text.toString().orEmpty()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString().orEmpty()
            val sellerId = auth.currentUser?.uid.orEmpty()
            val status = Status.ONSALE


            //처리 중에 진행률 표시줄 표시
            showProgress()

            //이미지가 선택된 경우
            if (selectedUri != null) {

                //사진을 업로드한 다음 글을 업로드
                val PhotoUri = selectedUri ?: return@setOnClickListener
                uploadPhoto(
                    PhotoUri,
                    successHandler = { uri ->
                        uploadArticle(sellerId, title, price, uri, description)
                    },
                    errorHandler = {
                        Toast.makeText(this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()

                        hideProgress()

                    }
                )
            } else {
                //이미지가 선택되지 않은 경우 이미지 없이 글 업로드
                uploadArticle(sellerId, title, price, "", description)

            }

        }

    }


    //Firebase Storage에 사진 업로드 메서드
    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {

        val fileName = "${System.currentTimeMillis()}.png"

        //사진을 저장소에 업로드
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    //성공하면 다운로드 URL을 가져와서 성공 핸들러 호출
                    storage.reference.child("article/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    //실패하면 에러 핸들러 호출
                    errorHandler()
                }
            }
    }


    //Firebase Database에 글 업로드 메서드
    private fun uploadArticle(sellerId: String, title: String, price: String, imageUrl: String, description: String) {

        //데이터베이스에 새 글 참조 생성
        val newArticleReference = articleDB.push()
        val chatKey = newArticleReference.key ?: throw Exception("Could not get chatKey.")

        //ArticleModel 객체 생성
        val model = ArticleModel(sellerId, title, System.currentTimeMillis(), price, imageUrl, "ONSALE", description, chatKey)

        //데이터베이스의 새 글 값 설정
        newArticleReference.setValue(model)

        //진행률 표시줄 숨기고 활동 종료
        hideProgress()
        finish()
    }

    //권한 요청 결과를 받았을 때 호출되는 메소드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //권한 요청 결과 확인
        when (requestCode) {
            1010 ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //콘텐트 프로바이더를 시작하는 메서드
    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2020)
    }

    // 권한 컨텍스트 팝업을 표시하는 메서드
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1010
                )
            }
            .create()
            .show()

    }


    //진행률 표시줄을 표시하는 메서드
    private fun showProgress(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible = true

    }

    //진행률 표시줄을 숨기는 메서드
    private fun hideProgress(){
        findViewById<ProgressBar>(R.id.progressBar).isVisible = false
    }

    //활동 결과를 수신했을 때 호출되는 메서드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //결과 코드 및 요청 코드 확인
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        //요청 코드에 따라 결과 처리
        when (requestCode) {
            2020 -> {
                val uri = data?.data
                if (uri != null) {
                    //선택한 이미지 URI 설정 및 이미지 표시
                    findViewById<ImageView>(R.id.photoImageView).setImageURI(uri)
                    selectedUri = uri
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}