package com.example.bugimarket

import ChatItem
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bugimarket.databinding.ChatItemBinding

class ChatItemAdapter : ListAdapter<ChatItem, ChatItemAdapter.ViewHolder>(diffUtil) {

    // 뷰홀더 클래스
    inner class ViewHolder(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // 데이터를 뷰에 바인딩하는 함수
        fun bind(chatItem: ChatItem) {
            binding.messageTextView.text = chatItem.message
            binding.senderTextView.text = chatItem.senderId
            Log.d("ChatItemAdapter", "${chatItem.message}, ${chatItem.senderId}")
        }
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // ChatItemBinding을 사용하여 레이아웃을 inflate하고 뷰홀더를 생성
        return ViewHolder(ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // 뷰홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 현재 아이템을 뷰홀더에 바인딩
        holder.bind(currentList[position])
        Log.d("ChatItemAdapter", "current Item = ${currentList[position].message}, ${currentList[position].senderId}")
    }

    companion object {
        // DiffUtil을 사용하여 아이템 비교
        val diffUtil = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
