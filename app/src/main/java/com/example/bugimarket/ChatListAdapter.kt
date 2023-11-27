package com.example.bugimarket

import ChatListItem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bugimarket.databinding.ChatListItemBinding

// ChatListItem 데이터를 사용하여 RecyclerView를 구성하는 어댑터 클래스
class ChatListAdapter(val onItemClicked: (ChatListItem) -> Unit) : ListAdapter<ChatListItem, ChatListAdapter.ViewHolder>(diffUtil) {

    // 뷰홀더 클래스
    inner class ViewHolder(private val binding: ChatListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // 데이터를 뷰에 바인딩하는 함수
        fun bind(chatListItem: ChatListItem) {
            // 아이템이 클릭되었을 때 실행할 동작 설정
            binding.root.setOnClickListener {
                // 클릭 이벤트를 정의한 람다 함수 호출
                onItemClicked(chatListItem)
            }

            // 뷰에 데이터를 설정
            binding.chatRoomTitleTextView.text = chatListItem.title //채팅방 이름을 게시글의 title로 설정
            binding.sendId.text = "채팅방 생성자: "+chatListItem.sellerId //채팅을 보낸 사람의 id가 나오도록 설정
        }
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // ChatListItemBinding을 사용하여 레이아웃을 inflate하고 뷰홀더를 생성
        return ViewHolder(ChatListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // 뷰홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 현재 아이템을 뷰홀더에 바인딩
        holder.bind(currentList[position])
    }

    companion object {
        // DiffUtil을 사용하여 아이템 비교
        val diffUtil = object : DiffUtil.ItemCallback<ChatListItem>() {
            override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
                // 아이템이 같은지 여부를 체크하는 함수
                return oldItem.foreignkey == newItem.foreignkey
            }

            override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
                // 아이템의 내용이 같은지 여부를 체크하는 함수
                return oldItem == newItem
            }
        }
    }
}
