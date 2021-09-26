package com.instructor.manito

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import java.util.*
import kotlin.collections.set

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

    private val playerMenu by lazy {
        bind.drawerView.menu.getItem(0).subMenu
    }
    // 미션창
    private var isExpanded = false
    private val missionCheckAdapter by lazy {
        MissionCheckAdapter(this, room.missions ?: arrayListOf())
    }



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
                    Database.getReference("chats/${room.rid}").orderByChild("timestamp")
                        .startAt(timestamp.toDouble())
                        .addChildEventListener(chatsChildEventListener)
                }
            }

            sendButton.setOnClickListener {
                Database.sendChat(room.rid!!, Chat.TYPE_MESSAGE, chatEditText.text.toString())
                chatEditText.text.clear()
            }
            messageRecycler.adapter = chatAdapter
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            menuButton.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.END)
            }
            startButton.setOnClickListener {
                Database.getReference("rooms/${room.rid}/state").setValue(Room.STATE_READY)
                    .addOnSuccessListener {
                        Database.getReference("rooms/${room.rid}/users").get()
                            .addOnSuccessListener {
                                val userList = it.getValue<HashMap<String, Any>>()!!
                                val users = userList.keys.shuffled()
                                val game = hashMapOf<String, String>()
                                val lastUserNumber = users.size - 1
                                for (i in users.indices) {
                                    if (i != lastUserNumber) {
                                        game[users[i]] = users[i + 1]
                                    } else {
                                        game[users[i]] = users[0]
                                    }
                                }
                                Database.getReference("")
                                    .updateChildren(
                                        hashMapOf<String, Any>(
                                            "games/${room.rid}" to game,
                                            "rooms/${room.rid}/state" to Room.STATE_START)
                                    )
                            }
                    }
            }
            if (room.manager == Authentication.uid) {
                startButton.visibility = View.VISIBLE
            }


            Database.getReference("rooms/${room.rid}/users")
                .addChildEventListener(roomChildEventListener)

            Database.getReference("rooms/${room.rid}/state").get().addOnSuccessListener {
                if(it.value.toString() == "START"){
                    startButton.visibility = View.GONE
                }
            }

            //미션창
            missionRecyclerRoom.layoutParams.height = 0
            constraintLayout6.setOnClickListener {
                changeVisibility()

            }
            missionRecyclerRoom.adapter = missionCheckAdapter


        }

    }

    private fun changeVisibility(){
        with(bind){
            isExpanded = !isExpanded
            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열

            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열


            missionRecyclerRoom.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val targetHeight = missionRecyclerRoom.measuredHeight

            val va = if (isExpanded) ValueAnimator.ofInt(0, targetHeight) else ValueAnimator.ofInt(targetHeight, 0)
            // Animation이 실행되는 시간, n/1000초
            // Animation이 실행되는 시간, n/1000초
            va.duration = 200
            va.addUpdateListener { animation -> // imageView의 높이 변경
                missionRecyclerRoom.layoutParams.height = animation.animatedValue as Int
                missionRecyclerRoom.requestLayout()
                // imageView가 실제로 사라지게하는 부분
                //constraintLayout6.setVisibility(if (isExpanded) View.VISIBLE else View.GONE)
            }
            // Animation start
            // Animation start
            if (isExpanded) {
                arrowImage.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
            } else {
                arrowImage.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            }
            va.start()

        }


    }



    override fun onBackPressed() {
        with(bind) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                finish()
            }
        }
    }

    private val chatsChildEventListener = object : ChildEventListener {
        override fun onChildAdded(
            snapshot: DataSnapshot,
            previousChildName: String?
        ) {
            val chat = snapshot.getValue<Chat>()!!
            chatList.add(chat)
            chatAdapter.notifyItemInserted(chatList.lastIndex)
            bind.messageRecycler.scrollToPosition(chatList.lastIndex)
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

    }

    private val roomChildEventListener = object : ChildEventListener {

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

    }
}