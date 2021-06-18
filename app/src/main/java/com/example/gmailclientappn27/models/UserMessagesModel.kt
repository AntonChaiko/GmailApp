package com.example.gmailclientappn27

data class UserMessagesModel(
    val date: String,
    val from: String,
    val subject: String,
    val attachmentId:String,
    val messageId:String,
    val filename: String

)

object UserMessagesModelClass {
    var dataObject: MutableList<UserMessagesModel> = mutableListOf()
}