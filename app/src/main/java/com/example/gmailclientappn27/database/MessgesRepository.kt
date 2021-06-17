package com.example.gmailclientappn27.database

import androidx.lifecycle.LiveData
import androidx.room.Query

class MessagesRepository(private var messagesDao: MessagesDao) {
    val readAllData: LiveData<List<Messages>> = messagesDao.readAllData()

    suspend fun addMessages(messages: Messages) {
        messagesDao.insert(messages)
    }

    suspend fun deleteAllMessages() {
        messagesDao.deleteAllMessages()
    }

    suspend fun getById(id: Long) {
        messagesDao.getById(id)
    }

    suspend fun delete(messages: Messages) {
        messagesDao.delete(messages)
    }
}