package com.example.gmailclientappn27.database

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "messages_table")
data class Messages(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val date: String,
    val subject: String,
    val form: String
)
