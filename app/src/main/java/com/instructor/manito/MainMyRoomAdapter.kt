package com.instructor.manito

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.instructor.manito.databinding.CellMainBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.databinding.CellMyRoomBinding
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import kotlinx.coroutines.NonCancellable.start
import splitties.activities.start
import splitties.bundle.putExtras

class MainMyRoomAdapter(private val context: Context, private var listData: ArrayList<Room>) :
    RecyclerView.Adapter<MainMyRoomAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainMyRoomAdapter.Holder {
        val view = CellMyRoomBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MainMyRoomAdapter.Holder, position: Int) {
        val room: Room = listData[position]
        //Log.e("dataList", "data : $listData")
        holder.binding(room)
    }

    override fun getItemCount(): Int {
        return listData.size
        Log.e("dataList", "size : ${listData.size}")
    }

    inner class Holder(private val bind: CellMyRoomBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(room: Room) {

            with(bind) {
                cellRoomTitleText.text = room.title.toString()

                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    // 눌렀을 때
                    itemView.setOnClickListener {
                        val rid = room.rid
                        val uid = Authentication.uid

                        Database.getReference("users/$uid/rooms/$rid")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        context.start<RoomActivity> {
                                            putExtras(RoomActivity.Extras) {
                                                RoomActivity.Extras.room = room
                                            }
                                        }
                                    } else {
                                        val updates = hashMapOf(
                                            "rooms/$rid/users/$uid" to true,
                                            "users/$uid/rooms/$rid" to ServerValue.TIMESTAMP
                                        )
                                        Database.getReference("").updateChildren(updates).addOnSuccessListener {
                                            Database.sendChat(rid!!, Chat.TYPE_ENTER, Chat.MESSAGE_ENTER)
                                            context.start<RoomActivity> {
                                                putExtras(RoomActivity.Extras) {
                                                    RoomActivity.Extras.room = room
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })

                    }

                }

            }
        }
    }
}

