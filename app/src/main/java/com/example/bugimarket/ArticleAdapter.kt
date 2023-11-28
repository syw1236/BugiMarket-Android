package com.example.bugimarket

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bugimarket.databinding.ItemArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import android.view.View


class ArticleAdapter(private val onItemClicked: (ArticleModel, Action) -> Unit) :
    ListAdapter<ArticleModel, ArticleAdapter.ArticleViewHolder>(Companion.diffUtil) {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    enum class Action {
        CLICK, DELETE
    }

    // ArticleAdapter 클래스
    inner class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position), Action.CLICK)
                }
            }

            binding.deleteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position), Action.DELETE)
                }
            }
        }

        fun bind(articleModel: ArticleModel) {
            val format = SimpleDateFormat("MM월 dd일")
            val date = Date(articleModel.createdAt)

            binding.titleTextView.text = articleModel.title
            binding.dateTextView.text = format.format(date).toString()
            binding.priceTextView.text = "${articleModel.price}원"
            binding.statusTextView.text =
                if (articleModel.status == Status.ONSALE.name) "판매중" else "판매완료"

            // 현재 사용자가 아이템 작성자인지 확인
            val isCurrentUserItem = auth.currentUser?.uid == articleModel.sellerId

            // 삭제 버튼 표시 여부 결정
            binding.deleteButton.visibility = if (isCurrentUserItem) View.VISIBLE else View.GONE

            if (articleModel.imageUrl.startsWith("gs://")) {
                val storageRef =
                    FirebaseStorage.getInstance().getReferenceFromUrl(articleModel.imageUrl)

                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Glide.with(binding.thumbnailImageView)
                        .load(imageUrl)
                        .into(binding.thumbnailImageView)
                }.addOnFailureListener {

                }
            } else {
                Glide.with(binding.thumbnailImageView)
                    .load(articleModel.imageUrl)
                    .into(binding.thumbnailImageView)
            }

            binding.root.setOnClickListener {
                onItemClicked(articleModel, Action.CLICK)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val articleModel = currentList[position]
        holder.bind(articleModel)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java).apply {
                putExtra("chatKey", articleModel.chatKey)
                putExtra("title", articleModel.title)
                putExtra("price", articleModel.price)
                putExtra("description", articleModel.description)
                putExtra("imageUrl", articleModel.imageUrl)
                putExtra("status", articleModel.status)
                putExtra("isSeller", articleModel.sellerId == auth.currentUser?.uid)
                putExtra("sellerId", articleModel.sellerId)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ArticleModel>() {
            override fun areItemsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean {
                return oldItem.createdAt == newItem.createdAt
            }

            override fun areContentsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean {
                return oldItem == newItem
            }
        }
    }

}
