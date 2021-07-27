package com.instructor.manito

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ServerValue
import com.instructor.manito.databinding.AlertdialogEdittextBinding
import com.instructor.manito.databinding.CellMainBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import splitties.activities.start
import splitties.bundle.putExtras
import splitties.toast.toast

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
                    // 눌렀을 때
                    itemView.setOnClickListener {
                        val rid = room.rid
                        val uid = Authentication.uid
                        // 내 방들 중 하나라면 - 근데 이건 무조건 새로운 방 아닌가?
                        Database.getReference("rooms/$rid").get().addOnSuccessListener {

                            // 비밀번호가 있을 때
                            if(it.child("password").value.toString() != ""){
                                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                val dialogview = inflater.inflate(R.layout.alertdialog_edittext, null)
                                val dialogText = dialogview.findViewById<EditText>(R.id.alertEditText)
                                val alertDialog = AlertDialog.Builder(context)
                                    .setTitle("비밀번호를 입력하세요")
                                    .setView(dialogview)
                                    .setPositiveButton("입장"){
                                        _:DialogInterface, _:Int->
                                        val enterPassword = dialogText.text.toString()
                                        // 비밀 번호 맞을 때
                                        if(it.child("password").value.toString().equals(enterPassword)){
                                            if (it.exists()) {
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
                                        // 비밀번호 틀렸을 때
                                        else{
                                            Toast.makeText(context, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .setNeutralButton("취소", null)
                                    .create()
                                alertDialog.show()

                            }else { // 비밀번호 없을 때
                                if (it.exists()) {
                                    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                    val dialogview = inflater.inflate(R.layout.alertdialog_enter_room, null)
                                    val dialogTitle = dialogview.findViewById<TextView>(R.id.alertTitleTextView)
                                    val dialogManager = dialogview.findViewById<TextView>(R.id.alertManagerTextView)
                                    val dialogMembers = dialogview.findViewById<TextView>(R.id.alertMembersTextView)
                                    val alertDialog = AlertDialog.Builder(context)
                                    with(alertDialog) {
                                        dialogTitle.text = it.child("title").value.toString()
                                        //setTitle(it.child("title").value.toString())
                                        setView(dialogview)
                                        Database.getReference("users/${it.child("manager").value.toString()}/nickname").get().addOnSuccessListener {
                                            dialogManager.text = it.value.toString()
                                        }
                                        val users = it.child("users").value as Map<String, Boolean>?
                                        dialogMembers.text = "${users?.size} / ${it.child("maxUsers").value}"
                                        setPositiveButton("입장") {_:DialogInterface, _: Int->
                                            context.start<RoomActivity> {
                                                putExtras(RoomActivity.Extras) {
                                                    RoomActivity.Extras.room = room
                                                }
                                            }
                                        }
                                        setNeutralButton("취소", null)
                                        show()
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

}