package com.example.bugimarket

data class ChatItem(
    val senderId: String, //문자 보낸 이 id
    val message: String, //보낸 메시지
) {
    constructor(): this("", "")
}