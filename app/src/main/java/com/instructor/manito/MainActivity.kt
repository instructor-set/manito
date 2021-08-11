package com.instructor.manito


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.tasks.OnSuccessListener
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
    private val roomAdapter = MainMyRoomAdapter(this@MainActivity, myRoomList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(bind) {
            setContentView(root)
            if (!Authentication.isLoggedIn()) {
                finish()
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
            mainRecycler.layoutManager = LinearLayoutManager(this@MainActivity)

            // 내가 들어간 방 어댑터 연결
            mainManitoRoomRecycler.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            mainManitoRoomRecycler.adapter = roomAdapter

            // 새로고침
            //bind.swipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);
            swipeRefreshLayout.setOnRefreshListener {
                Database.getReference("rooms").get().addOnSuccessListener {
                    refreshChatList(it)
                }
            }

        }
        test()



    }

    private fun refreshChatList(snapshot: DataSnapshot) {
        dataList.clear()
        for (roomPair in snapshot.children) {
            val room = roomPair.getValue<Room>()!!
            dataList.add(room)
        }
        dataList.reverse()
        adapter.notifyDataSetChanged()
        bind.swipeRefreshLayout.setRefreshing(false)
    }

    fun test() {
        myRoomList.clear()
        Database.getReference("users/${Authentication.uid}/rooms").addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                //get 사용하기
                val rid = snapshot.key as String
                Database.getReference("rooms/${rid}").addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        myRoomList.add(snapshot.getValue<Room>()!!)
                        Util.j(myRoomList.toString())
                        roomAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // 어댑터 안의 내용 삭제
                // hashmap?
                // remove
                // 값을 get으로 가지고 와서 room으로
                // 안되면 hashmap 사용해서 rid 키로
                myRoomList.remove(snapshot.getValue<Room>())
                adapter.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //시작할 때 새로고침
    override fun onStart() {
        super.onStart()
        with(bind) {
            swipeRefreshLayout.setRefreshing(true)
            Database.getReference("rooms").get().addOnSuccessListener {
                refreshChatList(it)
            }

        }

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