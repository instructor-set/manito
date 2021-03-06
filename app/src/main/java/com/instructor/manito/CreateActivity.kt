package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ServerValue
import com.instructor.manito.databinding.ActivityCreateBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util.dummy
import splitties.activities.start
import splitties.bundle.putExtras

class CreateActivity : AppCompatActivity() {
    private val bind by lazy {
        ActivityCreateBinding.inflate(layoutInflater)
    }

    private val missions = arrayListOf("")
    private val adapter = MissionAdapter(this@CreateActivity, missions)


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        with(bind) {
            setContentView(root)
            createCreateRoomButton.setOnClickListener {
                // 제목이 없으면
                titleEditText.text.ifBlank {
                    return@setOnClickListener
                }
                createCreateRoomButton.isEnabled = false
                val roomRef = Database.getReference("rooms").push()
                val uid = Authentication.uid!!
                val rid = roomRef.key
                val iter = missions.iterator()
                while (iter.hasNext()){
                    iter.next().ifBlank {
                        iter.remove()
                    }
                }
                val room = Room(rid, titleEditText.text.toString(), passwordEditText.text.toString(),
                    dummy(16) as Int?,
                    hashMapOf(
                        uid to true
                    ),
                    uid,
                    Room.STATE_WAIT,
                    missions.distinct()
                )
                val updates = hashMapOf(
                    "rooms/$rid" to room,
                    "users/$uid/rooms/$rid" to ServerValue.TIMESTAMP
                )
                Database.getReference("").updateChildren(updates).addOnSuccessListener {
                    Database.sendChat(rid!!, Chat.TYPE_ENTER, Chat.MESSAGE_ENTER)
                    start<RoomActivity> {
                        putExtras(RoomActivity.Extras) {
                            this.room = room
                        }
                    }
                }
            }

            // 어댑터 연결
            missionRecycler.adapter = adapter
        }




    }

//    override fun onStart() {
//        super.onStart()
//
//    }


}