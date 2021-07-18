package com.instructor.manito

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class CreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val createTitle = findViewById<EditText>(R.id.textName)
        val createPassword = findViewById<EditText>(R.id.textPassword)
        val createButton = findViewById<Button>(R.id.buttonCreate)

        var create = intent.getSerializableExtra("create")

        createTitle.setText(create.title)
        createPassword.setText(create.passWord)

        //방만들기,,,

    }


}