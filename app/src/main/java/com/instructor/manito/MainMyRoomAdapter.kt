package com.instructor.manito

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
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
        Log.e("dataList", "size : ${listData.size}")
    }

    inner class Holder(private val bind: CellMyRoomBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(room: Room) {

            with(bind) {
                cellRoomTitleText.text = room.title.toString()

                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    // 방 나가기 버튼을 눌렀을 때
                    exitButton.setOnClickListener {
                        // 알림먼저 - 나간 방은 다시 들어갈 수 없습니다.
                        // 확인 누르면 방 나가기 --> 내데이터에서 삭제, 방목록에서 삭제
                        // user의 rooms 정보에서 삭제
                        val inflater =
                            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val dialogView = inflater.inflate(R.layout.dialog_exit_room, null)
                        val alertDialog = AlertDialog.Builder(context)
                        // 방장일 때 아닐 때 구분
                        Database.getReference("rooms/${room.rid}/manager").get()
                            .addOnSuccessListener {
                                if (it.value.toString().equals(Authentication.uid.toString())) {
                                    with(alertDialog) {
                                        setView(dialogView)
                                        setTitle("방 나가기")
                                        setMessage("나가면 다시 들어갈 수 없습니다. ")
                                        setMessage("방장이 나가면 방이 사라집니다.")
                                        setPositiveButton("확인") { _: DialogInterface, _: Int ->
                                            // 내 목록에서 삭제
                                            Database.getReference("users/${Authentication.uid}/rooms")
                                                .child("${room.rid}").removeValue().addOnSuccessListener(object :
                                                    OnSuccessListener<Void> {
                                                    override fun onSuccess(p0: Void?) {


                                                    }

                                                })
                                            // 사람들 목록에서 삭제


                                            var users = arrayListOf<String>()
                                            Database.getReference("rooms/${room.rid}/users").get().addOnSuccessListener {
                                                    snapshot: DataSnapshot ->
                                                users = snapshot.value.toString().split(',') as ArrayList<String>
                                                Toast.makeText(
                                                    context,
                                                    users.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                            // 방을 삭제

                                            Database.getReference("rooms/${room.rid}").removeValue().addOnSuccessListener(object :
                                                OnSuccessListener<Void> {
                                                override fun onSuccess(p0: Void?) {
                                                    Toast.makeText(context, "삭제", Toast.LENGTH_SHORT).show()
                                                }

                                            })
                                            listData.remove(room)
                                            notifyDataSetChanged()



                                        }
                                        setNeutralButton("취소", null)
                                        show()
                                    }


                                } else {
                                    with(alertDialog) {
                                        setView(dialogView)
                                        setTitle("방 나가기")
                                        setMessage("나가면 다시 들어갈 수 없습니다. ")
                                        setPositiveButton("확인") { _: DialogInterface, _: Int ->

                                            delete_content(room)

                                        }
                                        setNeutralButton("취소", null)
                                        show()
                                    }
                                }
                            }


                    }
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

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })

                    }

                }

            }
        }

        fun delete_content(room: Room){

            Database.getReference("users/${Authentication.uid}/rooms")
                .child("${room.rid}").removeValue()
                .addOnSuccessListener(object :
                    OnSuccessListener<Void> {
                    override fun onSuccess(p0: Void?) {

                    }

                })
            Database.getReference("rooms/${room.rid}/users")
                .child("${Authentication.uid}").removeValue()
                .addOnSuccessListener(object :
                    OnSuccessListener<Void> {
                    override fun onSuccess(p0: Void?) {


                        listData.remove(room)
                        notifyDataSetChanged()

                    }

                })


        }
    }
}
