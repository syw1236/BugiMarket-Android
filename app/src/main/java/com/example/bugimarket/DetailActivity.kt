package com.example.bugimarket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bugimarket.databinding.ActivityDetailBinding
import com.google.firebase.auth.FirebaseAuth

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isSeller = intent.getStringExtra("sellerId")
        val title = intent.getStringExtra("title")
        val price = intent.getStringExtra("price")
        val description = intent.getStringExtra("description")
        val imageUrl = intent.getStringExtra("imageUrl")
        val status = intent.getStringExtra("status")

        // 로그인한 사용자의 Uid를 가져옴
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // 로그인한 사용자가 아이템을 올린 사용자와 동일한 경우에만 'edit_button'을 보여줌
        if (currentUserId == isSeller) {
            binding.editButton.visibility = View.VISIBLE

            binding.editButton.setOnClickListener {
                val editIntent = Intent(this, EditArticleActivity::class.java)
                editIntent.putExtra("articleId", this.intent.getStringExtra("chatKey"))
                startActivityForResult(editIntent, REQUEST_EDIT)
            }

        } else {
            binding.editButton.visibility = View.GONE
        }

        binding.sellerTextView.text = isSeller
        binding.detailTitle.text = title
        binding.detailPrice.text = "${price}원"
        binding.detailDescription.text = description

        if (imageUrl != null) {
            Glide.with(binding.detailImage)
                .load(imageUrl)
                .into(binding.detailImage)
        }

        if (status == "ONSALE") {
            binding.detailStatus.text = "판매중"
            binding.detailStatus.background = ContextCompat.getDrawable(this, R.drawable.rounded_red)
        } else {
            binding.detailStatus.text = "판매완료"
            binding.detailStatus.background = ContextCompat.getDrawable(this, R.drawable.rounded_gray)
        }

        // 채팅 버튼 리스너 추가
        binding.chatButton.setOnClickListener {
//            val chatIntent = Intent(this, ChatActivity::class.java)
//            startActivity(chatIntent)
        }


        binding.backButton.setOnClickListener {
            finish()
        }
    }

    // onActivityResult에서 결과를 받아서 UI 업데이트
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EDIT && resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra("title")
            val price = data?.getStringExtra("price")
            val description = data?.getStringExtra("description")
            val imageUrl = data?.getStringExtra("imageUrl")
            val status = data?.getStringExtra("status")

            binding.detailTitle.text = title
            binding.detailPrice.text = "${price}원"
            binding.detailDescription.text = description

            if (imageUrl != null) {
                Glide.with(binding.detailImage)
                    .load(imageUrl)
                    .into(binding.detailImage)
            }

            if (status == Status.ONSALE.name) {
                binding.detailStatus.text = "판매중"
                binding.detailStatus.background = ContextCompat.getDrawable(this, R.drawable.rounded_red)
            } else {
                binding.detailStatus.text = "판매완료"
                binding.detailStatus.background = ContextCompat.getDrawable(this, R.drawable.rounded_gray)
            }
        }
    }

    companion object {
        const val REQUEST_EDIT = 1001
    }
}