package com.instructor.manito

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.CellMyRoomBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
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
    }

    inner class Holder(private val bind: CellMyRoomBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(room: Room) {

            val rid = room.rid
            val uid = Authentication.uid

            with(bind) {
                cellRoomTitleText.text = room.title.toString()
                cellRoomStateText.text = when(room.state) {
                    Room.STATE_WAIT -> "대기중"
                    Room.STATE_READY -> "준비"
                    Room.STATE_START -> "게임중"
                    else -> ""
                }
                Database.getReference("games/$rid/$uid").get().addOnSuccessListener {
                    val manitoUid = it.getValue<String>()
                    // 게임 시작 안했음
                    if (manitoUid == null) {
                        cellMyManitoText.visibility = View.GONE
                    } else {
                        Util.uidToNickname(manitoUid) { nickname ->
                            if (nickname != Util.MESSAGE_UNDEFINED) {
                                cellMyManitoText.text = nickname as String
                            }
                        }
                    }

                }



                // 방 나가기 버튼을 눌렀을 때
                exitButton.setOnClickListener {
                    // 알림먼저 - 나간 방은 다시 들어갈 수 없습니다.
                    // 확인 누르면 방 나가기 --> 내데이터에서 삭제, 방목록에서 삭제
                    // user의 rooms 정보에서 삭제
                    val inflater = LayoutInflater.from(context)
                    val dialogView = inflater.inflate(R.layout.dialog_exit_room, null)
                    val alertDialog = AlertDialog.Builder(context)
                    Database.getReference("rooms/${room.rid}").get()
                        .addOnSuccessListener {

                            val roomData = it.getValue<Room>()
                            val isManager = roomData?.manager.equals(uid.toString())

                            with(alertDialog) {
                                setView(dialogView)
                                setTitle("방 나가기")
                                setMessage("나가면 다시 들어갈 수 없습니다. ")
                                if (isManager) {
                                    setMessage("방장이 나가면 방이 사라집니다.")
                                }
                                setPositiveButton("확인") { _: DialogInterface, _: Int ->
                                    val updates = hashMapOf<String, Any?>()
                                    if (isManager) {
                                        // 유저들의 방 목록에서 지우고
                                        for (user in roomData?.users?.keys!!) {
                                            updates["users/$user/rooms/$rid"] = null
                                        }
                                        // 방을 지우고
                                        updates["rooms/$rid"] = null
                                    } else {
                                        // 내 방 목록에서 지우고
                                        updates["users/$uid/rooms/$rid"] =
                                            null
                                        // 방에서 나를 지우고
                                        updates["rooms/$rid/users/$uid"] =
                                            null
                                    }
                                    Database.getReference("").updateChildren(updates).addOnSuccessListener {


                                        (this@MainMyRoomAdapter.context as MainActivity).refreshChatList(true)
                                        Database.sendChat(rid!!, Chat.TYPE_EXIT, Chat.MESSAGE_EXIT)
                                    }
                                    // 방을 삭제
                                }
                                setNeutralButton("취소", null)
                                show()
                            }


                        }


                }
                // 눌렀을 때
                itemView.setOnClickListener {

                    Database.getReference("users/$uid/rooms/$rid").get().addOnSuccessListener {
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
                            Database.getReference("").updateChildren(updates)
                                .addOnSuccessListener {
                                    Database.sendChat(
                                        rid!!,
                                        Chat.TYPE_ENTER,
                                        Chat.MESSAGE_ENTER
                                    )
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
