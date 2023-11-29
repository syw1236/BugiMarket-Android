package com.example.bugimarket

import ChatListItem
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bugimarket.DBKey.Companion.CHILD_CHAT
import com.example.bugimarket.databinding.ActivityDetailBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var userDB: DatabaseReference

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
        binding.chatButton.setOnClickListener { //채팅하기 버튼을 클릭하면 채팅방이 생성되고 해당 채팅방으로 이동하게 된다.
            if (status == "ONSALE") { // 게시글 상태가 판매중일 때 채탕하기 기능 활성화
                userDB = Firebase.database.reference.child(DBKey.DB_USERS)
                if (currentUserId != null) {
                    if (currentUserId != isSeller) {

                        val foreignKey = "$currentUserId-$isSeller-$title"

                        // 이미 생성된 채팅방인지 체크
                        userDB.child(currentUserId)
                            .child(CHILD_CHAT)
                            .orderByChild("foreignkey")
                            .equalTo(foreignKey)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        // 이미 생성된 채팅방이면 해당 채팅방으로 이동
//                                        val chatRoomKey = snapshot.children.first().key
                                        val chatIntent = Intent(this@DetailActivity, ChatRoomActivity::class.java)
                                        chatIntent.putExtra("chatKey", foreignKey)
                                        startActivity(chatIntent)
                                    } else {
                                        // 채팅방 생성
                                        val chatRoom = ChatListItem(
                                            buyerId = currentUserId,
                                            sellerId = isSeller ?: "",
                                            title = title ?: "",
                                            foreignkey = foreignKey
                                        )
                                        userDB.child(currentUserId)
                                            .child(CHILD_CHAT)
                                            .push()
                                            .setValue(chatRoom)

                                        userDB.child(isSeller ?: "")
                                            .child(CHILD_CHAT)
                                            .push()
                                            .setValue(chatRoom)

                                        // 채팅방으로 이동
                                        val chatIntent = Intent(this@DetailActivity, ChatRoomActivity::class.java)
                                        chatIntent.putExtra("chatKey", chatRoom.foreignkey)
                                        startActivity(chatIntent)
                                        Toast.makeText(
                                            this@DetailActivity,
                                            "채팅방이 생성되었습니다. 예의를 지켜 채팅해주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // 처리 중 오류가 발생한 경우
                                    Toast.makeText(
                                        this@DetailActivity,
                                        "오류가 발생했습니다. 다시 시도해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                    else {
                        Toast.makeText(this, "자신의 게시물에서는 채팅을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else { // 게시글 상태가 판매완료일 때
                if(currentUserId == isSeller){ //판매자와 현재 로그인한 사람이 같으면
                    Toast.makeText(this, "자신의 게시물에서는 채팅을 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "이미 판매가 완료된 상품입니다.", Toast.LENGTH_SHORT).show()
                }
            }
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