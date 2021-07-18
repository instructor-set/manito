package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityCreateBinding
import splitties.fragments.addToBackStack
import splitties.fragments.fragmentTransaction

class CreateActivity : AppCompatActivity() {
    private val create by lazy {
        ActivityCreateBinding.inflate(layoutInflater)
    }

    private lateinit var adminRoomFragment: AdminRoomFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(create.root)


        val createTitle = create.textName
        val createPassword = create.textPassword
        val createButton = create.buttonCreate

        adminRoomFragment = AdminRoomFragment.newInstance(1, createTitle.text.toString(), createPassword.text.toString(), "진하?")

        create.buttonCreate.setOnClickListener {
            fragmentTransaction(false) {
                replace(R.id.mainFrame, adminRoomFragment)
                addToBackStack()
            }
        }

    }


}