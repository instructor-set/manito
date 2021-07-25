package com.instructor.manito

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ServerValue
import com.instructor.manito.databinding.CellMainBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import splitties.activities.start
import splitties.bundle.putExtras

class MainRoomAdapter(private val context: Context, private var listData: ArrayList<Room>) :
    RecyclerView.Adapter<MainRoomAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRoomAdapter.Holder {
        val view = CellMainBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MainRoomAdapter.Holder, position: Int) {
        val room: Room = listData[position]
        //Log.e("dataList", "data : $listData")
        holder.binding(room)
    }

    override fun getItemCount(): Int {
        return listData.size
        //Log.e("dataList", "size : ${listData.size}")
    }

    inner class Holder(private val bind: CellMainBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(room: Room) {

            with(bind) {
                cellTitleText.text = room.title
                @SuppressLint("SetTextI18n")
                cellNumberOfPeople.text = "${room.users?.size}/${room.maxUsers}"
                if (room.password.isNullOrBlank()) {
                    cellKeyImage.visibility = View.INVISIBLE
                }

                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    itemView.setOnClickListener {
                        val rid = room.rid
                        val uid = Authentication.uid
                        Database.getReference("users/$uid/rooms/$rid").get().addOnSuccessListener {
                            if(it.exists()){
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

                    }
                }
            }


        }

    }

}