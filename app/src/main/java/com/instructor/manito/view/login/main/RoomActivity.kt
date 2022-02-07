package com.instructor.manito.view.login.main

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.instructor.manito.R
import com.instructor.manito.ShowAllFragment
import com.instructor.manito.databinding.ActivityRoomBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Game
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import com.instructor.manito.view.login.main.room.FinishFragment
import com.instructor.manito.view.login.main.room.MissionCheckAdapter
import com.instructor.manito.view.login.main.room.RoomChatAdapter
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.withExtras
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
    private val myManitoMenu by lazy {
        bind.drawerView.menu.getItem(1).subMenu
    }

    // 종료
    private val exitMenu by lazy {
        bind.drawerView.menu.getItem(2)
    }

    // 미션창
    private var isExpanded = false
    private val missionCheckAdapter by lazy {
        MissionCheckAdapter(this, room.missions ?: arrayListOf(), room.rid!!)
    }

    private var menuExpanded = false
    private var showFragment = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)



        with(bind) {
            shareButton.setOnClickListener {
                val dynamicLink = Firebase.dynamicLinks.dynamicLink {
                    link = Uri.parse("https://manito.page.com/${room.rid}")
                    domainUriPrefix = "https://manito.page.link/"
                    androidParameters { }
                }
                startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, dynamicLink.uri.toString())
                    type = "text/plain"
                }, "Share"))
            }

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
            Database.getReference("games/${room.rid}/${Authentication.uid}")
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val game = snapshot.getValue<Game>()
                        if (game != null) {
                            Util.uidToNickname(game.manito!!) { nickname ->
                                if (nickname != Util.MESSAGE_UNDEFINED) {
                                    myManitoMenu.add("$nickname")
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            sendButton.setOnClickListener {
                Database.sendChat(room.rid!!, Chat.TYPE_MESSAGE, chatEditText.text.toString())
                chatEditText.text.clear()
            }
            messageRecycler.adapter = chatAdapter
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            menuButton.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.END)
            }
            button1.setOnClickListener {
                if (button1.text.equals("게임 시작")) {
                    menuVisibility()
                    Database.getReference("rooms/${room.rid}/state").setValue(Room.STATE_READY)
                        .addOnSuccessListener {
                            Database.getReference("rooms/${room.rid}/users").get()
                                .addOnSuccessListener {
                                    val userList = it.getValue<HashMap<String, String>>()!!
                                    val users = userList.values.shuffled()
                                    val games = hashMapOf<String, Game>()
                                    val missions = HashMap<String, Boolean>()
                                    room.missions?.forEach { mission ->
                                        missions[mission] = false
                                    }
                                    for (i in users.indices) {
                                        if (i != users.size - 1) {
                                            games[users[i]] = Game(users[i + 1], missions)
                                        } else {
                                            games[users[i]] = Game(users[0], missions)
                                        }
                                    }
                                    Database.getReference("")
                                        .updateChildren(
                                            hashMapOf<String, Any>(
                                                "games/${room.rid}" to games,
                                                "rooms/${room.rid}/state" to Room.STATE_START)
                                        )
                                    Database.sendChat(room.rid!!,
                                        Chat.TYPE_IMPORTANT,
                                        "게임이 시작되었습니다.")
                                }
                        }

                } else {
                    Database.getReference("rooms/${room.rid}/state").setValue(Room.STATE_END)
                    Database.sendChat(room.rid!!, Chat.TYPE_IMPORTANT, "게임이 종료되었습니다.")
                }

            }
            if (room.manager == Authentication.uid) {
                button1.visibility = View.VISIBLE
            }

            Database.getReference("rooms/${room.rid}/users")
                .addChildEventListener(roomChildEventListener)

            root.setOnClickListener {
                Util.j(it.id)
            }
            Database.getReference("rooms/${room.rid}/state")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        when (snapshot.getValue(String::class.java)) {
                            Room.STATE_START -> {
                                button1.text = "게임 종료"
                                missionCheckAdapter.isGameStart = true
                                missionCheckAdapter.notifyDataSetChanged()
                            }
                            Room.STATE_END -> {
                                button1.visibility = View.GONE
                                button2.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

            //미션창
            missionRecyclerRoom.layoutParams.height = 0
            constraintLayout6.setOnClickListener {
                changeVisibility()

            }
            missionRecyclerRoom.adapter = missionCheckAdapter

            constraintLayout7.setOnClickListener {
                menuVisibility()
            }

            val transaction =
                supportFragmentManager.beginTransaction().add(R.id.frameLayout, FinishFragment())
            transaction.commit()
            button2.setOnClickListener {
                setFragment(false)
            }
            button3.setOnClickListener {
                Util.j(button3.text)
            }


        }

    }

    private fun changeVisibility() {
        with(bind) {
            isExpanded = !isExpanded
            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열

            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열


            missionRecyclerRoom.measure(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            val targetHeight = missionRecyclerRoom.measuredHeight

            val va = if (isExpanded) ValueAnimator.ofInt(0, targetHeight) else ValueAnimator.ofInt(
                targetHeight,
                0)
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

    private fun menuVisibility() {
        with(bind) {
            menuExpanded = !menuExpanded
            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열

            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열

            constraintLayout8.measure(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            val targetHeight = constraintLayout8.measuredHeight

            val constraints = ConstraintSet()
            constraints.clone(rootLayout)


            val va =
                if (menuExpanded) ValueAnimator.ofInt(0, targetHeight) else ValueAnimator.ofInt(
                    targetHeight,
                    0)
            // Animation이 실행되는 시간, n/1000초
            // Animation이 실행되는 시간, n/1000초
            va.duration = 200
            va.addUpdateListener { animation -> // imageView의 높이 변경
                //bottomNavi.layoutParams.height = animation.animatedValue as Int
                //bottomNavi.requestLayout()
                constraintLayout8.setVisibility(if (menuExpanded) View.VISIBLE else View.GONE)

                // imageView가 실제로 사라지게하는 부분

            }


            // Animation start
            // Animation start
            if (menuExpanded) {
                arrowImage2.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                constraintLayout8.visibility = View.VISIBLE
                constraints.connect(constraintLayout7.id,
                    ConstraintSet.BOTTOM,
                    constraintLayout8.id,
                    ConstraintSet.TOP)
                constraints.applyTo(rootLayout)
            } else {
                arrowImage2.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
                constraintLayout8.visibility = View.GONE
                constraints.connect(constraintLayout7.id,
                    ConstraintSet.BOTTOM,
                    constraintLayout.id,
                    ConstraintSet.TOP)
                constraints.applyTo(rootLayout)
            }

            va.start()

        }

    }

    fun setFragment(showAll: Boolean) {
        with(bind) {
            if (showAll) {
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, ShowAllFragment())
                transaction.commit()
            } else {
                showFragment = !showFragment
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, FinishFragment())
                transaction.commit()
                frameLayout.visibility = if (showFragment) View.VISIBLE else View.GONE

            }

        }

    }


    override fun onBackPressed() {
        with(bind) {
            when {
                drawerLayout.isDrawerOpen(GravityCompat.END) -> {
                    drawerLayout.closeDrawer(GravityCompat.END)
                }
                showFragment -> {
                    frameLayout.visibility = View.GONE
                    showFragment = false

                }
                else -> {
                    finish()
                }
            }
        }
    }

    private val chatsChildEventListener = object : ChildEventListener {
        override fun onChildAdded(
            snapshot: DataSnapshot,
            previousChildName: String?,
        ) {
            val chat = snapshot.getValue<Chat>()!!
            chatList.add(chat)
            chatAdapter.notifyItemInserted(chatList.lastIndex)
            bind.messageRecycler.scrollToPosition(chatList.lastIndex)
        }

        override fun onChildChanged(
            snapshot: DataSnapshot,
            previousChildName: String?,
        ) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

        override fun onChildMoved(
            snapshot: DataSnapshot,
            previousChildName: String?,
        ) {
        }

        override fun onCancelled(error: DatabaseError) {
        }

    }

    private val roomChildEventListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val uid = snapshot.getValue<String>()!!
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