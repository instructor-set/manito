package com.instructor.manito.lib

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

object Util {
    const val MESSAGE_UNDEFINED = "_UNDEFINED"
    val nicknameMap: HashMap<String, String> = hashMapOf()
    val timestampMap: HashMap<String, Any> = hashMapOf()
    fun j(msg: String) = Log.d("jinha", msg)
    fun dummy(any: Any?) = any

    fun uidToNickname(uid: String, toDoListener: ToDoListener): String? {
        return if (nicknameMap.containsKey(uid)) {
            val nickname = nicknameMap.getValue(uid)
            toDoListener.toDo(nickname)
            nickname
        } else {
            Database.getReference("users/$uid/nickname").addValueEventListener(object: ValueEventListener {
                var first = true
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nickname = snapshot.getValue<String>() ?: MESSAGE_UNDEFINED
                    nicknameMap[uid] = nickname
                    if (first) {
                        first = false
                        toDoListener.toDo(nickname)
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
            null
        }
    }
    fun getTimestamp(uid: String, rid: String, toDoListener: ToDoListener) {
        val id = uid + rid
        if (timestampMap.containsKey(id)) {
            val timestamp = timestampMap.getValue(id)
            toDoListener.toDo(timestamp)
        } else {
            Database.getReference("users/$uid/rooms/$rid").addValueEventListener(object: ValueEventListener {
                var first = true
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timestamp = snapshot.getValue<Long>() ?: MESSAGE_UNDEFINED
                    timestampMap[id] = timestamp
                    if (first) {
                        first = false
                        toDoListener.toDo(timestamp)
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
    interface ToDoListener {
        fun toDo(any: Any?)
    }
}