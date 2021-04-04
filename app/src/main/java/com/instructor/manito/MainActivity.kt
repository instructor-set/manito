package com.instructor.manito

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.instructor.manito.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d("test", "test")
    }
}