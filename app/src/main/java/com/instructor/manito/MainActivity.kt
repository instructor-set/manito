package com.instructor.manito

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityMainBinding
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    private val adminRoomFragment
        // AdminRoomFragment.newInstance(1, "아무나 들어와라", 1234, "진하?")

    private val main by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        fun createRoom(): MutableList<Create>

        val title : String
        val passWord :Int

        main.button.setOnClickListener {
            adminRoomFragment.change()
        }
        main.roomButton.setOnClickListener{
            fragmentTransaction(false) {
                replace(R.id.mainFrame, adminRoomFragment)
                addToBackStack()
            }
        }
    }
}