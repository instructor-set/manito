package com.instructor.manito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityMainBinding
import splitties.activities.start

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            start<LobbyActivity>()
        }
    }
}