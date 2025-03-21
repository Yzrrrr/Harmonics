package com.example.project2.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class ChatItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val character: String,
    val chatText: String
)


@Dao
interface ChatItemDao {
    @Insert
    suspend fun insert(chatItem: ChatItem)

    @Query("SELECT * FROM ChatItem")
    suspend fun getAllChatItems(): List<ChatItem>
}

@Database(entities = [ChatItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatItemDao(): ChatItemDao
}