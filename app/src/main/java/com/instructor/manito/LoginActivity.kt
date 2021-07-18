package com.instructor.manito

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityLoginBinding
import splitties.activities.start

class LoginActivity : AppCompatActivity() {

    private val login by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(login.root)
        login.testAButton.setOnClickListener {
            Log.d("jinha","what?")
            start<TestAActivity>()
        }


    }
}