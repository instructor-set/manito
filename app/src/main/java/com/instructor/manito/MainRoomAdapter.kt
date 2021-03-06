package com.instructor.manito

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.AlertdialogEdittextBinding
import com.instructor.manito.databinding.AlertdialogEnterRoomBinding
import com.instructor.manito.databinding.CellMainBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import splitties.activities.start
import splitties.bundle.putExtras

class MainRoomAdapter(private val context: Context, private var listData: ArrayList<Room>) :
    RecyclerView.Adapter<MainRoomAdapter.Holder>() {

    val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRoomAdapter.Holder {
        val view = CellMainBinding.inflate(inflater, parent, false)
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

    private fun enterRoom(context: Context, rid: String?, uid: String?) {

        Database.getReference("rooms/$rid").get().addOnSuccessListener {
            val room = it.getValue<Room>()
            if (room != null && room.state == Room.STATE_WAIT) {
                if (it.child("users/$uid").exists()) {
                    context.start<RoomActivity> {
                        putExtras(RoomActivity.Extras) {
                             this.room = room
                        }
                    }
                } else {
                    val updates = hashMapOf(
                        "rooms/$rid/users/$uid" to true,
                        "users/$uid/rooms/$rid" to ServerValue.TIMESTAMP
                    )
                    Database.getReference("")
                        .updateChildren(updates)
                        .addOnSuccessListener {
                            Database.sendChat(
                                rid!!,
                                Chat.TYPE_ENTER,
                                Chat.MESSAGE_ENTER
                            )
                            context.start<RoomActivity> {
                                putExtras(RoomActivity.Extras) {
                                    this.room = room
                                }
                            }
                        }
                }
            } else {
                // ?????? ?????????????????? ?????? ???
            }
        }
    }

    inner class Holder(private val bind: CellMainBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(room: Room) {

            with(bind) {
                cellTitleText.text = room.title
                @SuppressLint("SetTextI18n")
                cellNumberOfPeople.text = "${room.users?.size ?: 0}/${room.maxUsers}"
                if (room.password.isNullOrBlank()) {
                    cellKeyImage.visibility = View.INVISIBLE
                }


                // ????????? ???
                itemView.setOnClickListener {
                    val rid = room.rid
                    val uid = Authentication.uid

                    val enterRoomBinding =
                        AlertdialogEnterRoomBinding.inflate(inflater)
                    with(enterRoomBinding) {
                        alertTitleTextView.text = room.title
                        @SuppressLint("SetTextI18n")
                        alertMembersTextView.text =
                            "${room.users?.size ?: 0} / ${room.maxUsers}"
                        AlertDialog.Builder(context).setView(root)
                            .setPositiveButton("??????") { _: DialogInterface, _: Int ->


                                if (!room.password.isNullOrBlank()) {

                                    val editTextBinding =
                                        AlertdialogEdittextBinding.inflate(inflater)

                                    with(editTextBinding) {
                                        AlertDialog.Builder(context)
                                            .setTitle("??????????????? ???????????????")
                                            .setView(root)
                                            .setPositiveButton("??????") { _: DialogInterface, _: Int ->
                                                val enterPassword =
                                                    alertEditText.text.toString()
                                                // ?????? ?????? ?????? ???
                                                if (room.password == enterPassword) {
                                                    enterRoom(context, rid, uid)
                                                }
                                                // ???????????? ????????? ???
                                                else {
                                                    Toast.makeText(
                                                        context,
                                                        "??????????????? ???????????????.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            .setNeutralButton("??????", null)
                                            .create().show()
                                    }

                                } else { // ???????????? ?????? ???
                                    enterRoom(context, rid, uid)
                                }
                            }.setNeutralButton("??????", null).also { alertDialog ->
                                Util.uidToNickname(room.manager!!) {
                                    if (it != Util.MESSAGE_UNDEFINED) {
                                        val nickname = it as String
                                        alertManagerTextView.text = nickname
                                        alertDialog.show()
                                    }
                                }
                            }
                    }


                }

            }


        }

    }

}