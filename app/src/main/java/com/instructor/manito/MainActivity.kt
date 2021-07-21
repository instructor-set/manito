package com.instructor.manito

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityMainBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    var dataList = arrayListOf<Room>()
    val adapter = MainAdapter(this@MainActivity, dataList)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            setContentView(root)
            createRoomButton.setOnClickListener {
                start<CreateActivity>{
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            }
            enterRoomButton.setOnClickListener {
                Toast.makeText(this@MainActivity, dataList.toString(), Toast.LENGTH_LONG).show()
            }

            Database.getReference("rooms").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for(a in snapshot.children){
                        dataList.add(a.getValue<Room>()!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Util.j(error.details)
                }

            })

            mainRecycler.adapter = adapter
            mainRecycler.layoutManager = LinearLayoutManager(this@MainActivity)

        }


    }
}