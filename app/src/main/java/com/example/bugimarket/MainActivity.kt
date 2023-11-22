package com.example.bugimarket

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductPostAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase 초기화
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().reference.child("productPosts")

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductPostAdapter(ArrayList()) // 여러분의 데이터 모델에 맞게 수정
        recyclerView.adapter = adapter

        // 상품 게시물 등록 버튼
        writePostButton.setOnClickListener {
            startActivity(Intent(this, WriteProductPost::class.java))
        }

        // 데이터베이스에서 상품 게시물을 실시간으로 감지하여 업데이트
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = mutableListOf<Product>() // 우리의 데이터 모델에 맞게 수정
                for (postSnapshot in dataSnapshot.children) {
                    val product = postSnapshot.getValue(Product::class.java) // 우리의 데이터 모델에 맞게 수정
                    if (product != null) {
                        productList.add(product)
                    }
                }
                adapter.updateData(productList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 읽기 실패 시 처리
            }
        })

        // RecyclerView 아이템 롱 클릭 리스너 - 판매글 수정
        adapter.setOnItemLongClickListener(object : ProductPostAdapter.OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {
                val selectedProduct = adapter.getItem(position)
                if (selectedProduct.userId == auth.currentUser?.uid) {
                    // 현재 사용자가 작성한 글이면 수정 화면으로 이동
                    val intent = Intent(this@MainActivity, ModifyProductPost::class.java)
                    intent.putExtra("productId", selectedProduct.productId)
                    startActivity(intent)
                }
            }
        })

        // RecyclerView 아이템 클릭 리스너 - 판매글 보기
        adapter.setOnItemClickListener(object : ProductPostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val selectedProduct = adapter.getItem(position)
                // 상세 화면으로 이동
                val intent = Intent(this@MainActivity, ShowProductPost::class.java)
                intent.putExtra("productId", selectedProduct.productId)
                startActivity(intent)
            }
        })

        // 로그아웃 버튼

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 홈 버튼
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            //  메인 액티비티를 다시 실행
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 채팅창 이동 버튼
        val chatButton = findViewById<ImageButton>(R.id.chatButton)
        chatButton.setOnClickListener {
            startActivity(Intent(this, ChattingList::class.java))
        }
    }

    // onCreateOptionsMenu 함수를 추가하여 메뉴를 생성하고 필터링 기능을 구현.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // onOptionsItemSelected 함수를 추가하여 메뉴 아이템이 선택되었을 때의 동작을 정의.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filterMenu -> {
                // AlertDialog를 이용한 필터링 기능을 추가.
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        val items = arrayOf("전체", "판매중", "판매완료")
        val checkedItem = 0 // 기본적으로 전체가 선택되도록 설정

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("판매여부 필터링")
            .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                // 여기에 선택된 아이템에 따라 필터링을 수행하는 코드를 추가
                // which 변수에 선택된 아이템의 인덱스가 전달된다.
            }

        val dialog = builder.create()
        dialog.show()
    }
}
