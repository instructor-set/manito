package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityTestaBinding
import splitties.fragments.addToBackStack
import splitties.fragments.fragmentTransaction

class TestAActivity : AppCompatActivity() {

    private val adminRoomFragment = AdminRoomFragment.newInstance(1, "아무나 들어와라", 1234, "진하?")

    private val main by lazy {
        ActivityTestaBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(main.root)
        main.button.setOnClickListener {
            adminRoomFragment.change()
        }
        main.roomButton.setOnClickListener {
            fragmentTransaction(false) {
                replace(R.id.mainFrame, adminRoomFragment)
                addToBackStack()
            }
        }

    }
}