data class ChatListItem(
    val buyerId: String, // 구매자의 식별자
    val sellerId: String, // 판매자의 식별자
    val foreignkey: String, // 방을 구별하는 외래키
    val title: String // 채팅 방 제목
) {
    constructor() : this("", "", "", "") // 기본 생성자를 사용하여 빈 ChatListItem 객체 생성
}
