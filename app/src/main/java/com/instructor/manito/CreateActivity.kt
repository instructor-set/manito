package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

    private val dataList = arrayListOf<String>()
    private val adapter = MissionAdapter(this@CreateActivity, dataList)


    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        with(bind) {
            dataList.add("미션1")
            setContentView(root)
            createCreateRoomButton.setOnClickListener {
                createCreateRoomButton.isEnabled = false
                val roomRef = Database.getReference("rooms").push()
                val uid = Authentication.uid!!
                val rid = roomRef.key
                val room = Room(rid, titleEditText.text.toString(), passwordEditText.text.toString(),
                    dummy(16) as Int?,
                    hashMapOf(
                        uid to true
                    ),
                    uid
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
            missionRecycler.layoutManager = LinearLayoutManager(this@CreateActivity)
        }




    }

//    override fun onStart() {
//        super.onStart()
//
//    }


}