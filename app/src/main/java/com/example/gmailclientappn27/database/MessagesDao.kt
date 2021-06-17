package com.example.gmailclientappn27.database

import android.graphics.ColorSpace.Model
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Messages>>

    @Query("SELECT * FROM messages_table")
    fun getAllMessages(): List<Messages>?

    @Query("SELECT * FROM messages_table WHERE id = :id")
    fun getById(id: Long): Messages?

    @Insert
    fun insert(messages: Messages?)

    @Update
    fun update(messages: Messages?)

    @Delete
    fun delete(messages: Messages?)

    @Query("DELETE FROM messages_table")
    suspend fun deleteAllMessages()
}