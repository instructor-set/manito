package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityCreateBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Database
import splitties.activities.start
import splitties.bundle.putExtras

class CreateActivity : AppCompatActivity() {
    private val bind by lazy {
        ActivityCreateBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            setContentView(root)
            createCreateRoomButton.setOnClickListener {
                createCreateRoomButton.isEnabled = false
                Database.getReference("rooms").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val roomNo = currentData.childrenCount + 1
                        currentData.child("$roomNo").value =
                            Room(roomNo, titleEditText.text.toString(), passwordEditText.text.toString())
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        currentData!!.apply {
                            start<RoomActivity> {
                                putExtras(RoomActivity.Extras) {
                                    room = child("$childrenCount").getValue<Room>()!!
                                }
                            }
                        }
                    }

                })

            }
        }

    }


}