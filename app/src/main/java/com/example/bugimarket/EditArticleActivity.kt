package com.example.bugimarket

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.bugimarket.DBKey.Companion.DB_ARTICLES
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class EditArticleActivity : AppCompatActivity() {

    private var selectedUri: Uri? = null
    private var status: Status = Status.ONSALE
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val storage: FirebaseStorage by lazy { Firebase.storage }
    private val articleDB: DatabaseReference by lazy { Firebase.database.reference.child(DB_ARTICLES) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_article)

        val chatKey = intent.getStringExtra("articleId") ?: return finish()
        val articleReference = articleDB.child(chatKey)

        // 기존 데이터 가져오기
        articleReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val article = snapshot.getValue(ArticleModel::class.java)
                findViewById<EditText>(R.id.titleEditText).setText(article?.title.orEmpty())
                findViewById<EditText>(R.id.priceEditText).setText(article?.price.orEmpty())
                findViewById<EditText>(R.id.detailDescriptionTextView).setText(article?.description.orEmpty())

                // 이미지 뷰에 이미지 설정
                val imageUrl = article?.imageUrl.orEmpty()
                if (imageUrl.isNotEmpty()) {
                    Glide.with(this@EditArticleActivity)
                        .load(imageUrl)
                        .into(findViewById<ImageView>(R.id.photoImageView))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditArticleActivity, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<Button>(R.id.salesButton).setOnClickListener {
            status = Status.ONSALE
            Toast.makeText(this@EditArticleActivity, "판매중으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.salesCompleteButton).setOnClickListener {
            status = Status.SOLDOUT
            Toast.makeText(this@EditArticleActivity, "판매완료로 변경되었습니다.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val title = findViewById<EditText>(R.id.titleEditText).text.toString().orEmpty()
            val price = findViewById<EditText>(R.id.priceEditText).text.toString().orEmpty()
            val description = findViewById<EditText>(R.id.detailDescriptionTextView).text.toString().orEmpty()

            showProgress()

            articleReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val article = snapshot.getValue(ArticleModel::class.java)
                    val imageUrl = article?.imageUrl.orEmpty()
                    updateArticle(articleReference, title, price, imageUrl, description, status)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditArticleActivity, "이미지 URL을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    hideProgress()
                }
            })
        }
    }

    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.reference.child("article/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    private fun updateArticle(articleReference: DatabaseReference, title: String, price: String, imageUrl: String, description: String, status: Status) {
        val model = ArticleModel(auth.currentUser?.uid.orEmpty(), title, System.currentTimeMillis(), price, imageUrl, status.toString(), description, articleReference.key.orEmpty())
        articleReference.setValue(model)

        // Intent에 수정된 데이터를 담아서 setResult()로 결과를 설정
        val resultIntent = Intent().apply {
            putExtra("title", title)
            putExtra("price", price)
            putExtra("description", description)
            putExtra("imageUrl", imageUrl)
            putExtra("status", status.name)
        }
        setResult(Activity.RESULT_OK, resultIntent)

        hideProgress()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1010 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2020)
    }

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

    private fun showProgress() {
        findViewById<ProgressBar>(R.id.progressBar).isVisible = true
    }

    private fun hideProgress() {
        findViewById<ProgressBar>(R.id.progressBar).isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            2020 -> {
                val uri = data?.data
                if (uri != null) {
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
