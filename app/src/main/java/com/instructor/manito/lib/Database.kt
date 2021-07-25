package com.instructor.manito.lib

import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.instructor.manito.dto.Chat

object Database {
    private val database = Firebase.database("https://android-manito-default-rtdb.asia-southeast1.firebasedatabase.app/")

    fun getReference(path: String) = database.getReference(path)
    fun set(path: String, value: Any?) = getReference(path).setValue(value)
    fun sendChat(rid: String, type: Int, message: String) {
        getReference("chats/$rid").push()
            .setValue(Chat(Authentication.uid, type, message, ServerValue.TIMESTAMP))
    }
}