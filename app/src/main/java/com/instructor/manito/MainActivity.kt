package com.instructor.manito

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {


    var dataList = arrayListOf<Room>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fun makeDummyData() : MutableList<Room>{
            val data : MutableList<Room> = mutableListOf()

            val roomT = Room(0, "test", null)
            data.add(roomT)
            for(no in 1..10){
                val num = no
                val title = "${no}번째"
                val key = no - 1
                val room = Room(num, title, key)
                data.add(room)
            }
            return data
        }

        val enterRoomButton = findViewById<Button>(R.id.EnterRoomButton)
        enterRoomButton.setOnClickListener {
            Toast.makeText(this, dataList.toString(), Toast.LENGTH_LONG).show()
        }

        dataList = makeDummyData() as ArrayList<Room>

        val adapter = MainAdapter(this, dataList)
        val mainRecycler = findViewById<RecyclerView>(R.id.mainRecycler)

        mainRecycler.adapter = adapter
        mainRecycler.layoutManager = LinearLayoutManager(this)


    }
}