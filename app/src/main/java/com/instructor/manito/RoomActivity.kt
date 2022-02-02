package com.instructor.manito

import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavDeepLinkBuilder
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.appinvite.FirebaseAppInvite
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.instructor.manito.databinding.ActivityRoomBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Game
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
    private val myManitoMenu by lazy {
        bind.drawerView.menu.getItem(1).subMenu
    }
    // 미션창
    private var isExpanded = false
    private val missionCheckAdapter by lazy {
        MissionCheckAdapter(this, room.missions ?: arrayListOf())
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        //share link
        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data
        handleIntent(intent)

//        val pendingIntent = NavDeepLinkBuilder(context)
//            .setGraph(R.navigation.nav_graph)
//            .setDestination(R.id.android)
//            .setArguments(args)
//            .createPendingIntent()

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
            Database.getReference("games/${room.rid}/${Authentication.uid}").addValueEventListener(object:
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
            startButton.setOnClickListener {
                Database.getReference("rooms/${room.rid}/state").setValue(Room.STATE_READY)
                    .addOnSuccessListener {
                        Database.getReference("rooms/${room.rid}/users").get()
                            .addOnSuccessListener {
                                val userList = it.getValue<HashMap<String, Any>>()!!
                                val users = userList.keys.shuffled()
                                val games = hashMapOf<String, Game>()
                                val missions = HashMap<String, Boolean>()
                                val lastUserNumber = users.size - 1
                                room.missions?.forEach {  mission ->
                                    missions[mission] = false
                                }
                                for (i in users.indices) {
                                    if (i != lastUserNumber) {
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


            //링크생성
            shareButton.setOnClickListener{
                test(this@RoomActivity, "a")
//                createDynamicLink()
//                createShortDynamicLink()
////                getInvitation()
//                Log.d("dynamicLink","success")
            }
        }
        handleDeepLink()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?){
        val appLinkAction = intent?.action
        val appLinkData: Uri? = intent?.data
        if(Intent.ACTION_VIEW == appLinkAction){
            appLinkData?.lastPathSegment?.also{ roomEnt->
                Uri.parse("content://com.manito_app/game/")
                    .buildUpon()
                    .appendPath(roomEnt)
                    .build().also { appData ->
                        enterRoom(appData)
                    }

            }
        }
    }

    private fun enterRoom(appData: Uri?) {

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

    fun processShortLink(shortLink: Uri?, previewLink: Uri?) {
        shortLink?.let { shareLink(it) }
        Log.d("A", shortLink.toString())
        Log.d("A", previewLink.toString())
    }

    fun createDynamicLink(){
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse("https://manito.page.link/")
            domainUriPrefix = "https://manito.page.link"
            // Open links with this app on Android
            androidParameters { }
            // Open links with com.example.ios on iOS
            iosParameters("com.example.ios") { }
        }

        val dynamicLinkUri = dynamicLink.uri
        Log.d("DynamicLink!!", dynamicLinkUri.toString())
    }

    fun getInviteDeepLink(roomId: String,): Uri {

        return Uri.parse("https://manito.page.link/game/${roomId}")
    }

    fun test(activity: Activity, roomId: String){
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(getInviteDeepLink("example"))
            .setDynamicLinkDomain("manito.page.link")
            .buildShortDynamicLink()
            .addOnCompleteListener(activity){
                task-> if(task.isSuccessful){
                    val shortLink: Uri = task.result.shortLink!!
                try{
                    Log.d("testCod", shortLink.toString())
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString())
                    sendIntent.type = "text/plain"
                    activity.startActivity(Intent.createChooser(sendIntent, "Share"))
                } catch(ignored: ActivityNotFoundException){
                }
                }else{
                    Log.d("test",task.toString())
                }
            }
    }

    fun test2() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                var deeplink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deeplink = pendingDynamicLinkData.link
                }

                if (deeplink != null) {
                    val segment: String = deeplink.lastPathSegment!!
                    Log.d("test", segment.toString())
                }
            }
    }


                fun createShortDynamicLink() {
                    val shortLink = Firebase.dynamicLinks.shortLinkAsync {
                        link = getInviteDeepLink("example")
                        domainUriPrefix = "https://manito.page.link"

                    }.addOnSuccessListener { (shortLink, flowchartLink) ->


                        processShortLink(shortLink, flowchartLink)
                    }.addOnFailureListener {
                        //error
                    }
                }

                fun shareLink(myDynamicLink: Uri) {
                    // [START ddl_share_link]
                    val sendIntent = Intent().apply {
                        val msg = "Hey, check this out: $myDynamicLink"
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, msg)
                        type = "text/plain"
                    }
                    startActivity(sendIntent)
                    // [END ddl_share_link]
                }

                fun handleDeepLink() {
                    Firebase.dynamicLinks
                        .getDynamicLink(intent)
                        .addOnSuccessListener(this) { pendingDynamicLinkData ->
                            // Get deep link from result (may be null if no link is found)
                            var deepLink: Uri? = null
                            if (pendingDynamicLinkData != null) {
                                deepLink = pendingDynamicLinkData.link
                            }
                            var roomCode = deepLink?.lastPathSegment
                            Log.d("AAwkfaksA", roomCode.toString())

                            // [START_EXCLUDE]
                            // Display deep link in the UI
                            if (deepLink != null) {
                                Snackbar.make(
                                    findViewById(android.R.id.content),
                                    "Found deep link!", Snackbar.LENGTH_LONG
                                ).show()

                                val linkReceiveTextView = bind.linkviewReceive2

                                linkReceiveTextView.text = deepLink.toString()
                            } else {
                                Log.d(this@RoomActivity.toString(), "getDynamicLink: no link found")
                            }
                            // [END_EXCLUDE]
                        }
                        .addOnFailureListener(this) { e ->
                            Log.w(this@RoomActivity.toString(), "getDynamicLink:onFailure", e)
                        }
                }

                fun getInvitation() {
                    // [START ddl_get_invitation]
                    Firebase.dynamicLinks
                        .getDynamicLink(intent)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                // Handle error
                                // ...
                            }
                            val invite = FirebaseAppInvite.getInvitation(task.result)
                            if (invite != null) {
                                // Handle invite
                                // ...
                            }
                        }
                    // [END ddl_get_invitation]
                }

}
