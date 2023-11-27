package com.example.bugimarket

import ChatListItem
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bugimarket.DBKey.Companion.CHILD_CHAT
import com.example.bugimarket.DBKey.Companion.DB_USERS
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.example.bugimarket.databinding.ChatListFragmentBinding

class ChatListFragment : Fragment(R.layout.chat_list_fragment) {

    private var binding: ChatListFragmentBinding? = null
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatRoomList = mutableListOf<ChatListItem>()

    // FirebaseAuth 인스턴스를 지연 초기화로 생성
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 바인딩 설정
        val fragmentChatlistBinding = ChatListFragmentBinding.bind(view)
        binding = fragmentChatlistBinding

        // 어댑터 초기화
        chatListAdapter = ChatListAdapter(onItemClicked = { chatRoom ->
            context?.let {
                // 채팅방 항목을 클릭하면 ChatRoomActivity로 이동
                val intent = Intent(it, ChatRoomActivity::class.java)
                intent.putExtra("chatKey", chatRoom.foreignkey)

                startActivity(intent)
            }
        })

        // 채팅방 리스트 초기화
        chatRoomList.clear()

        // RecyclerView 설정
        fragmentChatlistBinding.chatRecyclerView.adapter = chatListAdapter
        fragmentChatlistBinding.chatRecyclerView.layoutManager = LinearLayoutManager(context)

        // 사용자가 인증되어 있는지 확인
        if (auth.currentUser == null) {
            return
        }

        // Firebase Realtime Database에서 채팅 데이터 가져오기 //목록들을 가져옴
        val chatDB =
            Firebase.database.reference.child(DB_USERS).child(auth.currentUser!!.uid)
                .child(
                    CHILD_CHAT
                )
        //로그인한 사용자가 참여한 채팅방 목록을 최소 한 번 불러오기 위해 해당 리스터를 달음
        chatDB.addListenerForSingleValueEvent(object : ValueEventListener { //채팅방 리스트가 추가될 때 마다 아이템을 불러오기 위해 ValueEventListener를 달음
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    // DataSnapshot을 ChatListItem으로 변환
                    val model = it.getValue(ChatListItem::class.java)
                    model ?: return

                    // ChatListItem을 리스트에 추가
                    chatRoomList.add(model)
                }

                // 어댑터에 리스트 제출 및 데이터 변경 알림
                chatListAdapter.submitList(chatRoomList)
                chatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // 필요시 onCancelled 이벤트 처리
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // Resume 시 어댑터에 데이터 변경 알림
        chatListAdapter.notifyDataSetChanged()
    }
}
