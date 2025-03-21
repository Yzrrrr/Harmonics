package com.example.project2.data

import android.content.Context
import androidx.compose.runtime.Immutable


class ChatDataTest{
    val chatLists = listOf(
        ChatItem(id=0, character = "user", chatText = "Hellooooo"),
        ChatItem(id=0,character = "bot", chatText = "Hellooooo"),
        ChatItem(id=0,character = "user", chatText = "Hellooooo"),
        ChatItem(id=0,character = "bot", chatText = "Hellooooo"),
        ChatItem(id=0,character = "user", chatText = "Hellooooo"),
        ChatItem(id=0,character = "bot", chatText = "Hellooooo"),
    )
}

//fun GetChatItemsFromSQL(Context: Context): List<ChatItem>{
//    val chatItemsList = mutableListOf<ChatItem>()
//    val db = DBHelper(Context, null)
//    val cursor = db.getChat()
//
//    cursor?.use {
//        if (it.moveToFirst()) {
//            do {
//                val character = it.getString(it.getColumnIndexOrThrow(DBHelper.CHARACTER_COl))
//                val chatText = it.getString(it.getColumnIndexOrThrow(DBHelper.TEXT_COL))
//                val chatItem = ChatItem(character, chatText)
//                chatItemsList.add(chatItem)
//            } while (it.moveToNext())
//        }
//    }
//
//    return chatItemsList
//}

