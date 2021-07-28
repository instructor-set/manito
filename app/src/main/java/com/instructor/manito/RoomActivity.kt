package com.instructor.manito

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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

    private var nextItemId: Int = 1
    private val uidToItemId: HashMap<String, Int> = hashMapOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)



        with(bind) {
            sendButton.isEnabled = false
            titleText.text = room.title
            passwordText.text = room.password
            chatEditText.addTextChangedListener(textWatcher)
            Util.getTimestamp(Authentication.uid!!, room.rid!!) {
                if (Util.MESSAGE_UNDEFINED == it) {
                    finish()
                } else {
                    val timestamp = it as Long
                    Database.getReference("chats/${room.rid}").orderByChild("timestamp").startAt(timestamp.toDouble()).addChildEventListener(object: ChildEventListener{
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val chat = snapshot.getValue<Chat>()!!
                            chatList.add(chat)
                            chatAdapter.notifyDataSetChanged()
                            messageRecycler.scrollToPosition(chatList.size - 1)
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
                }
            }

            sendButton.setOnClickListener {
                Database.sendChat(room.rid!!, Chat.TYPE_MESSAGE, chatEditText.text.toString())
                chatEditText.text.clear()
            }
            messageRecycler.adapter = chatAdapter
            messageRecycler.layoutManager = LinearLayoutManager(this@RoomActivity)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            menuButton.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.END)
            }
            val playerMenu = drawerView.menu.getItem(0).subMenu
            drawerView.setNavigationItemSelectedListener {
                Util.j("${drawerLayout.isDrawerOpen(GravityCompat.END)}")
                true
            }

            Database.getReference("rooms/${room.rid}/users").addChildEventListener(object: ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    val uid = snapshot.key!!
                    val itemId = nextItemId++
                    uidToItemId[uid] = itemId
                    Util.uidToNickname(uid) {
                        if (it == Util.MESSAGE_UNDEFINED) {
                            finish()
                        } else {
                            val nickname = it as String
                            playerMenu.add(Menu.NONE, itemId, Menu.NONE, nickname)
                        }
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val uid = snapshot.key!!
                    val itemId = uidToItemId.getValue(uid)
                    uidToItemId.remove(uid)
                    playerMenu.removeItem(itemId)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


        }
    }

    override fun onBackPressed() {
        with(bind){
            if(drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                finish()
            }
        }
    }
}