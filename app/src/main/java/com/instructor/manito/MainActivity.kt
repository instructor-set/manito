package com.instructor.manito


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityMainBinding
import com.instructor.manito.databinding.AlertdialogEdittextBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Database
import splitties.activities.start
import splitties.toast.toast

class MainActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val dataList = arrayListOf<Room>()
    private val adapter = MainRoomAdapter(this@MainActivity, dataList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(bind) {
            setContentView(root)
            createRoomButton.setOnClickListener {
                start<CreateActivity> {
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            }


            enterRoomButton.setOnClickListener {
                val builder = AlertDialog.Builder(this@MainActivity)
                val builderItem = AlertdialogEdittextBinding.inflate(layoutInflater)
                val editText = builderItem.editText
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



            mainRecycler.adapter = adapter
            mainRecycler.layoutManager = LinearLayoutManager(this@MainActivity)

            //bind.swipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);
            swipeRefreshLayout.setOnRefreshListener {
                onRefreshListener()
            }

        }

    }

    override fun onStart() {
        super.onStart()
        onRefreshListener()
    }

    private fun onRefreshListener() {
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


