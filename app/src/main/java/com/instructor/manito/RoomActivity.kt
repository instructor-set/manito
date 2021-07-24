package com.instructor.manito

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityRoomBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import com.instructor.manito.lib.Util.j
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.withExtras

class RoomActivity : AppCompatActivity() {

    object Extras : BundleSpec() {
        var room: Room by bundle()
    }

    private val room by lazy {
        withExtras(Extras) {
            room
        }
    }

    private val bind by lazy {
        ActivityRoomBinding.inflate(layoutInflater)
    }

    private val whiteGrayInt by lazy {
        ContextCompat.getColor(this@RoomActivity, R.color.whiteGray)
    }

    private val whiteInt by lazy {
        ContextCompat.getColor(this@RoomActivity, R.color.white)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            with(bind) {
                p0?.run {
                    // 채팅 메시지가 비어있으면 전송 버튼 비활성화
                    sendButton.isEnabled = if (toString().trimEnd().isEmpty()) {
                        sendButton.setColorFilter(whiteGrayInt)
                        false
                    }
                    // 채팅 메시지가 있으면 전송 버튼 활성화
                    else {
                        sendButton.setColorFilter(whiteInt)
                        true
                    }
                }
            }
        }

    }

    private val chatList = arrayListOf<Chat>()
    private val chatAdapter = RoomChatAdapter(this, chatList)

    private var firstTimestamp = Long.MAX_VALUE



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)



        with(bind) {
            sendButton.isEnabled = false
            titleText.text = room.title
            passwordText.text = room.password
            chatEditText.addTextChangedListener(textWatcher)
            Database.getReference("chats/${room.rid}")
                .addChildEventListener(object : ChildEventListener {
                    var start = false
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val chat = snapshot.getValue<Chat>()!!
                        Util.getTimestamp(Authentication.uid!!, room.rid!!, object: Util.ToDoListener{
                            override fun toDo(any: Any?) {
                                if (any == Util.MESSAGE_UNDEFINED) {
                                    finish()
                                }
                                val timestamp = any as Long
                                if (timestamp <= chat.timestamp as Long) {
                                    chatList.add(chat)
                                    chatAdapter.notifyDataSetChanged()
                                    messageRecycler.scrollToPosition(chatList.size - 1)
                                }

                            }

                        })

                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        j("childremoved")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        j("childmoved")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        j("childcancelled")
                    }

                })
            sendButton.setOnClickListener {
                Database.sendChat(room.rid!!, Chat.TYPE_MESSAGE, chatEditText.text.toString())
                chatEditText.text.clear()
            }
            messageRecycler.adapter = chatAdapter
            messageRecycler.layoutManager = LinearLayoutManager(this@RoomActivity)


        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}