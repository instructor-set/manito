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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.getValue
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
                cellNumberOfPeople.text = "${room.users?.size ?: 0}/${room.maxUsers}"
                if (room.password.isNullOrBlank()) {
                    cellKeyImage.visibility = View.INVISIBLE
                }


                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    // 눌렀을 때
                    itemView.setOnClickListener {
                        val rid = room.rid
                        val uid = Authentication.uid

                        // 비밀번호가 있을 때
                        if(!room.password.isNullOrBlank()){
                            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val dialogview = inflater.inflate(R.layout.alertdialog_edittext, null)
                            val dialogText = dialogview.findViewById<EditText>(R.id.alertEditText)
                            val alertDialog = AlertDialog.Builder(context)
                                .setTitle("비밀번호를 입력하세요")
                                .setView(dialogview)
                                .setPositiveButton("입장"){
                                        _: DialogInterface, _:Int->
                                    val enterPassword = dialogText.text.toString()
                                    // 비밀 번호 맞을 때
                                    if(room.password == enterPassword){
                                        Database.getReference("users/$uid/rooms/$rid").get().addOnSuccessListener {
                                            snapshot: DataSnapshot ->
                                            if(snapshot.exists()){
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
                                    // 비밀번호 틀렸을 때
                                    else{
                                        Toast.makeText(context, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNeutralButton("취소", null)
                                .create()
                            alertDialog.show()

                        }else { // 비밀번호 없을 때
                            Database.getReference("users/$uid/rooms/$rid").get().addOnSuccessListener {
                                    snapshot: DataSnapshot ->
                                if(snapshot.exists()){

                                    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                    val dialogview = inflater.inflate(R.layout.alertdialog_enter_room, null)
                                    val dialogTitle = dialogview.findViewById<TextView>(R.id.alertTitleTextView)
                                    val dialogManager = dialogview.findViewById<TextView>(R.id.alertManagerTextView)
                                    val dialogMembers = dialogview.findViewById<TextView>(R.id.alertMembersTextView)
                                    val alertDialog = AlertDialog.Builder(context)
                                    with(alertDialog) {
                                        dialogTitle.text = room.title
                                        setView(dialogview)
                                        Database.getReference("users/${room.manager}/nickname").get().addOnSuccessListener {
                                            nickname ->
                                            dialogManager.text = nickname.getValue<String>()
                                        }
                                        @SuppressLint("SetTextI18n")
                                        dialogMembers.text = "${room.users?.size ?: 0} / ${room.maxUsers}"
                                        setPositiveButton("입장") { _: DialogInterface, _: Int->
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