package com.instructor.manito


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityMainBinding
import com.instructor.manito.databinding.AlertdialogEdittextBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import splitties.activities.start
import splitties.toast.toast

class MainActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val dataList = arrayListOf<Room>()
    private val adapter = MainRoomAdapter(this@MainActivity, dataList)

    // 내가 들어간 방
    private val myRoomList = arrayListOf<Room>()
    private val myRoomIndexMap = hashMapOf<String, Int>()
    private val myRoomValueEventListenerMap = hashMapOf<String, ValueEventListener>()
    private val roomAdapter = MainMyRoomAdapter(this@MainActivity, myRoomList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(bind) {
            setContentView(root)

            if (!Authentication.isLoggedIn()) {
                finish()
            }
            Util.uidToNickname(Authentication.uid.toString()) {
                if (it != Util.MESSAGE_UNDEFINED) {
                    toolbar.title = it as String
                }
            }

            // 방 만들기
            createRoomButton.setOnClickListener {
                start<CreateActivity> {
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            }

            //방 입장하기
            enterRoomButton.setOnClickListener {
                val builder = AlertDialog.Builder(this@MainActivity)
                val builderItem = AlertdialogEdittextBinding.inflate(layoutInflater)
                val editText = builderItem.alertEditText
                with(builder) {
                    setTitle("초대 링크를 입력하세요")
                    setView(builderItem.root)
                    setPositiveButton("OK") { _: DialogInterface, _: Int ->
                        if (editText.text != null) toast("입력된 것 : ${editText.text}")
                    }
                    show()
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
                            Util.j("$rid, ${snapshot.getValue<Room>()}")
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
    /*
    패스워드 V
    방 정보창 V
    내 방 V
    비밀번호 사람수 위치 바꾸기 V
     */
    /*
    해야할 거
    미션 - 1:1 체크
    랜덤 매칭 알고리즘
    최대 인원수 저지

     */
    /*
    파이어베이스 추가
    sha 추가
    figma 얘기해주고
    지금까지한거 물어보고
    물어볼거있냐 물어보고
    최대인원수 추가해줘!
     */


//    fun test() {
//        Database.getReference("users/${Authentication.uid}/rooms").get().addOnSuccessListener {
//            for (child in it.children) {
//                Database.getReference("rooms/${child.key}").get().addOnSuccessListener {
//                    val room = it.getValue<Room>()
//
//                }
//            }
//        }
//    }


}