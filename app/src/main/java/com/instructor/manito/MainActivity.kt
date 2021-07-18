package com.instructor.manito

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructor.manito.databinding.ActivityMainBinding
import com.instructor.manito.dto.Room
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    private val main by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    var dataList = arrayListOf<Room>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(main.root)

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
        main.createRoomButton.setOnClickListener {
            start<CreateActivity>()
        }
        main.enterRoomButton.setOnClickListener {
            Toast.makeText(this, dataList.toString(), Toast.LENGTH_LONG).show()
        }

        dataList = makeDummyData() as ArrayList<Room>

        val adapter = MainAdapter(this, dataList)
        val mainRecycler = main.mainRecycler

        mainRecycler.adapter = adapter
        mainRecycler.layoutManager = LinearLayoutManager(this)


    }
}