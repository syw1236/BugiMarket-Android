package com.example.bugimarket

data class ArticleModel(
    val sellerId: String,
    var title: String,
    val createdAt: Long,
    var price: String,
    val imageUrl: String,

    var status: String = Status.ONSALE.name,  //디폴트는 판매중
    var description: String,
    val chatKey: String? = null
){
    constructor(): this("","",0,"","", "", "")
}