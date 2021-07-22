package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityMainBinding
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    private val main by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(main.root)
        main.button.setOnClickListener {
            start<LobbyActivity>()
        }
    }
}
