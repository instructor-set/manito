package com.instructor.manito


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityMainBinding
import com.instructor.manito.databinding.AlertdialogEdittextBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import splitties.activities.start
import splitties.toast.toast

class MainActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val dataList = arrayListOf<Room>()
    private val adapter = MainRoomAdapter(this@MainActivity, dataList)

    //내가 들어간 방
    private val roomIEntered = arrayListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Authentication.isLoggedIn()) {
            finish()
        }
        with(bind) {
            setContentView(root)

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
            /*
            Database.getReference("rooms").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (a in snapshot.children) {
                        dataList.add(a.getValue<Room>()!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Util.j(error.details)
                }

            })

             */


            // 어댑터 연결
            mainRecycler.adapter = adapter
            mainRecycler.layoutManager = LinearLayoutManager(this@MainActivity)

            // 새로고침
            //bind.swipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);
            swipeRefreshLayout.setOnRefreshListener {
                Database.getReference("rooms").get().addOnSuccessListener {
                    dataList.clear()
                    for (room in it.children) {
                        dataList.add(room.getValue<Room>()!!)
                    }
                    adapter.notifyDataSetChanged()
                    swipeRefreshLayout.setRefreshing(false)
                }
            }
            val updates: MutableMap<String, Any> = HashMap()

        }

        val mDatabase = Database.getReference("rooms")
        mDatabase.child(Authentication.uid.toString()).addValueEventListener(object :
            ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val name = postSnapshot.child("rooms").getValue(true)
                    Log.e("myName", name.toString())

                }

            }

        }


        )



    }






    //시작할 때 새로고침
    override fun onStart() {
        super.onStart()
        with (bind) {
            swipeRefreshLayout.setRefreshing(true)
            Database.getReference("rooms").get().addOnSuccessListener {
                dataList.clear()
                for (room in it.children) {
                    dataList.add(room.getValue<Room>()!!)
                }
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.setRefreshing(false)

            }
            Database.getReference("rooms").get().addOnSuccessListener {
                for(room in it.children){
                    roomIEntered.clear()
                    if(Authentication.uid == room.getValue<Room>()?.manager){
                        roomIEntered.add(room.getValue<Room>()!!)
                    }

                }
                Log.d("myRoom", roomIEntered.toString())
            }


        }

    }



    //data class Room(var no: Long? = null, var title: String? = null, var password: String? = null, val numberOfPeople: Int? = 0, val participatingUsers: MutableList<User>? = null)
    //data class User(val nickname: String = "")
//    fun makeDummyData() : MutableList<Room>{
//        val data : MutableList<Room> = mutableListOf()
//
//        val roomT = Room(0, "test", null)
//        data.add(roomT)
//        for(no in 1..10){
//            val num = no
//            val title = "${no}번째"
//            val key = no - 1
//            val users : MutableList<User> = mutableListOf(User("가은"), User("진하"), User("성덕"))
//            val room = Room(num.toLong(), title, key.toString(), no * 2, users = users)
//            Log.e("room", users.toString())
//            data.add(room)
//
//        }
//
//        return data
//    }

}


