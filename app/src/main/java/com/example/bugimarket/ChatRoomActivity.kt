package com.example.bugimarket

import ChatItem
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bugimarket.DBKey.Companion.DB_CHATS
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class ChatRoomActivity : AppCompatActivity() {

    // FirebaseAuth 인스턴스를 지연 초기화로 생성
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    // Firebase Realtime Database에 대한 참조 변수
    private var chatDB: DatabaseReference? = null

    // 채팅 아이템을 담을 리스트 및 어댑터
    private val chatList = mutableListOf<ChatItem>()
    private val adapter = ChatItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_room)

        // Intent로부터 채팅 키 가져오기
        val foreignkey = intent.getLongExtra("chatKey", -1)
        Log.d("ChatRoomActivity", "chatKey = $foreignkey")

        // RecyclerView에 어댑터 및 레이아웃 매니저 설정
        findViewById<RecyclerView>(R.id.chatRecyclerView).adapter = adapter
        findViewById<RecyclerView>(R.id.chatRecyclerView).layoutManager = LinearLayoutManager(this)

        // Firebase Realtime Database의 특정 채팅 키에 대한 참조 설정
        chatDB = Firebase.database.reference.child(DB_CHATS).child("$foreignkey")

        // 채팅 데이터의 변화를 감지하는 ChildEventListener 설정
        chatDB!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // 데이터베이스에 추가된 채팅 아이템을 가져와 리스트에 추가
                val chatItem = snapshot.getValue(ChatItem::class.java)
                chatItem ?: return
                Log.d("ChatRoomActivity", "${chatItem.message}, ${chatItem.senderId}")
                chatList.add(chatItem)

                // 어댑터에 리스트 제출 및 데이터 변경 알림
                adapter.submitList(chatList)
                adapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })

        // 전송 버튼 클릭 시 동작
        findViewById<Button>(R.id.sendButton).setOnClickListener {
            // 현재 사용자의 UID와 입력된 메시지를 이용하여 ChatItem 객체 생성
            val chatItem = ChatItem(
                senderId = auth.currentUser!!.uid,
                message = findViewById<EditText>(R.id.messageEditText).text.toString()
            )

//            // 채팅 데이터베이스에 새로운 채팅 아이템 추가
//            chatDB!!.push().setValue(chatItem)

            // 채팅 데이터베이스에 새로운 채팅 아이템 추가
            chatDB!!.push().setValue(chatItem)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        findViewById<EditText>(R.id.messageEditText).setText("") //보낸 후 입력창을 초기화 시킴
                        //마지막으로 보낸 문자가 chatRoom의 제목 아래에 표시되어야함

                        
                    } else {
                        // setValue 작업이 실패한 경우 처리
                        // 예를 들어, 사용자에게 알림을 표시할 수 있습니다.
                    }
                }
        }
    }
}
