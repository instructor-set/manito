package com.instructor.manito

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.baoyz.widget.PullRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityMainBinding
import com.instructor.manito.databinding.AlertdialogEdittextBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.dto.User
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import kotlinx.android.synthetic.main.activity_main.*
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    var dataList = arrayListOf<Room>()
    //val adapter = MainAdapter(this@MainActivity, dataList)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
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
                    setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
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

            dataList = makeDummyData() as ArrayList<Room>

            val adapter = MainAdapter(this@MainActivity, dataList)

            mainRecycler.adapter = adapter
            mainRecycler.layoutManager = LinearLayoutManager(this@MainActivity)

            //bind.swipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);
            bind.swipeRefreshLayout.setOnRefreshListener {
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

                adapter.notifyDataSetChanged()
                bind.swipeRefreshLayout.setRefreshing(false)

            }

        }

    }

    fun toast(message:String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //data class Room(var no: Long? = null, var title: String? = null, var password: String? = null, val numberOfPeople: Int? = 0, val participatingUsers: MutableList<User>? = null)
    //data class User(val nickname: String = "")
    fun makeDummyData() : MutableList<Room>{
        val data : MutableList<Room> = mutableListOf()

        val roomT = Room(0, "test", null)
        data.add(roomT)
        for(no in 1..10){
            val num = no
            val title = "${no}번째"
            val key = no - 1
            val users : MutableList<User> = mutableListOf(User("가은"), User("진하"), User("성덕"))
            val room = Room(num.toLong(), title, key.toString(), no * 2, participatingUsers = users)
            Log.e("room", users.toString())
            data.add(room)

        }

        return data
    }

}







