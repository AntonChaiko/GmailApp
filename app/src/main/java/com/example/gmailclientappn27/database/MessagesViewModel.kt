package com.example.gmailclientappn27.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MessagesViewModel(application: Application) : AndroidViewModel(application) {
    val readAllData: LiveData<List<Messages>>
    private val repository: MessagesRepository

    init {
        val wordsDao = MessagesDatabase.getDatabase(application).messagesDao()
        repository = MessagesRepository(wordsDao!!)
        readAllData = repository.readAllData
    }

    fun addMessage(messages: Messages) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMessages(messages)
        }
    }



    fun deleteAllMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllMessages()
        }
    }
    fun deleteCurrentMessage(messages: Messages) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(messages)
        }
    }
}