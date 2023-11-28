package com.example.bugimarket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bugimarket.DBKey.Companion.DB_ARTICLES
import com.example.bugimarket.DBKey.Companion.DB_USERS
import com.example.bugimarket.databinding.HomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class HomeFragment : Fragment(R.layout.home) {

    private var binding: HomeBinding? = null
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference
    private val articleList = mutableListOf<ArticleModel>()

    //판매 상태에 따라 게시물 필터링하는 메서드
    fun filterArticlesBySaleStatus(onSale: Boolean) {
        val filteredList = articleList.filter { article ->
            when (onSale) {
                true -> {
                    article.status == "ONSALE"
                }
                false ->{
                    article.status == "SOLDOUT"
                }
            }
        }
        articleAdapter.submitList(filteredList)
    }

    //데이터베이스 ChildEventListener 인터페이스를 구현한 리스너 객체
    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return
            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java) ?: return
            val index = articleList.indexOfFirst { it.chatKey == articleModel.chatKey }
            if (index > -1) {
                articleList[index] = articleModel
                articleAdapter.notifyItemChanged(index)
            }
        }


        override fun onChildRemoved(snapshot: DataSnapshot) {
            // 필요한 경우 삭제 이벤트 처리

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            // 필요한 경우 이동 이벤트 처리
        }

        override fun onCancelled(error: DatabaseError) {
            // 필요한 경우 취소 이벤트 처리
        }
    }

    //FirebaseAuth 인스턴스를 저장하는 프로퍼티
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    //화면이 생성될 때 호출되는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ViewBinding을 사용하여 레이아웃 바인딩
        val fragmentHomeBinding = HomeBinding.bind(view)
        binding = fragmentHomeBinding

        //ArticleAdapter 인스턴스 및 데이터 초기화
        articleList.clear()

        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        userDB = Firebase.database.reference.child(DB_USERS)
        articleAdapter = ArticleAdapter(onItemClicked = { articleModel, action ->
            when (action) {
                ArticleAdapter.Action.CLICK -> {
                    if (auth.currentUser != null) {
                        if (auth.currentUser!!.uid != articleModel.sellerId) {
                            // 채팅방으로 이동하는 로직 추가
                        } else {
                            Snackbar.make(view, "내가 올린 아이템입니다.", Snackbar.LENGTH_LONG).show()
                        }
                    } else {
                        Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
                    }
                }
                ArticleAdapter.Action.DELETE -> {
                    // 삭제 옵션이 선택되었을 때 deleteArticle 메서드 호출
                    deleteArticle(articleModel)
                }
            }
        })


        //RecyclerView에 LinearLayoutManager 및 Adapter 설정
        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        //글 작성 버튼에 대한 클릭 리스너 설정
        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(requireContext(), AddArticleActivity::class.java)
                startActivity(intent)
            } else {
                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }
        }

        //필터링 버튼에 대한 클릭 리스너 설정
        fragmentHomeBinding.filteringButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.filtering_menu, popupMenu.menu)

            //팝업 메뉴 아이템 클릭 시 동작 정의
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_item_onsale -> {
                        filterArticlesBySaleStatus(true)
                        true
                    }
                    R.id.menu_item_soldout -> {
                        filterArticlesBySaleStatus(false)
                        true
                    }
                    R.id.menu_item_all -> {
                        articleAdapter.submitList(articleList)
                        true
                    }
                    else -> false
                }
            }

            //팝업 메뉴 표시
            popupMenu.show()
        }

        //데이터베이스에 ChildEventListener 등록
        articleDB.addChildEventListener(listener)
    }

    //화면이 다시 시작할 때 호출되는 메서드
    override fun onResume() {
        super.onResume()
        articleAdapter.notifyDataSetChanged()
    }

    //화면이 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        //데이터베이스의 ChildEventListener 제거
        articleDB.removeEventListener(listener)
    }

    private fun deleteArticle(articleModel: ArticleModel) {
        // 로컬 목록에서 게시물 제거
        val index = articleList.indexOfFirst { it.chatKey == articleModel.chatKey }
        if (index > -1) {
            articleList.removeAt(index)
            articleAdapter.notifyItemRemoved(index)
        }

        // Firebase Realtime Database에서 게시물 제거
        articleDB.child(articleModel.chatKey ?: "").removeValue()
    }


}