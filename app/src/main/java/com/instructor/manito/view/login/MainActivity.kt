package com.instructor.manito.view.login


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.instructor.manito.databinding.ActivityMainBinding
import com.instructor.manito.databinding.DialogEditnameBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.dto.User
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import com.instructor.manito.view.LoginActivity
import com.instructor.manito.view.login.main.CreateActivity
import com.instructor.manito.view.login.main.MainMyRoomAdapter
import com.instructor.manito.view.login.main.MainRoomAdapter
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val inflater: LayoutInflater by lazy { LayoutInflater.from(this@MainActivity) }

    private val dataList = arrayListOf<Room>()
    private val adapter = MainRoomAdapter(this@MainActivity, dataList)

    // 내가 들어간 방
    private val myRoomList = arrayListOf<Room>()
    private val myRoomIndexMap = hashMapOf<String, Int>()
    private val myRoomValueEventListenerMap = hashMapOf<String, ValueEventListener>()
    private val roomAdapter = MainMyRoomAdapter(this@MainActivity, myRoomList)

    private fun withDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { data: PendingDynamicLinkData? ->
                data?.link?.lastPathSegment?.let {
                    adapter.enterRoom(this, it, Authentication.uid)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(bind) {
            setContentView(root)


            if (!Authentication.isLoggedIn()) {
                finish()
                start<LoginActivity>()
            }
            withDynamicLink()
            Util.uidToNickname(Authentication.uid.toString(), repeat = true) {
                if (it != Util.MESSAGE_UNDEFINED) {
                    toolbar.title = it as String
                }
            }
            Database.getReference("users/${Authentication.uid}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Authentication.user = snapshot.getValue<User>()
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

            // 방 만들기
            createRoomButton.setOnClickListener {
                start<CreateActivity> {
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            }

            // 어댑터 연결
            mainRecycler.adapter = adapter

            // 내가 들어간 방 어댑터 연결
            mainManitoRoomRecycler.adapter = roomAdapter

            // 새로고침
            //bind.swipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);
            swipeRefreshLayout.setOnRefreshListener {
                refreshRoomList(false)
            }

            // 닉네임 수정
            with(DialogEditnameBinding.inflate(inflater)) {
                val nicknameEditDialog = AlertDialog.Builder(this@MainActivity).setView(root)
                    .setPositiveButton("수정") { _: DialogInterface, _: Int ->
                        if(nameEditText.text.isBlank()){
                           return@setPositiveButton
                        }
                        Database.getReference("users/${Authentication.uid}/nickname")
                            .setValue(nameEditText.text.toString().replace(" ", ""))
                    }.setNeutralButton("취소", null)
                    .create()
                editName.setOnClickListener {
                    nameEditText.hint = Authentication.nickname
                    nicknameEditDialog.show()
                }
            }

        }
        test()


    }

    fun refreshRoomList(refreshing: Boolean) {
        bind.swipeRefreshLayout.setRefreshing(refreshing)
        Database.getReference("rooms").get().addOnSuccessListener {
            dataList.clear()
            for (roomPair in it.children) {
                val room = roomPair.getValue<Room>()!!
                if (room.state == Room.STATE_WAIT) {
                    dataList.add(room)
                }
            }
            dataList.reverse()
            adapter.notifyDataSetChanged()
            bind.swipeRefreshLayout.setRefreshing(false)
        }
    }

    fun test() {

        Database.getReference("users/${Authentication.uid}/rooms")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val rid = snapshot.key as String

                    myRoomValueEventListenerMap[rid] = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists())
                                return
                            if (myRoomIndexMap.containsKey(rid)) {
                                val myRoomIndex = myRoomIndexMap.getValue(rid)
                                myRoomList[myRoomIndex] = snapshot.getValue<Room>()!!
                                roomAdapter.notifyItemChanged(myRoomIndex)
                            } else {
                                val myRoomIndex = myRoomList.lastIndex + 1
                                myRoomList.add(snapshot.getValue<Room>()!!)
                                myRoomIndexMap[rid] = myRoomIndex
                                roomAdapter.notifyItemInserted(myRoomIndex)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    }

                    Database.getReference("rooms/${rid}")
                        .addValueEventListener(myRoomValueEventListenerMap.getValue(rid))

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val rid = snapshot.key!!
                    val myRoomIndex = myRoomIndexMap.getValue(rid)
                    myRoomIndexMap.remove(rid)
                    Database.getReference("rooms/$rid")
                        .removeEventListener(myRoomValueEventListenerMap.getValue(rid))
                    myRoomValueEventListenerMap.remove(rid)
                    myRoomList.removeAt(myRoomIndex)
                    roomAdapter.notifyItemRemoved(myRoomIndex)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    //시작할 때 새로고침
    override fun onStart() {
        super.onStart()
        refreshRoomList(true)
    }

}