package com.example.bugimarket

data class ChatListItem(
    val buyerId: String,
    val sellerId: String,
    val foreignkey: Long,
    val title: String
) {
    constructor(): this("", "", 0, "")
}