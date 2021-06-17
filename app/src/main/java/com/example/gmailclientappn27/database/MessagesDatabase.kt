package com.example.gmailclientappn27.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Messages::class], version = 1, exportSchema = false)
abstract class MessagesDatabase : RoomDatabase() {


    abstract fun messagesDao(): MessagesDao?

    companion object {
        @Volatile
        private var INSTANCE: MessagesDatabase? = null

        fun getDatabase(context: Context): MessagesDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    MessagesDatabase::class.java,
                    "messages_table"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}