package com.example.gmailclientappn27.models

data class UserMessagesModel(
    val date: String,
    val from: String,
    val subject: String,
    val attachmentId:String,
    val messageId:String,
    val filename: String

)

