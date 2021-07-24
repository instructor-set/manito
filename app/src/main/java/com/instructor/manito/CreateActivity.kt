package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityCreateBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import splitties.activities.start
import splitties.bundle.putExtras

class CreateActivity : AppCompatActivity() {
    private val bind by lazy {
        ActivityCreateBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(bind) {
            setContentView(root)
            createCreateRoomButton.setOnClickListener {
                createCreateRoomButton.isEnabled = false
                val roomRef = Database.getReference("rooms").push()
                val uid = Authentication.uid!!
                val room = Room(roomRef.key, titleEditText.text.toString(), passwordEditText.text.toString(),
                    Util.dummy(16) as Int?,
                    hashMapOf<String, Boolean>(
                        uid to true
                    ),
                    uid
                )
                roomRef.setValue(room).addOnSuccessListener {
                    start<RoomActivity> {
                        putExtras(RoomActivity.Extras) {
                            RoomActivity.Extras.room = room
                        }
                    }
                }
            }
        }


    }


}