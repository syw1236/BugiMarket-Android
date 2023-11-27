data class ChatItem(
    val senderId: String, // 메시지를 보낸 사용자의 식별자
    val message: String, // 보낸 메시지 내용
) {
    constructor() : this("", "") // 기본 생성자를 사용하여 빈 ChatItem 객체 생성
}
