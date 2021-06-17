package com.example.gmailclientappn27.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [Messages::class], version = 2, exportSchema = false)
abstract class MessagesDatabase : RoomDatabase() {


    abstract fun messagesDao(): MessagesDao?

    companion object {
        @Volatile
        private var INSTANCE: MessagesDatabase? = null
        private val migration_1_2:Migration = object : Migration(1,2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE messages_table ADD COLUMN attachmentId TEXT NOT NULL DEFAULT''")
                database.execSQL("ALTER TABLE messages_table ADD COLUMN messageId TEXT NOT NULL DEFAULT''")
            }
        }


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
                ).addMigrations(migration_1_2).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}